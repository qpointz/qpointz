#
# Scala and sbt Dockerfile
#
# https://github.com/hseeberger/scala-sbt
#

# Pull base image
FROM openjdk:11-jdk

# Env variables
ENV SCALA_VERSION 2.13.3
ENV SBT_VERSION 1.3.13

# Scala expects this file
# RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install -y sbt xsltproc python3 python3-pip && \
  rm -rf /var/lib/apt/lists/*

RUN \
    mkdir -p /data/.sbt && \
    mkdir -p /data/.ivy2 && \
    ln -s /data/.ivy2 /root/.ivy2 && \
    ln -s /data/.sbt /root/.sbt

RUN \
   pip3 install -U requests rst2pdf sphinx sphinx_rtd_theme

VOLUME /data

# Define working directory
WORKDIR /root