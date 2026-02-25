ARG BASE_IMAGE

FROM $BASE_IMAGE

COPY --chown=1000:1000 ./src/main/docker/samples/application.yml /config/
COPY --chown=1000:1000 ./src/main/docker/samples/etc/skymill-flow.yml /config/
COPY --chown=1000:1000 ./src/main/docker/samples/data/ /data/

ENV SPRING_PROFILES_ACTIVE=skymill