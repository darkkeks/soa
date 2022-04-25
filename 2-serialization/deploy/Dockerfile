FROM openjdk:11-jdk-slim AS build
WORKDIR /build/

# Run gradlew with only wrapper files to cache binary
COPY gradlew /build/
COPY gradle /build/gradle
RUN ./gradlew --version

COPY . /build
RUN ./gradlew 2-serialization:build -x test --no-daemon

FROM openjdk:11-jdk-slim
RUN mkdir /app

# expecting a single jar
COPY --from=build /build/2-serialization/build/libs/2-serialization-all.jar /app/app.jar

ENTRYPOINT ["java", "-Xmx512M", "-jar", "/app/app.jar"]

