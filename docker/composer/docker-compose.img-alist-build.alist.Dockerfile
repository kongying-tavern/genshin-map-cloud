FROM xhofe/alist:v3.30.0

WORKDIR /alist
ADD docker/config/img-alist-builder/alist .

RUN chmod -R +x /alist/*.sh

VOLUME ["/alist/data"]
EXPOSE 5244
ENTRYPOINT ["bash", "/alist/startup.sh"]
