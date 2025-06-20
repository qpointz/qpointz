ARG BASE_IMAGE

FROM $BASE_IMAGE

COPY --chown=1000:1000 build/test/ /test/
COPY --chown=1000:1000 application-moneta.yml /app/config/

ENTRYPOINT ["./bin/mill-service", "--spring.profiles.active=moneta"]

CMD []