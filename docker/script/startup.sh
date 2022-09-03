#!/bin/bash
printenv
service genshin-map-ability-gateway start
service genshin-map-ability-auth start
service genshin-map-api-system start
service genshin-map-api-core start

while [[ true ]]; do
    sleep 30
done
