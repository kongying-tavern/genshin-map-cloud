# Builder
FROM maven:3-eclipse-temurin-11 AS builder

WORKDIR /data

ARG BUILD_PROFILE

COPY genshin-map-commons genshin-map-commons
COPY genshin-map-config genshin-map-config
COPY genshin-map-dependencies genshin-map-dependencies
COPY genshin-map-data genshin-map-data
COPY genshin-map-generator genshin-map-generator
COPY genshin-map-ability genshin-map-ability
COPY genshin-map-api genshin-map-api
COPY pom.xml pom.xml
COPY docker/config/maven docker/config
COPY docker/cache docker/cache

RUN --mount=type=cache,target=/root/.m2,rw \
    cp -f ./docker/cache/application-datasource.yml ./genshin-map-config/src/main/resources-${BUILD_PROFILE}/application-datasource-${BUILD_PROFILE}.yml && \
    cp -f ./docker/cache/application-image.yml ./genshin-map-config/src/main/resources-${BUILD_PROFILE}/application-image-${BUILD_PROFILE}.yml && \
    cp -f ./docker/cache/application-websocket.yml ./genshin-map-config/src/main/resources-${BUILD_PROFILE}/application-websocket-${BUILD_PROFILE}.yml && \
    cp -f ./docker/cache/application-oauth.yml ./genshin-map-config/src/main/resources-${BUILD_PROFILE}/application-oauth-${BUILD_PROFILE}.yml && \
    cp -f ./docker/cache/application-nacos.yml ./genshin-map-config/src/main/resources-${BUILD_PROFILE}/bootstrap-nacos-${BUILD_PROFILE}.yml && \
    cp -f ./docker/cache/application-nacos.yml ./genshin-map-ability/genshin-map-ability-gateway/src/main/resources-${BUILD_PROFILE}/bootstrap-nacos-${BUILD_PROFILE}.yml && \
    cp -f ./docker/cache/application-nacos.yml ./genshin-map-api/genshin-map-api-core/genshin-map-api-core-core/src/main/resources-${BUILD_PROFILE}/bootstrap-nacos-${BUILD_PROFILE}.yml && \
    mvn clean package -s ./docker/config/maven.xml -P ${BUILD_PROFILE} -f pom.xml && \
    mkdir -p ./dist && \
    cp -f ./genshin-map-ability/genshin-map-ability-gateway/target/genshin-map-ability-gateway-1.0.jar ./dist && \
    cp -f ./genshin-map-api/genshin-map-api-core/genshin-map-api-core-core/target/genshin-map-api-core-core-1.0.jar ./dist
