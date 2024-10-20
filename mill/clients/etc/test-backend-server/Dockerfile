ARG BASE_IMAGE
FROM $BASE_IMAGE

ARG AZ_TID

RUN mkdir -p /app/config/ssl 

ENV AZURE_TEST_TENANT_ID=$AZ_TID

COPY --chown=1000:1000 application-auth.yml /app/config
COPY --chown=1000:1000 application-tls.yml /app/config
COPY --chown=1000:1000 application.yml /app/config
COPY --chown=1000:1000 passwd.yaml /app/config
COPY --chown=1000:1000 testdata.sql /app/config
COPY --chown=1000:1000 server.crt /app/config/ssl
COPY --chown=1000:1000 server.key /app/config/ssl