FROM mcr.microsoft.com/powershell

WORKDIR /data
ADD docker/config/img-alist-builder/alist-init .

RUN chmod -R +x /data/*.ps1

VOLUME ["/data/record"]
ENTRYPOINT ["pwsh", "/data/init.ps1"]
