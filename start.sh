#!/bin/sh

if [ -n "$SPRING_DATASOURCE_URL" ] && echo "$SPRING_DATASOURCE_URL" | grep -q "^postgresql://"; then
    URL="$SPRING_DATASOURCE_URL"
    URL="${URL#postgresql://}"

    USER=$(echo "$URL" | cut -d: -f1)
    REST=$(echo "$URL" | cut -d: -f2-)

    PASS=$(echo "$REST" | cut -d@ -f1)
    HOST_DB=$(echo "$REST" | cut -d@ -f2-)

    HOST=$(echo "$HOST_DB" | cut -d/ -f1)
    DB=$(echo "$HOST_DB" | cut -d/ -f2)

    JDBC_URL="jdbc:postgresql://${HOST}/${DB}?user=${USER}&password=${PASS}"
    export SPRING_DATASOURCE_URL="$JDBC_URL"
fi

exec java -jar app.jar --spring.profiles.active=prod
