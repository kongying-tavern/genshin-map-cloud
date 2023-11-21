FROM php:7.4-apache

WORKDIR /var/www/html/
ADD docker/config/apt.list /etc/apt/sources.list

RUN apt update && \
    apt install -y \
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

VOLUME ["/var/www/html"]
EXPOSE 80
