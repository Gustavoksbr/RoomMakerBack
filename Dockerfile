#FROM gradle:7.6.2-jdk17 AS build
FROM gradle/gradle@sha256:8b031c5d0a51c4a961d7f6c69788d75433285702d7d526e0b74070a2f470a1a1 AS build

WORKDIR /app

COPY gradlew .
COPY gradle/wrapper/ /app/gradle/wrapper/
COPY build.gradle settings.gradle /app/

RUN ./gradlew dependencies

COPY src /app/src

RUN ./gradlew bootJar --no-daemon

#FROM openjdk:17-jre-slim
FROM openjdk@sha256:1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]