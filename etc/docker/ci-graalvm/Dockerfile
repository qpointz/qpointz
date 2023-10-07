FROM debian:11-slim

ENV GRAAL_VERSION="22.3.1"
ENV JAVA_VERSION="17"
ENV JAVA_HOME="/usr/lib/jvm/graalvm"
ENV PATH="/usr/lib/jvm/graalvm/bin:${PATH}"


RUN \
  apt-get update && \
  apt-get install -y wget zip gnupg && \
  wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAAL_VERSION}/graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz && \
  tar -xvzf graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz && \
  mkdir -p /usr/lib/jvm && \
  mv graalvm-ce-java${JAVA_VERSION}-${GRAAL_VERSION}/ /usr/lib/jvm/graalvm && \
  rm graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz && \
  apt-get clean autoclean && \
  apt-get autoremove --yes && \
  rm -rf /var/lib/{apt,dpkg,cache,log}/ 

  
##  && \
##  wget https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O gradle.zip && \
##  unzip -d /opt/gradle gradle.zip

##VOLUME /data

# Define working directory
WORKDIR /root