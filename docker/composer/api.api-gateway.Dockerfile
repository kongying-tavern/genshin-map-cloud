# API
FROM eclipse-temurin:11-jre AS api

WORKDIR /data

ARG JAR_FILE=genshin-map-ability-gateway-1.0.jar

COPY --from=api-builder /data/dist/${JAR_FILE} ./api-gateway.jar

RUN ln -s /usr/local/openjdk-11/bin/java /bin/java

VOLUME ["/data/logs"]
EXPOSE 8101

ENTRYPOINT ["java", "-server", "-Dfile.encoding=UTF-8", "-jar", "/data/api-gateway.jar"]
