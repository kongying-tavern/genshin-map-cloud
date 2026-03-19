FROM openresty/openresty:alpine-fat

WORKDIR /data

COPY docker/config/img-alist-builder/minio-proxy/modules-enabled/ /etc/nginx/modules-enabled/
COPY docker/config/img-alist-builder/minio-proxy/conf.d/ /etc/nginx/conf.d/

RUN opm get agentzh/lua-resty-http openresty/lua-resty-dns && \
    mkdir -p ./log && \
    rm -f /etc/nginx/conf.d/default.conf

VOLUME ["/data/log"]
EXPOSE 80
