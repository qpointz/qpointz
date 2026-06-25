FROM azul/zulu-openjdk:25-latest AS openjdk

RUN apt update && \
    apt install binutils -y

RUN --mount=target=/mnt \
    set -eux; \
    modules_file=/mnt/build/jlink-modules/jlink-modules.txt; \
    if [ ! -f "${modules_file}" ]; then \
      echo "ERROR: ${modules_file} not found. Run jlink-modules generation before docker build."; \
      exit 1; \
    fi; \
    modules="$(cat "${modules_file}")"; \
    echo "Selected modules ${modules}"; \
    "$JAVA_HOME"/bin/jlink \
      --add-modules "${modules}" \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress=2 \
      --output /customjre

FROM debian:trixie-slim
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# copy JRE from the base image
COPY --from=openjdk --chown=1000:1000  /customjre $JAVA_HOME

#end of base image

ARG APPLICATION_USER=appuser
RUN useradd -u 1000 -M -s /usr/sbin/nologin $APPLICATION_USER

# Configure working directory
RUN mkdir -p /app/config && \
    chown -R $APPLICATION_USER /app && \
    mkdir -p /data && \
    chown -R $APPLICATION_USER /data

USER 1000

EXPOSE 9090
EXPOSE 8080

COPY --chown=1000:1000 ./build/install/mill-jdbc-shell/ /app/

WORKDIR /app

ENTRYPOINT ["./bin/mill-jdbc-shell"]

CMD [""]


