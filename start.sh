#!/bin/sh

if [ -n "$SPRING_DATASOURCE_URL" ] && echo "$SPRING_DATASOURCE_URL" | grep -q "^postgresql://"; then
    SPRING_DATASOURCE_URL="jdbc:$SPRING_DATASOURCE_URL"
    export SPRING_DATASOURCE_URL
fi

exec java -jar app.jar --spring.profiles.active=prod
