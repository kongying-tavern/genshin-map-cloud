FROM maven:3-eclipse-temurin-11 AS builder

WORKDIR /data

ADD genshin-map-commons genshin-map-commons
ADD genshin-map-config genshin-map-config
ADD genshin-map-dependencies genshin-map-dependencies
ADD genshin-map-data genshin-map-data
ADD genshin-map-generator genshin-map-generator
ADD genshin-map-ability genshin-map-ability
ADD genshin-map-api genshin-map-api
ADD pom.xml pom.xml
ADD docker/config docker/config
ADD docker/cache docker/cache

RUN --mount=type=cache,target=/root/.m2,rw \
    cp -f ./docker/cache/application.yml ./genshin-map-config/src/main/resources-dev/application-datasource-dev.yml && \
    mvn clean package -s ./docker/config/maven.xml -P dev -f pom.xml && \
    mkdir -p ./dist && \
    cp ./genshin-map-ability/genshin-map-ability-gateway/target/genshin-map-ability-gateway-1.0.jar ./dist && \
    cp ./genshin-map-api/genshin-map-api-core/genshin-map-api-core-core/target/genshin-map-api-core-core-1.0.jar ./dist
