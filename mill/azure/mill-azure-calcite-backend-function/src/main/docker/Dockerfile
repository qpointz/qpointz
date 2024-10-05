FROM azul/zulu-openjdk:17-latest

ARG APPLICATION_USER=appuser

RUN apt update && \
    apt install wget -y && \
    wget https://packages.microsoft.com/config/ubuntu/22.04/packages-microsoft-prod.deb && \
    dpkg -i packages-microsoft-prod.deb && \
    apt-get update && \
    apt-get install azure-functions-core-tools-4 -y && \
    apt-get clean autoclean && \
    apt-get autoremove --yes && \
    rm -rf /var/lib/{apt,dpkg,cache,log} && \
    adduser --no-create-home --uid 1000 $APPLICATION_USER

USER 1000

EXPOSE 7071

COPY --chown=1000:1000 ./build/azure-functions/mill-calcite-backend-func /app/

ENV DOTNET_SYSTEM_GLOBALIZATION_INVARIANT=1

WORKDIR /app

CMD ["func","host","start"]