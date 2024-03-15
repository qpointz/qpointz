FROM debian:11-slim as graalvm-jdk

ENV GRAAL_CE_VERSION="21.0.1"
ENV JAVA_HOME="/usr/lib/jvm/graalvm"
ENV PATH="/usr/lib/jvm/graalvm/bin:${PATH}"

RUN \
  apt-get update && \
  apt install -y wget binutils  && \
  wget https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-${GRAAL_CE_VERSION}/graalvm-community-jdk-${GRAAL_CE_VERSION}_linux-x64_bin.tar.gz -O graalvm.tar.gz && \
  mkdir -p ${JAVA_HOME} && \
  tar -C ${JAVA_HOME} -xvzf graalvm.tar.gz --strip-components=1 && \
  rm -Rf graalvm.tar.gz

RUN \
  $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

## main app image
FROM debian:11-slim
ENV JAVA_HOME="/usr/lib/jvm/graalvm"
ENV PATH="$JAVA_HOME:$JAVA_HOME/bin:$PATH"
COPY --from=graalvm-jdk /customjre $JAVA_HOME
COPY build/install/rapids-grpc-service/ /home/app/

RUN du -h $JAVA_HOME --max-depth=0

WORKDIR /home/app
ENV RAPIDS_APPLICATION_CONFIG_ADDITIONAL_DIR="/config"
ENTRYPOINT ["./bin/rapids-grpc-service", "-"]


