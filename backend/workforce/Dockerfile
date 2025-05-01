FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/workforce-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.address=0.0.0.0 -jar app.jar"]

    