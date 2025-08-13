ARG BASE_IMAGE
FROM $BASE_IMAGE

ARG AZ_TID

ENV AZURE_TEST_TENANT_ID=$AZ_TID

COPY --chown=1000:1000 application-auth.yml /app/config
COPY --chown=1000:1000 ./ssl/server.key /app/config/ssl/server.key
COPY --chown=1000:1000 ./ssl/server.crt /app/config/ssl/server.crt