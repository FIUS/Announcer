FROM gradle:jdk25 as builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src
RUN gradle build jar

FROM eclipse-temurin:25-jre
EXPOSE 9080

COPY --from=builder /home/gradle/src/build/libs/announcer.jar /

WORKDIR /
CMD java -jar /announcer.jar
