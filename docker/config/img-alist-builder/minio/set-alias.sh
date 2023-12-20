#!/usr/bin/env bash

set +o history
/usr/bin/mc alias set minio http://localhost:9000 "${MINIO_ROOT_USER}" "${MINIO_ROOT_PASSWORD}"
set -o history
