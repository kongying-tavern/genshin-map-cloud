package worker

import (
	"runtime"
	"testing"
)

func TestStaticPool(t *testing.T) {
	f := func(i int) {
		println(i)
	}
	pool := NewStaticPool(runtime.NumCPU())
	for i := 0; i < 30; i++ {
		i := i
		pool.Add(func() {
			f(i)
		})
	}
	pool.Stop()
	t.Fail()
}

func BenchmarkStaticPool(b *testing.B) {
	f := func(i int) {
		println(i)
	}
	pool := NewStaticPool(runtime.NumCPU())
	for i := 0; i < b.N; i++ {
		i := i
		pool.Add(func() {
			f(i)
		})
	}
	pool.Stop()
}
