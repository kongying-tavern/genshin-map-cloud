package worker

import (
	"sync"
)

type empty struct{}

//LimitPool 带并发控制的协程池
type LimitPool struct {
	wg       *sync.WaitGroup
	capacity int        //最大容量
	limitCh  chan empty //控制并发的channel
}

//NewLimitPool 新的LimitPool
func NewLimitPool(capacity int) *LimitPool {
	if capacity < 1 {
		capacity = 1
	}
	return &LimitPool{
		wg:       new(sync.WaitGroup),
		capacity: capacity,
		limitCh:  make(chan empty, capacity),
	}
}

//Add 增加新任务
func (p *LimitPool) Add(task func()) {
	p.limitCh <- empty{}
	p.wg.Add(1)
	go p.do(task)
}

//Stop 结束任务
func (p *LimitPool) Stop() {
	p.wg.Wait()
}

//do 执行某个任务
func (p *LimitPool) do(task func()) {
	task()
	<-p.limitCh
	p.wg.Done()
}
