FROM minio/mc:latest

WORKDIR /data
ADD docker/config/img-alist-builder/minio-init .

RUN chmod +x /data/init.sh

VOLUME ["/data/record"]
ENTRYPOINT ["bash", "/data/init.sh"]
