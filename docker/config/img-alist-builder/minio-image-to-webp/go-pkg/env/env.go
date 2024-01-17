package env

import (
	"os"
	"strconv"
	"strings"
)

//String ...
func String(key string, value ...string) string {
	s := os.Getenv(key)
	if s == "" && len(value) == 1 {
		return value[0]
	}
	return s
}

//StringArray ...
func StringArray(key, sep string, value ...string) []string {
	ans := []string{}
	s := os.Getenv(key)
	if s == "" && len(value) > 0 {
		ans = value
	} else {
		ans = strings.Split(s, sep)
	}
	return ans
}

//Int ...
func Int(key string, value ...int) int {
	s := os.Getenv(key)
	if s == "" && len(value) == 1 {
		return value[0]
	}
	result, _ := strconv.Atoi(s)
	return result
}

//Bool ...
func Bool(key string, value ...bool) bool {
	s := os.Getenv(key)
	if s == "" && len(value) == 1 {
		return value[0]
	}
	parses, _ := strconv.ParseBool(s)
	return parses
}

//Float32 ...
func Float32(key string, value ...float32) float32 {
	s := os.Getenv(key)
	if s == "" && len(value) == 1 {
		return value[0]
	}
	parses, _ := strconv.ParseFloat(s, 32)
	return float32(parses)
}

//Float64 ...
func Float64(key string, value ...float64) float64 {
	s := os.Getenv(key)
	if s == "" && len(value) == 1 {
		return value[0]
	}
	parses, _ := strconv.ParseFloat(s, 64)
	return parses
}
