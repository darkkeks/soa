FROM openjdk:11-jdk-slim AS build
WORKDIR /build/

# Run gradlew with only wrapper files to cache binary
COPY gradlew /build/
COPY gradle /build/gradle
RUN ./gradlew --version

COPY . /build
RUN ./gradlew 3-voice-chat:serverJar -x test --no-daemon

FROM openjdk:11-jdk-slim
RUN mkdir /app

# expecting a single jar
COPY --from=build /build/3-voice-chat/build/libs/3-voice-chat-server.jar /app/app.jar

ENTRYPOINT ["java", "-Xmx512M", "-jar", "/app/app.jar"]

