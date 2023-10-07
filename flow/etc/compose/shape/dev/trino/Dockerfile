FROM azul/zulu-openjdk-debian:18

ENV TRINO_VERSION 393

RUN apt update && \
    apt upgrade -y && \
    apt install -y curl python python3 && \
    curl trino-server.tar.gz https://repo1.maven.org/maven2/io/trino/trino-server/${TRINO_VERSION}/trino-server-${TRINO_VERSION}.tar.gz \
    | tar xvz && \
    mv /trino-server-${TRINO_VERSION} /trino-server

COPY etc /trino-server/etc

RUN curl https://repo1.maven.org/maven2/io/trino/trino-cli/${TRINO_VERSION}/trino-cli-${TRINO_VERSION}-executable.jar -o /trino-server/bin/trino && \
         chmod +x /trino-server/bin/trino

ENV S3_CREDENTIALS_ACCESS_KEY_ID=key
ENV S3_CREDENTIALS_ACCESS_SECRET_KEY=secret
ENV S3_ENDPOINT_URL=endpoint
ENV TRINO_PASSWORD=qp

EXPOSE 8080
EXPOSE 8443

WORKDIR /trino-server

ENTRYPOINT ["./bin/launcher", "run"]