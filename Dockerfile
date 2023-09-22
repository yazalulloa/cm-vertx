FROM maven:3.9.1-amazoncorretto-20-debian AS build

# bun tailwindcss -i ./webroot/styles.css -o ./webroot/output.css --watch

WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,id=s/9ca38fb7-806e-466c-8394-ebcc2ba25746-/root/.m2 cd /app && mvn clean package -Dmaven.test.skip  && rm -r target

COPY src ./src
RUN --mount=type=cache,id=s/9ca38fb7-806e-466c-8394-ebcc2ba25746-/root/.m2 cd /app && mvn package -Dmaven.test.skip

FROM eclipse-temurin:20.0.1_9-jre-alpine
# FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/cm-vertx.jar .
COPY --from=build /app/target/libs/* ./libs/
COPY webapp ./webapp
COPY webroot ./webroot

ENTRYPOINT ["java", "--enable-preview" ,"-jar","cm-vertx.jar"]
