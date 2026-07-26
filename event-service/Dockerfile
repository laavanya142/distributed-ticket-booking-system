FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/event-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
