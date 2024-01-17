package env

import (
	"math"
	"os"
	"reflect"
	"testing"
)

func TestString(t *testing.T) {
	type args struct {
		key   string
		value []string
	}
	tests := []struct {
		name string
		envs [2]string
		args args
		want string
	}{
		{"should be  default value", [2]string{"KEY", ""}, args{"KEY", []string{"s1"}}, "s1"},
		{"should be  environment value", [2]string{"KEY", "s2"}, args{"KEY", []string{"s1"}}, "s2"},
		{"should be  empty value", [2]string{"KEY", "s2"}, args{"KEY2", []string{}}, ""},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			os.Setenv(tt.envs[0], tt.envs[1])
			if got := String(tt.args.key, tt.args.value...); got != tt.want {
				t.Errorf("String() = %v, want %v", got, tt.want)
			}
			os.Clearenv()
		})
	}
}

func TestStringArray(t *testing.T) {
	type args struct {
		key   string
		sep   string
		value []string
	}
	tests := []struct {
		name string
		envs [2]string
		args args
		want []string
	}{
		{"should be  default value", [2]string{"KEY", ""}, args{"KEY", ",", []string{"s1"}}, []string{"s1"}},
		{"should be  environment value,single", [2]string{"KEY", "s2"}, args{"KEY", ",", []string{"s1"}}, []string{"s2"}},
		{"should be  environment value,multi", [2]string{"KEY", "s1,s2,s3"}, args{"KEY", ",", []string{"s1"}}, []string{"s1", "s2", "s3"}},
	}
	for _, tt := range tests {
		os.Setenv(tt.envs[0], tt.envs[1])
		t.Run(tt.name, func(t *testing.T) {
			if got := StringArray(tt.args.key, tt.args.sep, tt.args.value...); !reflect.DeepEqual(got, tt.want) {
				t.Errorf("StringArray() = %v, want %v", got, tt.want)
			}
		})
		os.Clearenv()
	}
}

func TestInt(t *testing.T) {
	type args struct {
		key   string
		value []int
	}
	tests := []struct {
		name string
		envs [2]string
		args args
		want int
	}{
		{"should be  default value", [2]string{"KEY", ""}, args{"KEY", []int{3}}, 3},
		{"should be  environment value", [2]string{"KEY", "2"}, args{"KEY", []int{3}}, 2},
		{"should be  zero value", [2]string{"KEY", "s2"}, args{"KEY2", []int{}}, 0}}
	for _, tt := range tests {
		os.Setenv(tt.envs[0], tt.envs[1])
		t.Run(tt.name, func(t *testing.T) {
			if got := Int(tt.args.key, tt.args.value...); got != tt.want {
				t.Errorf("Int() = %v, want %v", got, tt.want)
			}
		})
		os.Clearenv()
	}
}

func TestBool(t *testing.T) {
	type args struct {
		key   string
		value []bool
	}
	tests := []struct {
		name string
		envs [2]string
		args args
		want bool
	}{
		{"should be  default value", [2]string{"KEY", ""}, args{"KEY", []bool{true}}, true},
		{"should be  environment value", [2]string{"KEY", "True"}, args{"KEY", []bool{false}}, true},
		{"should be  false value", [2]string{"KEY", "s2"}, args{"KEY2", []bool{}}, false},
	}
	for _, tt := range tests {
		os.Setenv(tt.envs[0], tt.envs[1])
		t.Run(tt.name, func(t *testing.T) {
			if got := Bool(tt.args.key, tt.args.value...); got != tt.want {
				t.Errorf("Bool() = %v, want %v", got, tt.want)
			}
		})
		os.Clearenv()
	}
}

func TestFloat32(t *testing.T) {
	type args struct {
		key   string
		value []float32
	}
	tests := []struct {
		name string
		envs [2]string
		args args
		want float32
	}{
		{"should be  default value", [2]string{"KEY", ""}, args{"KEY", []float32{2.3}}, 2.3},
		{"should be  environment value", [2]string{"KEY", "3.3"}, args{"KEY", []float32{1}}, 3.3},
		{"should be  false value", [2]string{"KEY", "s2"}, args{"KEY2", []float32{}}, 0},
	}
	for _, tt := range tests {
		os.Setenv(tt.envs[0], tt.envs[1])
		t.Run(tt.name, func(t *testing.T) {
			if got := Float32(tt.args.key, tt.args.value...); math.Abs(float64(got-tt.want)) > 1e-6 {
				t.Errorf("Float32() = %v, want %v", got, tt.want)
			}
		})
		os.Clearenv()
	}
}

func TestFloat64(t *testing.T) {
	type args struct {
		key   string
		value []float64
	}
	tests := []struct {
		name string
		envs [2]string
		args args
		want float64
	}{
		{"should be  default value", [2]string{"KEY", ""}, args{"KEY", []float64{2.3}}, 2.3},
		{"should be  environment value", [2]string{"KEY", "3.3"}, args{"KEY", []float64{1}}, 3.3},
		{"should be  false value", [2]string{"KEY", "s2"}, args{"KEY2", []float64{}}, 0},
	}
	for _, tt := range tests {
		os.Setenv(tt.envs[0], tt.envs[1])
		t.Run(tt.name, func(t *testing.T) {
			if got := Float64(tt.args.key, tt.args.value...); math.Abs(float64(got-tt.want)) > 1e-6 {
				t.Errorf("Float64() = %v, want %v", got, tt.want)
			}
		})
		os.Clearenv()
	}
}
