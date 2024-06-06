FROM debian:11-slim as graalvm-jdk

ENV GRAAL_CE_VERSION="21.0.1"
ENV JAVA_HOME="/usr/lib/jvm/graalvm"
ENV PATH="/usr/lib/jvm/graalvm/bin:${PATH}"

RUN \
  apt-get update && \
  apt-get install -y binutils wget && \
  wget https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-${GRAAL_CE_VERSION}/graalvm-community-jdk-${GRAAL_CE_VERSION}_linux-x64_bin.tar.gz -O graalvm.tar.gz && \
  mkdir -p ${JAVA_HOME} && \
  tar -C ${JAVA_HOME} -xvzf graalvm.tar.gz --strip-components=1 && \
  $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

FROM debian:11-slim
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# copy JRE from the base image
COPY --from=graalvm-jdk --chown=1000:1000  /customjre $JAVA_HOME

#end of base image

ARG APPLICATION_USER=appuser
RUN adduser --no-create-home --uid 1000 $APPLICATION_USER

# Configure working directory
RUN mkdir /app && \
    chown -R $APPLICATION_USER /app && \
    mkdir /config && \
    chown -R $APPLICATION_USER /config && \
    mkdir /data && \
    chown -R $APPLICATION_USER /data

USER 1000

EXPOSE 9099
EXPOSE 8080

COPY --chown=1000:1000 ./build/install/delta-service-calcite/ /app/
COPY --chown=1000:1000 ./src/main/resources/application.yaml /config/application.yaml

RUN ln -s /config /app/config

WORKDIR /app

ENTRYPOINT ["./bin/delta-service-calcite"]

CMD [""]


