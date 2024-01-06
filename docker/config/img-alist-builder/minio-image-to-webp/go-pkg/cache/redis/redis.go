package redis

import (
	"context"
	"encoding/json"
	"log"
	"math/rand"
	"strconv"
	"sync"
	"time"

	redis "github.com/go-redis/redis/v8"
	"github.com/google/uuid"

	"docker-component.local/minio-image-to-webp/go-pkg/env"
)

//环境变量
var (
	redisAddress  = env.StringArray("REDIS_ADDRESS", ",", "domain.local:7000", "domain.local:7001", "domain.local:7002", "domain.local:7003", "domain.local:7004", "domain.local:7005")
	redisPassword = env.String("REDIS_PASSWORD")
	client        *Client
	once          sync.Once
)

type Client struct {
	redis.UniversalClient
}

//GetClient get redis connection
func GetClient() *Client {
	once.Do(func() {
		client = newClient()
	})
	return client
}

func newClient() *Client {
	return &Client{redis.NewUniversalClient(&redis.UniversalOptions{
		Addrs:    redisAddress,
		Password: redisPassword,
	})}
}

//DLock 分布式锁
func (c *Client) DLock(ctx context.Context, key string, expire time.Duration, f func()) {
	value := rand.Int()
	err := c.SetEX(ctx, key, value, expire).Err()
	if err == nil {
		f()
		c.DelIfEquals(ctx, key, value)
	} else {
		log.Println("get redis lock failed", err)
	}
}

//DelIfEquals 删除key,value都相等的key
func (c *Client) DelIfEquals(ctx context.Context, key string, value interface{}) {
	script := redis.NewScript(`
		if redis.call("get",KEYS[1])==ARGV[1] then
			return redis.call("del",KEYS[1])
		else
			return 0
		end
	`)
	sha, err := script.Load(ctx, c).Result()
	if err != nil {
		log.Panicln("script is wrong,err:", err)
	}
	_, err = c.EvalSha(ctx, sha, []string{key}, value).Result()
	if err != nil {
		log.Println("exec failed,err:", err)
	}
}

//ReadDataFromQueue 阻塞读
func (c *Client) ReadDataFromQueue(ctx context.Context, f func(queue, msg string), keys ...string) {
	for {
		data, err := c.BLPop(ctx, time.Minute, keys...).Result()
		if err != nil {
			log.Println("blpop error:", err)
			time.Sleep(1 * time.Second)
		} else {
			f(data[0], data[1])
		}
	}
}

//TaskItem 任务
type TaskItem struct {
	ID  string
	Msg interface{}
}

//WriteDelayQueue 写延时队列
func (c *Client) WriteDelayQueue(ctx context.Context, key string, dealTime int64, data interface{}) {
	msg := &TaskItem{
		ID:  uuid.New().String(),
		Msg: data,
	}
	value, err := json.Marshal(msg)
	if err != nil {
		log.Println("json marshal failed,error:", err)
	}
	c.ZAdd(ctx, key, &redis.Z{Score: float64(dealTime), Member: value}).Result()
}

//ReadDelayQueue 读延时队列
func (c *Client) ReadDelayQueue(ctx context.Context, key string, handleMsg func(data *TaskItem)) {
	for {
		values, err := c.ZRangeByScore(ctx, key, &redis.ZRangeBy{
			Max:   strconv.FormatInt(time.Now().Unix(), 10),
			Count: 1}).Result()
		if err != nil {
			log.Println("ZRangeByScore failed,error:", err)
			time.Sleep(time.Second)
			continue
		}
		if len(values) == 0 {
			time.Sleep(time.Second)
			continue
		}
		value := values[0]
		_, err = c.ZRem(ctx, key, value).Result()
		if err == nil {
			msg := new(TaskItem)
			err = json.Unmarshal([]byte(value), msg)
			if err != nil {
				log.Println("json Unmarshal failed,error:", err)
				continue
			}
			handleMsg(msg)
		} else {
			log.Println("ZRem failed,error:", err)
		}
	}
}

//CreateDelayDoubleDelTask 创建延时删除任务，延时3-5s双删
func (c *Client) CreateDelayDoubleDelTask(ctx context.Context, key, delKey string) {
	c.Del(ctx, key).Result()
	c.WriteDelayQueue(ctx, key, time.Now().Add(time.Duration(3+rand.Intn(2))*time.Second).Unix(), delKey)
}

//DelayDoubleDel 执行延时任务延时3-5s双删
func (c *Client) DoDelayDoubleDelTask(ctx context.Context, key string) {
	c.ReadDelayQueue(ctx, key, func(data *TaskItem) {
		c.Del(ctx, data.Msg.(string)).Result()
	})
}
