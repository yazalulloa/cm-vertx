FROM maven:3.9.1-amazoncorretto-20-debian AS build

WORKDIR /app
COPY pom.xml .
RUN mvn clean package -Dmaven.test.skip  && rm -r target

COPY src ./src
RUN mvn package -Dmaven.test.skip

FROM eclipse-temurin:20.0.1_9-jre-alpine
# FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/cm-vertx.jar .
COPY --from=build /app/target/libs/* ./libs/
COPY webapp ./webapp
COPY webroot ./webroot

ENTRYPOINT ["java", "--enable-preview" ,"-jar","cm-vertx.jar"]
