FROM alpine:3.19

RUN apk add --no-cache bash curl jq

WORKDIR /app
COPY run.sh .
COPY request.csv .

RUN chmod +x run.sh

CMD ["./run.sh"]