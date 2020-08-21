FROM openjdk:8-jdk-alpine

RUN mkdir -p /opt && \
    cd /opt && \
    wget http://apache.mirrors.hoobly.com/drill/drill-1.17.0/apache-drill-1.17.0.tar.gz && \
    tar zxvf apache-drill-1.17.0.tar.gz && \
    mv apache-drill-1.17.0 apache-drill && \
    rm apache-drill-1.17.0.tar.gz && \
    apk add --no-cache bash

EXPOSE 8047

EXPOSE 31010

EXPOSE 31011

EXPOSE 31012

VOLUME /data

WORKDIR "/opt/apache-drill"

ENTRYPOINT ["/bin/bash", "-c", "/opt/apache-drill/bin/drill-embedded"]