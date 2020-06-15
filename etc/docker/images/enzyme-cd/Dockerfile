
#
# Scala and sbt Dockerfile
#
# https://github.com/hseeberger/scala-sbt
#

# Pull base image
FROM openjdk:8u191

ENV ENZYME_LABVERSION=0.2.0

COPY enzyme-server-${ENZYME_VERSION}.tgz /root/enzyme-server-${ENZYME_VERSION}.tgz

RUN \
    cd /root && \
    tar -xzf enzyme-server-${ENZYME_VERSION}.tgz && \
    mkdir -p /root/enzyme && \
    mv /root/enzyme-server-${ENZYME_VERSION} /root/enzyme/srv && \
    rm enzyme-server-${ENZYME_VERSION}.tgz

WORKDIR /root/enzyme/srv

ENTRYPOINT ./bin/enzyme-server

EXPOSE 8080