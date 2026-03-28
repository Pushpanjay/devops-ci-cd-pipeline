FROM openjdk:17-jdk-slim
COPY target/demo.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
