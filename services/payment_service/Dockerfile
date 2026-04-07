# Build stage
FROM maven:3.9-openjdk-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/payment_service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 5005

CMD ["java", "-jar", "app.jar"]