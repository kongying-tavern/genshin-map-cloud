#!/usr/bin/env bash

BASE_DIR=/data
DATA_DIR=${BASE_DIR}/record
LOCK_FILE=${DATA_DIR}/initialized.lock

# Initialization steps
function step_init_minio () {
  mc alias set minio http://minio.local:9000 "${MINIO_ROOT_USERNAME}" "${MINIO_ROOT_PASSWORD}"
  mc admin user svcacct add \
    --access-key "${MINIO_KEY}" \
    --secret-key "${MINIO_SECRET}" \
    minio "${MINIO_ROOT_USERNAME}"
}

function step_add_img2webp_trigger () {
  mc admin config set minio \
    notify_redis:1 \
    address="redis.local:6379" \
    format="access" \
    key="MINIO_BUCKET_NOTIFY:IMAGE" \
    password="" \
    queue_dir="" \
    queue_limit="0"
  mc admin service restart minio
  mc mb \
    --ignore-existing \
    --region "minio" \
    --with-versioning \
    minio/${MINIO_BUCKET_IMAGE}
  mc anonymous set public minio/${MINIO_BUCKET_IMAGE}
  mc event add minio/${MINIO_BUCKET_IMAGE} arn:minio:sqs::1:redis --suffix .jpg --event put
  mc event add minio/${MINIO_BUCKET_IMAGE} arn:minio:sqs::1:redis --suffix .jpeg --event put
  mc event add minio/${MINIO_BUCKET_IMAGE} arn:minio:sqs::1:redis --suffix .jfif --event put
  mc event add minio/${MINIO_BUCKET_IMAGE} arn:minio:sqs::1:redis --suffix .png --event put
}

# Main processes
function turn_off_history () {
  set +o history
}

function turn_on_history () {
  set -o history
}

function do_initialize () {
  step_init_minio
  step_add_img2webp_trigger
}

function post_initialize () {
  touch "${LOCK_FILE}"
}

# Executor
if [ ! -f "$LOCK_FILE" ]; then
  turn_off_history
  do_initialize
  turn_on_history

  post_initialize
fi
