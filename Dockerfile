FROM eclipse-temurin:17-jdk

COPY ./data/. .

CMD ["java", "-jar", "srbot.jar"]
