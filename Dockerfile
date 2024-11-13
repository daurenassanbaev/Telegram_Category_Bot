FROM openjdk:17
WORKDIR /app
COPY /target/telegram-tt-0.0.1-SNAPSHOT.jar telegram.jar
COPY image/img.png /app/img.png
COPY image/img_1.png /app/img_1.png
ENTRYPOINT ["java", "-jar", "telegram.jar"]