FROM minio/mc:RELEASE.2024-01-05T05-04-32Z

WORKDIR /data
ADD docker/config/img-alist-builder/minio-init .

RUN chmod +x /data/init.sh

VOLUME ["/data/record"]
ENTRYPOINT ["bash", "/data/init.sh"]
