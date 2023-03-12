FROM gradle:jdk11 as builder

WORKDIR /app
COPY . .

ENV EXCHANGE_PORT=8000
EXPOSE ${EXCHANGE_PORT}

CMD ./gradlew run --args="${EXCHANGE_PORT}"