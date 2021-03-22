FROM gradle:6.8.3-jdk11-hotspot as builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src
RUN gradle build

FROM adoptopenjdk:11-jre-hotspot
EXPOSE 9080

COPY --from=builder /home/gradle/src/build/libs/announcer.jar /
 
CMD java -jar /announcer.jar
