# Builder
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

# API
FROM openjdk:11 AS api

WORKDIR /data
COPY --from=builder /data/dist .
ADD docker/config/apt.list /etc/apt/sources.list
ADD --chmod=+x docker/config/startup.sh startup.sh
ADD --chmod=+x docker/config/api-gateway.service /etc/systemd/system/genshin-map-ability-gateway.service
ADD --chmod=+x docker/config/api-core.service /etc/systemd/system/genshin-map-api-core.service

RUN ln -s /usr/local/openjdk-11/bin/java /bin/java && \
    apt update && \
    apt install -y systemctl

VOLUME ["/data"]
EXPOSE 8101

ENTRYPOINT ["/bin/sh", "/data/startup.sh"]
