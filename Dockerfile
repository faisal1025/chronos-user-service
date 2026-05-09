#---------------BUILD-----------------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /UserService
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle ./gradle
# 3️⃣ Give permission
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew build --no-daemon
#-------------RUN---------------------
FROM eclipse-temurin:17-jdk
WORKDIR /UserService
COPY --from=build /UserService/build/libs/*.jar userservice.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "userservice.jar"]
CMD ["--spring.profiles.active=prod"]
