#!/usr/bin/env bash

MC=/usr/bin/mc

# Set MinIO client alias
set +o history
$MC alias set minio http://localhost:9000 "${MINIO_ROOT_USER}" "${MINIO_ROOT_PASSWORD}"
set -o history

# Health check
$MC ping minio --count 5 || exit 1

exit 0;
