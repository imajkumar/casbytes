# CasBytes Core Service — production-oriented container image
FROM eclipse-temurin:21-jre-alpine AS runtime

ARG APP_USER=casbytes
RUN addgroup -S ${APP_USER} && adduser -S ${APP_USER} -G ${APP_USER}

WORKDIR /app

COPY target/casbytes-core-service-*.jar /app/app.jar

USER ${APP_USER}

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

EXPOSE 8080 8081

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]
