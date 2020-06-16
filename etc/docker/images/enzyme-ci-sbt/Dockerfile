#
# Scala and sbt Dockerfile
#
# https://github.com/hseeberger/scala-sbt
#

# Pull base image
FROM openjdk:11.0.6-jdk

# Env variables
ENV SCALA_VERSION 2.13.2
ENV SBT_VERSION 1.3.12

# Scala expects this file
# RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install -y sbt

RUN \
    mkdir -p /data/.sbt && \
    mkdir -p /data/.ivy2 && \
    ln -s /data/.ivy2 /root/.ivy2 && \
    ln -s /data/.sbt /root/.sbt

RUN \
   apt-get install -y xsltproc

VOLUME /data

# Define working directory
WORKDIR /root