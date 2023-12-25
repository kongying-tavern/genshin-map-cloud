#!/usr/bin/env bash

PING_STATUS=$(wget -S -O - http://localhost:5244/ping 2>&1 | grep 'HTTP/' | awk '{print $2}')

if [[ "${PING_STATUS}" != "200" ]]; then
  exit 1;
fi

exit 0;
