FROM minio/minio:RELEASE.2025-04-22T22-12-26Z

WORKDIR /minio
COPY docker/config/img-alist-builder/minio .

RUN chmod -R +x /minio/*.sh

VOLUME ["/data", "/root/.minio"]

EXPOSE 9000
EXPOSE 9999

ENTRYPOINT ["bash", "/minio/startup.sh"]
