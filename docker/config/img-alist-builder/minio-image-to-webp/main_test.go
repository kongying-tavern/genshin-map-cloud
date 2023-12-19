package main

import (
	"bytes"
	"io/ioutil"
	"testing"
)

func Test_encodeJPG(t *testing.T) {
	test_encodeImage(t, "./pics/1.jpg", "./pics/1.webp")
}

func Test_encodeJPEG(t *testing.T) {
	test_encodeImage(t, "./pics/2.jpeg", "./pics/2.webp")
}

func Test_encodePNG(t *testing.T) {
	test_encodeImage(t, "./pics/3.png", "./pics/3.webp")
}

func Test_encodeJFIF(t *testing.T) {
	test_encodeImage(t, "./pics/4.jfif", "./pics/4.webp")
}

func test_encodeImage(t *testing.T, inPath, outPath string) {
	data, err := ioutil.ReadFile(inPath)
	if err != nil {
		t.Error("encode webp failed,err:", err)
	}
	contentType := getContentType(data[:32])
	if validContentTypeMap[contentType] {
		data, err := encodeImage(bytes.NewBuffer(data), getContentType(data[:32]), 75)
		if err = ioutil.WriteFile(outPath, data, 0644); err != nil {
			t.Error("WriteFile failed,err:", err)
		}
	} else {
		t.Error("content type error:", contentType)
	}
}
