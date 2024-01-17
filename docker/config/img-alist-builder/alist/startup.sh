#!/usr/bin/env bash

ALIST=/opt/alist/alist
ALIST_DIR=/opt/alist

chown -R ${PUID}:${PGID} "${ALIST_DIR}"
umask ${UMASK}

# Disable history temporary to avoid password leaks
set +o history

# Set password for admin
$ALIST admin set "${ADMIN_PASSWORD}"
# Cancel 2FA fpr admin
$ALIST cancel2fa

set -o history

exec su-exec ${PUID}:${PGID} ${ALIST} server --no-prefix "${ALIST_DIR}"
