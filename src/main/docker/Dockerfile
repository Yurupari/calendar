FROM eclipse-temurin:25-jdk-alpine

ADD build/libs/calendar.jar /
ENTRYPOINT ["java","-server","-Djava.security.egd=file:/dev/./urandom","-jar","/calendar.jar"]