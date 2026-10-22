FROM azul/zulu-openjdk-alpine:23

WORKDIR /app

COPY target/adventOfSpring-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]