package worker

import (
	"sync"
)

//StaticPool 固定协程个数的协程池
type StaticPool struct {
	capacity int //最大容量
	wg       *sync.WaitGroup
	taskCh   chan func()
}

//NewStaticPool 初始化固定容量的协程池
func NewStaticPool(capacity int) *StaticPool {
	if capacity < 1 {
		capacity = 1
	}
	p := &StaticPool{
		capacity: capacity,
		wg:       new(sync.WaitGroup),
		taskCh:   make(chan func()),
	}
	p.wg.Add(capacity)
	for i := 0; i < capacity; i++ {
		go func() {
			for task := range p.taskCh {
				task()
			}
			p.wg.Done()
		}()
	}
	return p
}

//Add 新增任务
func (p *StaticPool) Add(task func()) {
	p.taskCh <- task
}

//Stop 结束任务
func (p *StaticPool) Stop() {
	close(p.taskCh)
	p.wg.Wait()
}
