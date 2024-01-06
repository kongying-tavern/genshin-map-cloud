package math

//Max return the maximum number
func Max(a, b int) int {
	if a > b {
		return a
	}
	return b
}

//Min return the minimum number
func Min(a, b int) int {
	if a < b {
		return a
	}
	return b
}

//MaxArr return the maximum number
func MaxArr(a ...int) int {
	max := a[0]
	for _, v := range a[1:] {
		if v > max {
			max = v
		}
	}
	return max
}

//MinArr return the minimum number
func MinArr(a ...int) int {
	min := a[0]
	for _, v := range a[1:] {
		if v < min {
			min = v
		}
	}
	return min
}
