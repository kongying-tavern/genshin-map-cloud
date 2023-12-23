#!/bin/bash
systemctl daemon-reload

systemctl restart genshin-map-ability-gateway
systemctl restart genshin-map-api-core

while true; do
    sleep 1000
done
