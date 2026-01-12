
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/SpaceXSpark-fat.jar app.jar


EXPOSE 4040

ENTRYPOINT ["java", "-jar", "app.jar"]
