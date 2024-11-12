FROM openjdk:21
WORKDIR /app
COPY /target/telegram-tt-0.0.1-SNAPSHOT.jar telegram.jar
ENTRYPOINT ["java", "-jar", "telegram.jar"]