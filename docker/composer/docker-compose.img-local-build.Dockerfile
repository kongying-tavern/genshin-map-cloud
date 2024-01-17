FROM php:7.4-apache-bullseye

WORKDIR /var/www/html
ADD docker/config/apt/debian-bullseye.list /etc/apt/sources.list
ADD docker/config/img-local-uploader .

RUN mkdir -p ./cache && \
    mkdir -p ./saved_img && \
    chown -R www-data:www-data . && \
    mkdir -p /data && \
    mv -f ./startup.sh /data/startup.sh && \
    chown -R www-data:www-data /data && \
    chmod +x /data/startup.sh && \
    apt-get update && \
    apt-get install -y \
        libwebp-dev \
        libjpeg-dev \
        libpng-dev \
        libxpm-dev \
        libfreetype6-dev && \
    docker-php-ext-configure gd \
        --enable-gd \
        --with-webp \
        --with-jpeg \
        --with-xpm \
        --with-freetype && \
    docker-php-ext-install gd

VOLUME ["/var/www/html/cache", "/var/www/html/saved_img"]
EXPOSE 80

ENTRYPOINT ["bash", "/data/startup.sh"]
