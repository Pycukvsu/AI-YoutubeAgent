FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache python3 py3-pip ffmpeg && \
    pip3 install --break-system-packages edge-tts pytrends

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
