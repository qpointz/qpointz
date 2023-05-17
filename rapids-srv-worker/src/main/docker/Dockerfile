FROM debian:11-slim

ENV GRAAL_VERSION="22.3.0"
ENV JAVA_VERSION="17"
ENV JAVA_HOME="/usr/lib/jvm/graalvm"
ENV PATH="/usr/lib/jvm/graalvm/bin:${PATH}"

# Install sbt
RUN \
  apt-get update && \
  apt-get install -y wget && \
  wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAAL_VERSION}/graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz && \
  tar -xvzf graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz && \
  mkdir -p /usr/lib/jvm && \
  mv graalvm-ce-java${JAVA_VERSION}-${GRAAL_VERSION}/ /usr/lib/jvm/graalvm && \
  rm graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz && \
  rm -rf /var/lib/apt/lists/*

COPY rapids-srv-worker/build/rapids-app/ /home/app/
COPY rapids-srv-worker/src/main/docker/application.yaml /home/app/etc/application.yaml

RUN \
   chmod +x /home/app/bin/rapids-srv-worker

EXPOSE 18200
EXPOSE 18220
EXPOSE 18300
EXPOSE 18080

WORKDIR /home/app

VOLUME /model

ENV RAPIDS_APPLICATION_CONFIG_ADDITIONAL_DIR="/config"

ENTRYPOINT ["./bin/rapids-srv-worker", "-"]
