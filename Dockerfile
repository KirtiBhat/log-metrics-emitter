FROM adoptopenjdk/openjdk11:latest
COPY /build/libs/log-metrics-emitter-app-0.0.1-SNAPSHOT.jar log-metrics-emitter.jar
ENTRYPOINT ["java","-jar","/log-metrics-emitter.jar"]