package elastic

import (
	"context"
	"errors"
	"log"
	"sort"
	"strings"
	"sync"
	"time"

	"docker-component.local/minio-image-to-webp/go-pkg/algorithm/math"
	"docker-component.local/minio-image-to-webp/go-pkg/env"
	"docker-component.local/minio-image-to-webp/go-pkg/worker"

	elastic "github.com/olivere/elastic/v7"
)

var (
	address = env.String("ESADDR", "http://domain.local:9200")
	client  *Client
	once    sync.Once
)

//GetClient get elasticsearch connection
func GetClient() *Client {
	once.Do(func() {
		client = newClient()
	})
	return client
}

//Client elasticsearch客户端
type Client struct {
	*elastic.Client
}

func newClient() *Client {
	c, err := elastic.NewClient(elastic.SetURL(strings.Split(address, ",")...), elastic.SetSniff(false))
	if err != nil {
		panic(err)
	}
	return &Client{c}
}

//AggsParam 聚合搜索条件
type AggsParam struct {
	Field string
	Size  int
}

//Aggs 聚合搜索
func Aggs(s *elastic.SearchService, params ...*AggsParam) *elastic.SearchService {
	if s != nil {
		for _, param := range params {
			s = s.Aggregation("group_by_"+param.Field, elastic.NewTermsAggregation().
				Field(param.Field).Size(param.Size))
		}
	}
	return s
}

//GetNewIndexName 获取新索引名
func (c *Client) GetNewIndexName(aliasName, layout string, nowTime ...time.Time) string {
	now := time.Now()
	if len(nowTime) > 0 {
		now = nowTime[0]
	}
	return aliasName + "-" + now.Format(layout)
}

//GetOldIndexNames 获取旧索引名
func (c *Client) GetOldIndexNames(ctx context.Context, aliasName string) []string {
	res, err := c.Aliases().Do(ctx)
	if err != nil {
		log.Println(err)
		return []string{}
	}
	return res.IndicesByAlias(aliasName)
}

//FindIndexesByAlias 获取别名相关有序索引名
func (c *Client) FindIndexesByAlias(ctx context.Context, aliasName, layout string) []string {
	indexNames, err := c.IndexNames()
	if err != nil {
		log.Println("获取索引名失败", err)
		return []string{}
	}
	intArr := make([]int, 0)
	for _, v := range indexNames {
		if strings.Contains(v, aliasName) {
			vIndex := strings.Replace(v, aliasName+"-", "", 1)
			t, err := time.ParseInLocation(layout, vIndex, time.Local)
			if err != nil {
				log.Println("时间转换失败", err)
				continue
			}
			intArr = append(intArr, int(t.Unix()))
		}
	}
	sort.Ints(intArr)
	indexArr := make([]string, 0)
	for _, v := range intArr {
		indexArr = append(indexArr, c.GetNewIndexName(aliasName, layout, time.Unix(int64(v), 0)))
	}
	return indexArr
}

//KeepIndex 保留最新的几个索引
func (c *Client) KeepIndex(ctx context.Context, indexNames []string, max int) error {
	if len(indexNames) <= max {
		return nil
	}
	_, err := c.DeleteIndex(indexNames[:len(indexNames)-max]...).Do(ctx)
	return err
}

//SetNewAlias 设置新的别名
func (c *Client) SetNewAlias(ctx context.Context, aliasName, newIndexName string) error {
	indexNames := c.GetOldIndexNames(ctx, aliasName)
	aliasActions := make([]elastic.AliasAction, 0, 1+len(indexNames))
	aliasActions = append(aliasActions, elastic.NewAliasAddAction(aliasName).Index(newIndexName))
	if len(indexNames) > 0 {
		aliasActions = append(aliasActions, elastic.NewAliasRemoveAction(aliasName).Index(indexNames...))
	}
	aliasService := elastic.NewAliasService(c.Client)
	_, err := aliasService.Action(aliasActions...).Do(ctx)
	return err
}

//CreateIndex 创建es索引
func (c *Client) CreateIndex(ctx context.Context, indexName, mapping string) error {
	exists, err := c.IndexExists(indexName).Do(ctx)
	if err != nil {
		return err
	}
	if !exists {
		// Create a new index.
		createIndex, err := c.Client.CreateIndex(indexName).BodyString(mapping).Do(ctx)
		if err != nil {
			// Handle error
			return err
		}
		if !createIndex.Acknowledged {
			return errors.New("操作失败")
		}
	}
	return nil
}

//BulkDoc es doc
type BulkDoc struct {
	ID  string
	Doc interface{}
}

//BulkInsert 多worker按一定数量批量导入es
func (c *Client) BulkInsert(ctx context.Context, bds []*BulkDoc, indexName string, bulkNum, workerNum int) []error {
	size := len(bds)
	max := size/bulkNum + 1
	pool := worker.NewStaticPool(workerNum)
	errs := make([]error, 0, max)
	for i := 0; i < max; i++ {
		i := i
		pool.Add(func() {
			bulkService := elastic.NewBulkService(c.Client)
			for j := i * bulkNum; j < math.Min(size, (j+1)*bulkNum); j++ {
				req := elastic.NewBulkIndexRequest()
				req.Index(indexName).
					Id(bds[j].ID).
					Doc(bds[j].Doc)
				bulkService.Add(req)
			}
			_, err := bulkService.Do(ctx)
			if err != nil {
				errs = append(errs, err)
			}
		})
	}
	pool.Stop()
	return errs
}
