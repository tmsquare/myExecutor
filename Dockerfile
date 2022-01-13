#
# Package stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src/
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests

#
# Build stage
#
FROM openjdk:11-slim
RUN apt-get update && \
    apt-get install net-tools && \
    apt-get install -y netcat
EXPOSE 8080
COPY --from=build /home/app/target/executor-2.0.jar /usr/local/executor.jar
COPY --from=build /home/app/target/executor-2.0-tests.jar /usr/local/executor-tests.jar
COPY --from=build /home/app/target/lib/* /usr/local/lib
ENV HW_HOME=/usr/local/
WORKDIR $HW_HOME