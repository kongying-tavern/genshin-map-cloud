# Builder
FROM maven:3-eclipse-temurin-11 AS builder

WORKDIR /data
COPY genshin-map-commons genshin-map-commons
COPY genshin-map-config genshin-map-config
COPY genshin-map-dependencies genshin-map-dependencies
COPY genshin-map-data genshin-map-data
COPY genshin-map-generator genshin-map-generator
COPY genshin-map-ability genshin-map-ability
COPY genshin-map-api genshin-map-api
COPY pom.xml pom.xml
COPY docker/config/apt/debian-bookworm.list /etc/apt/sources.list
COPY docker/config/maven docker/config
COPY docker/cache docker/cache

RUN --mount=type=cache,target=/root/.m2,rw \
    cp -f ./docker/cache/application-datasource.yml ./genshin-map-config/src/main/resources-dev/application-datasource-dev.yml && \
    cp -f ./docker/cache/application-image.yml ./genshin-map-config/src/main/resources-dev/application-image-dev.yml && \
    cp -f ./docker/cache/application-nacos.yml ./genshin-map-config/src/main/resources-dev/bootstrap-nacos-dev.yml && \
    cp -f ./docker/cache/application-nacos.yml ./genshin-map-ability/genshin-map-ability-gateway/src/main/resources-dev/bootstrap-nacos-dev.yml && \
    cp -f ./docker/cache/application-nacos.yml ./genshin-map-api/genshin-map-api-core/genshin-map-api-core-core/src/main/resources-dev/bootstrap-nacos-dev.yml && \
    mvn clean package -s ./docker/config/maven.xml -P dev -f pom.xml && \
    mkdir -p ./dist && \
    cp -f ./genshin-map-ability/genshin-map-ability-gateway/target/genshin-map-ability-gateway-1.0.jar ./dist && \
    cp -f ./genshin-map-api/genshin-map-api-core/genshin-map-api-core-core/target/genshin-map-api-core-core-1.0.jar ./dist

# API
FROM openjdk:11 AS api

WORKDIR /data
COPY --from=builder /data/dist .
COPY docker/config/apt/debian-bookworm.list /etc/apt/sources.list
COPY docker/config/api/startup.sh startup.sh
COPY docker/config/api/api-gateway.service /etc/systemd/system/genshin-map-ability-gateway.service
COPY docker/config/api/api-core.service /etc/systemd/system/genshin-map-api-core.service

RUN ln -s /usr/local/openjdk-11/bin/java /bin/java && \
    chmod +x /data/startup.sh && \
    chmod +x /etc/systemd/system/genshin-map-ability-gateway.service && \
    chmod +x /etc/systemd/system/genshin-map-api-core.service && \
    apt-get update && \
    apt-get install -y systemctl

VOLUME ["/data/logs"]
EXPOSE 8101

ENTRYPOINT ["/bin/sh", "/data/startup.sh"]
