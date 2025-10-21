FROM amazoncorretto:21.0.3-alpine AS builder
COPY . /
RUN ./gradlew clean build

#### Run
FROM amazoncorretto:21.0.3-alpine

RUN apk update && apk upgrade --no-cache
# Add app user
ARG APPLICATION_USER=appuser
RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER
# Configure working directory
RUN mkdir /app && \
    chown -R $APPLICATION_USER /app
USER 1000
COPY --from=builder /build/libs/ms-accounts*.jar /app/app.jar
WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Xshare:off -jar app.jar"]