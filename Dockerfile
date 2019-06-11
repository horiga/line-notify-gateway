FROM openjdk:8-jdk-alpine
LABEL maintainer="Nont Banditwong <nontster@gmail.com>"

# Set volume point to /tmp
VOLUME /tmp

# Make port 18081/tcp and 28081/tcp available to the world outside container
EXPOSE 18081/tcp
EXPOSE 28081/tcp

# Add the service itself
ARG JAR_FILE
COPY target/${JAR_FILE} /app.jar

RUN apk --update add tzdata
ENV TZ=Asia/Bangkok
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV JAVA_OPTS=""
ENTRYPOINT ["java", "-jar", "/app.jar" ]