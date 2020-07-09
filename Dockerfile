FROM openjdk:8
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN apt update -y && apt install maven -y&& mvn clean install
CMD ["java", "-jar", "./target/search-coding-challenge-1.0-SNAPSHOT-shaded.jar", "server", "configuration.yml"]
