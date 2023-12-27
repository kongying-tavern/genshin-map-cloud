package main

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"image"
	"image/jpeg"
	"image/png"
	"io"
	"log"
	"net/http"
	"net/url"
	"os"
	"os/signal"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/9d77v/go-pkg/cache/redis"
	"github.com/9d77v/go-pkg/env"
	"github.com/chai2010/webp"
	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
	"github.com/minio/minio-go/v7/pkg/notification"
)

var (
	redisList            = env.String("REDIS_LIST", "MINIO_BUCKET_NOTIFY:IMAGE")
	minioBucketName      = env.String("MINIO_BUCKET", "image")
	minioAddress         = env.String("MINIO_ADDRESS", "oss.domain.local:9000")
	minioAccessKeyID     = env.String("MINIO_ACCESS_KEY_ID", "minio")
	minioSecretAccessKey = env.String("MINIO_SECRET_ACCESS_KEY", "minio123")
	useSSL               = env.Bool("MINIO_USE_SSL", false)
	quality              = env.Float32("WEBP_QUALITY", 75)
	mode                 = env.Int("MODE", 0) //0:upload xxx.webp to minio,1:replace origin image with webp content,2:upload xxx.webp to minio and delete origin image
)

func main() {
	ctx := context.Background()
	imageCh := make(chan notification.Event)
	go handleRedis(ctx, imageCh)
	signals := make(chan os.Signal, 1)
	signal.Notify(signals, os.Kill, os.Interrupt, syscall.SIGINT, syscall.SIGTERM)
	for {
		select {
		case imageURL := <-imageCh:
			go handleImage(ctx, imageURL)
		case <-signals:
			log.Println("退出程序")
			time.Sleep(2 * time.Second)
			os.Exit(0)
		}
	}
}

var (
	minioClient *minio.Client
	once        sync.Once
)

func init() {
	creds := credentials.NewStaticV4(minioAccessKeyID, minioSecretAccessKey, "")
	var err error
	minioClient, err = minio.New(minioAddress, &minio.Options{
		Creds:  creds,
		Secure: useSSL,
	})
	if err != nil {
		log.Fatalln(err)
	}
}

type Record struct {
	Event []notification.Event
}

func handleRedis(ctx context.Context, imageCh chan notification.Event) {
	redis.GetClient().ReadDataFromQueue(ctx, func(queue, msg string) {
		records := make([]Record, 0)
		err := json.Unmarshal([]byte(msg), &records)
		if err != nil {
			log.Println("json unmarshal error:", err)
			return
		}
		for _, record := range records {
			for _, e := range record.Event {
				imageCh <- e
			}
		}
	}, redisList)
}

func handleImage(ctx context.Context, data notification.Event) {
	imageName, contentType := data.S3.Object.Key, data.S3.Object.ContentType
	imageName, _ = url.QueryUnescape(imageName)
	object, err := minioClient.GetObject(ctx, minioBucketName, imageName, minio.GetObjectOptions{})
	if err != nil {
		log.Println("GetObject failed:", err)
		return
	}
	webp, err := encodeImage(object, contentType, quality)
	if err != nil {
		log.Println("encodeImage failed:", err)
		return
	}
	buf := bytes.NewBuffer(webp)
	webpName := strings.Split(imageName, ".")[0] + ".webp"
	switch mode {
	case 0:
		_, err := minioClient.PutObject(ctx, minioBucketName, webpName, buf, int64(buf.Len()), minio.PutObjectOptions{ContentType: "image/webp"})
		if err != nil {
			log.Println("PutObject failed:", err)
			return
		}
	case 1:
		_, err := minioClient.PutObject(ctx, minioBucketName, imageName, buf, int64(buf.Len()), minio.PutObjectOptions{ContentType: "image/webp"})
		if err != nil {
			log.Println("PutObject failed:", err)
			return
		}
	case 2:
		_, err := minioClient.PutObject(ctx, minioBucketName, webpName, buf, int64(buf.Len()), minio.PutObjectOptions{ContentType: "image/webp"})
		if err != nil {
			log.Println("PutObject failed:", err)
			return
		}
		err = minioClient.RemoveObject(ctx, minioBucketName, imageName, minio.RemoveObjectOptions{})
		if err != nil {
			log.Println("PutObject failed:", err)
			return
		}
	}
}

var validContentTypeMap = map[string]bool{
	"image/jpeg": true,
	"image/png":  true,
}

func getContentType(data []byte) string {
	return http.DetectContentType(data)
}

func encodeImage(r io.Reader, contentType string, quality float32) ([]byte, error) {
	var img image.Image
	var err error
	switch contentType {
	case "image/jpeg":
		img, err = jpeg.Decode(r)
	case "image/png":
		img, err = png.Decode(r)
	default:
		return nil, errors.New("not supported image type. support image/jpeg,image/png")
	}
	if img == nil || err != nil {
		return nil, err
	}
	return webp.EncodeRGBA(img, quality)
}
