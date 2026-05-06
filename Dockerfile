# ---- Build ----
FROM gradle:7.6.2-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew bootJar --no-daemon


# ---- Runtime ----
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
