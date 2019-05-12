FROM gradle:jdk8-alpine as builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:8-jdk-alpine
EXPOSE 9080

COPY --from=builder /home/gradle/src/build/libs/announcer.jar /
 
CMD java -jar /announcer.jar
