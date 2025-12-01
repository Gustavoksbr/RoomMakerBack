# ---- Build ----
#FROM gradle:7.6.2-jdk17-alpine AS build

FROM gradle@sha256:8f5dbc642dbd1d6289a8692cfb47296d09939881f63637194ed73d328823d5de AS build

WORKDIR /app

COPY gradlew .
COPY gradle/wrapper/ /app/gradle/wrapper/
COPY build.gradle settings.gradle /app/

RUN ./gradlew dependencies

COPY src /app/src

RUN ./gradlew bootJar --no-daemon

# ---- Runtime ----

#FROM openjdk:17.0.1-jdk-slim AS runtime
 FROM openjdk@sha256:565d3643a78a657ca03e85c110af9579a07e833d6bcc14f475249c521b5c5d74 AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
