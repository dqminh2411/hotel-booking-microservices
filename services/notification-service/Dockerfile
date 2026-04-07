# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -q

COPY src src

RUN ./mvnw package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/notification-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 5000

CMD ["java", "-jar", "app.jar"]
