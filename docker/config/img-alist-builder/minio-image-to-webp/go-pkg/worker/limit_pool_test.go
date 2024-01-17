package worker

import (
	"runtime"
	"testing"
)

func TestLimitPool(t *testing.T) {
	f := func(i int) {
		println(i)
	}
	pool := NewLimitPool(runtime.NumCPU())
	for i := 0; i < 30; i++ {
		i := i
		pool.Add(func() {
			f(i)
		})
	}
	pool.Stop()
	t.Fail()
}

func BenchmarkLimitPool(b *testing.B) {
	f := func(i int) {
		println(i)
	}
	pool := NewLimitPool(runtime.NumCPU())
	for i := 0; i < b.N; i++ {
		i := i
		pool.Add(func() {
			f(i)
		})
	}
	pool.Stop()
}
