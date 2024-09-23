ARG BASE_IMAGE=graalvm-ci:latest

FROM $BASE_IMAGE

ARG GRADLE_VERSION="8.9"
ENV PATH="/opt/gradle/gradle-${GRADLE_VERSION}/bin:${PATH}"
COPY settings.xml /root/.m2/settings.xml

RUN \
  wget https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O gradle.zip && \
  unzip -d /opt/gradle gradle.zip
