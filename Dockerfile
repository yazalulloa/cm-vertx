FROM maven:3.9.1-amazoncorretto-20-debian AS build

# bun tailwindcss -i ./webroot/styles.css -o ./webroot/output.css --watch

WORKDIR /app
COPY pom.xml .

RUN --mount=type=cache,id=s/601aca77-348a-4ab8-860b-d698334af0be-/root/.m2 mvn clean package -Dmaven.test.skip  && rm -r target
# RUN --mount=type=cache,id=601aca77-348a-4ab8-860b-d698334af0be-cm-vertx-cache,target=/root/.m2 mvn clean package -Dmaven.test.skip  && rm -r target

COPY src ./src
RUN --mount=type=cache,id=s/601aca77-348a-4ab8-860b-d698334af0be-/root/.m2 mvn package -Dmaven.test.skip
# RUN --mount=type=cache,id=601aca77-348a-4ab8-860b-d698334af0be-cm-vertx-cache,target=/root/.m2 mvn package -Dmaven.test.skip

FROM eclipse-temurin:20.0.1_9-jre-alpine
# FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/cm-vertx.jar .
COPY --from=build /app/target/libs/* ./libs/
COPY webapp ./webapp
COPY webroot ./webroot

ENTRYPOINT ["java", "--enable-preview" ,"-jar","cm-vertx.jar"]
