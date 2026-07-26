FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/analytics-service-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
