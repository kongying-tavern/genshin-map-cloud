FROM nginx:1.25-alpine

WORKDIR /data
COPY docker/config/img-alist-builder/minio-proxy /etc/nginx

RUN mkdir -p ./log && \
    mkdir -p ./cache/proxy_temp_dir && \
    mkdir -p ./cache/proxy_cache_dir && \
    chown -R nginx:nginx .

VOLUME ["/data/cache", "/data/log"]
EXPOSE 80
