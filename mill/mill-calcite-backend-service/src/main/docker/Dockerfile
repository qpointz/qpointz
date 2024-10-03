FROM azul/zulu-openjdk:17-latest AS openjdk

RUN apt update && \
    apt install binutils -y && \
      $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre


FROM debian:12-slim
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# copy JRE from the base image
COPY --from=openjdk --chown=1000:1000  /customjre $JAVA_HOME

#end of base image

ARG APPLICATION_USER=appuser
RUN adduser --no-create-home --uid 1000 $APPLICATION_USER

# Configure working directory
RUN mkdir -p /app/config && \
    chown -R $APPLICATION_USER /app && \
    mkdir -p /data && \
    chown -R $APPLICATION_USER /data

USER 1000

EXPOSE 9099
EXPOSE 8080

COPY --chown=1000:1000 ./build/install/mill-calcite-backend-service-boot/ /app/

RUN ln -s /config /app/config

VOLUME /config
VOLUME /data

WORKDIR /app

ENTRYPOINT ["./bin/mill-calcite-backend-service"]

CMD [""]


