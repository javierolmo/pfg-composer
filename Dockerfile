# Install pfg-core and package pfg-composer
FROM maven:3.5-jdk-8-alpine
WORKDIR /app
COPY maven-settings.xml /root/.m2/settings.xml
COPY ./src /app/pfg-composer/src
COPY ./pom.xml /app/pfg-composer/pom.xml
RUN cd /app/pfg-composer && mvn package -DskipTests

# Run pfg-composer
FROM openjdk:8-jre AS pfg-composer
RUN apt-get update && apt-get install -y musescore
ENV QT_QPA_PLATFORM=offscreen
WORKDIR /app
COPY --from=0 /app/pfg-composer/target/pfg-composer.jar /app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]
