FROM gradle:jdk25 as builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src
RUN gradle build jar

FROM eclipse-temurin:25-jre
EXPOSE 9080
RUN mkdir -p /app && \
    useradd -U -d /app -s /bin/bash announcer && \
    chown announcer:announcer /app

COPY --from=builder /home/gradle/src/build/libs/announcer.jar /

WORKDIR /app
USER announcer
CMD java -jar /announcer.jar
