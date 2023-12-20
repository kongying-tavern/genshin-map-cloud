# Builder
FROM golang:1.21-bullseye AS builder

WORKDIR /data
ADD docker/config/img-alist-builder/minio-image-to-webp .

RUN \
    go env -w GOPROXY=https://goproxy.io,direct && \
    CGO_ENABLED=1 GOOS=linux GOARCH=amd64 go build -ldflags "-s -w"

# Runner
FROM amd64/debian AS runner
WORKDIR /data
COPY --from=builder /data/minio-image-to-webp ./minio-image-to-webp
ENTRYPOINT ["/data/minio-image-to-webp"]
