#!/usr/bin/env bash

WEB_BASE=/var/www/html

# Deal folder permissions
mkdir -p "${WEB_BASE}/cache/"
mkdir -p "${WEB_BASE}/saved_img/"
chown -R www-data:www-data "${WEB_BASE}"
chmod -R 0777 "${WEB_BASE}"

# Start Apache daemon
apache2-foreground
