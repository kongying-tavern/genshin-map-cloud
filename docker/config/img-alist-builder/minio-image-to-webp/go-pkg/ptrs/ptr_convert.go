package ptrs

//String 将string指针转换为string
func String(s *string) string {
	if s != nil {
		return *s
	}
	return ""
}

//StringPtr 获取string指针
func StringPtr(s string) *string {
	return &s
}

//Int 将int指针转换为int
func Int(i *int) int {
	if i != nil {
		return *i
	}
	return 0
}

//IntPtr 获取int指针
func IntPtr(i int) *int {
	return &i
}

//Int64 将int64指针转换为int64
func Int64(i *int64) int64 {
	if i != nil {
		return *i
	}
	return 0
}

//Int64Ptr 获取int64指针
func Int64Ptr(i int64) *int64 {
	return &i
}

//IntPtrToInt64 将int指针转换为int64
func IntPtrToInt64(i *int) int64 {
	if i != nil {
		return int64(*i)
	}
	return 0
}

//IntPtrToInt8 将int指针转换为int8
func IntPtrToInt8(i *int) int8 {
	if i != nil {
		return int8(*i)
	}
	return 0
}

//Float64 将float64指针转换为float64
func Float64(f *float64) float64 {
	if f != nil {
		return *f
	}
	return 0
}

//Float64Ptr 获取float64指针
func Float64Ptr(f float64) *float64 {
	return &f
}

//Bool 将bool指针转换为bool
func Bool(b *bool) bool {
	if b != nil {
		return *b
	}
	return false
}

//BoolPtr 获取bool指针
func BoolPtr(b bool) *bool {
	return &b
}
