# build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/*.jar /app/app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]