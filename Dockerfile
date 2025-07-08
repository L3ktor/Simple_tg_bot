FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/telegrambot-1.0.jar app.jar
CMD ["java", "-jar", "app.jar"]