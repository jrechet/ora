FROM amazoncorretto:11-alpine

WORKDIR /app

# Copie du JAR de l'application
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]