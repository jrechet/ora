# ORA CODEBASE REFERENCE

## Build Commands
- `./gradlew assemble` - Build the application
- `./gradlew bootJar` - Create executable JAR
- `./gradlew bootRun -Dgrails.env=prod -Dserver.port=8080 -Dora.config.path=./grails-app/conf/ora` - Run the application

## Test Commands
- `./gradlew test` - Run all tests
- `./gradlew test --tests "ora.monitoring.MonitoringServiceSpec"` - Run specific test class
- `./gradlew integrationTest` - Run integration tests

## Simulator Commands
- `cd simulator && npm start` - Run simulator server
- `docker-compose up` - Run application with simulator in Docker

## Database Commands
- `./gradlew startDevDb` - Start PostgreSQL in Docker
- `./gradlew stopDevDb` - Stop PostgreSQL
- `./gradlew resetDevDb` - Reset PostgreSQL

## Code Style
- Follow Grails/Groovy conventions
- Use camelCase for variables/methods, PascalCase for classes
- Group imports by package
- Use service pattern for business logic
- Include type definitions for method parameters and returns
- Use try/catch for handling expected exceptions
- Follow RESTful patterns in controllers