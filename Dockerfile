
FROM openjdk:21

WORKDIR /app

COPY target/train-0.0.1.jar train-app.jar

EXPOSE 8080

CMD ["java", "-jar", "train-app.jar"]

