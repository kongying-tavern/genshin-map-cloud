FROM minio/minio

WORKDIR /minio
ADD docker/config/img-alist-builder/minio .

RUN chmod -R +x /minio/*.sh

VOLUME ["/data", "/root/.minio"]

EXPOSE 9000
EXPOSE 9999

ENTRYPOINT ["bash", "/minio/startup.sh"]
