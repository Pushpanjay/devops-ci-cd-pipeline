FROM eclipse-temurin:17-jdk
COPY target/demo.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
