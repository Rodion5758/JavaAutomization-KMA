FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon shadowJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/Hw1-all.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
