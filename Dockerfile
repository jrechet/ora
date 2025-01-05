FROM amazoncorretto:11-alpine AS builder
WORKDIR /app

# Install dependencies
RUN apk add --no-cache bash

# Copy only gradle files first
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle gradle.properties ./
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY . .
RUN chmod +x ./gradlew

# Build
RUN ./gradlew assemble --no-daemon

# Final stage
FROM amazoncorretto:11-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]