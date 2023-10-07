FROM openjdk:11-jdk

ENV TRINO_VERSION 360

RUN apt update && \
    apt upgrade -y && \
    rm -rf /var/lib/apt/lists/* && \
    wget -O trino-server.tar.gz https://repo1.maven.org/maven2/io/trino/trino-server/${TRINO_VERSION}/trino-server-${TRINO_VERSION}.tar.gz && \
    tar xzf trino-server.tar.gz && \
    rm trino-server.tar.gz && \
    mv trino-server-360 /trino-server

ADD etc /trino-server/etc

ENV S3_CREDENTIALS_ACCESS_KEY_ID=key
ENV S3_CREDENTIALS_ACCESS_SECRET_KEY=secret
ENV S3_ENDPOINT_URL=endpoint

EXPOSE 8080

ENTRYPOINT ["/trino-server/bin/launcher", "run"]

RUN ls /trino-server/bin