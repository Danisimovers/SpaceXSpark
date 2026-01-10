# syntax=docker/dockerfile:1

# Используем официальный OpenJDK 21 slim
FROM eclipse-temurin:21-jdk-jammy

# Создаем рабочую директорию
WORKDIR /app

# Копируем fat JAR
COPY target/SpaceXSpark-fat.jar app.jar

# Копируем конфиги, если нужны (например log4j2.xml)
# COPY src/main/resources/log4j2.xml .

# Expose порта Spark UI (если нужно)
EXPOSE 4040

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
