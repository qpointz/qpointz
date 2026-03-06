FROM azul/zulu-openjdk:21-latest AS openjdk

ARG APP_EDITION="basic"

RUN apt update && \
    apt install binutils -y

RUN --mount=target=/mnt \
        set -eux; \
        modules_file=/mnt/build/jlink-modules/${APP_EDITION}/jlink-modules.txt; \
        modules="ALL-MODULE-PATH"; \
        if [ -f $modules_file ]; then \
           echo "modules file exists"; \
           modules=$(cat $modules_file);\
        else \
           echo "modules missing. fall back to ALL-MODULE-PATH"; \
        fi ; \
        echo "Selected modules $modules"; \
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

ARG APP_EDITION=basic
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

COPY --chown=1000:1000 ./build/install/mill-service-boot-${APP_EDITION}/ /app/

RUN ln -s /config /app/config

ENV LOADER_PATH=/extras

VOLUME /config

VOLUME /data

VOLUME /extras

WORKDIR /app

ENTRYPOINT ["./bin/mill-service"]