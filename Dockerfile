FROM openjdk:17-jdk-slim

COPY build/libs/ballog-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-Djdk.internal.platform.cgroup.enabled=false", "-jar", "app.jar"]
