ARG BASE_IMAGE

FROM $BASE_IMAGE

RUN echo "Hello world"

COPY --chown=1000:1000 ./build/install/samples/config/ /config/
COPY --chown=1000:1000 ./build/install/samples/etc/ /app/etc/
COPY --chown=1000:1000 ./build/install/samples/data/ /data/

RUN echo "data-dir" && \
    ls -R /data/moneta