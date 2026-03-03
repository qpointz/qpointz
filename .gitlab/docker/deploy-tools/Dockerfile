FROM debian:bookworm-slim

ARG TERRAFORM_VERSION=1.13.2
ARG HELM_VERSION=v3.19.0

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates curl jq unzip gnupg; \
    curl -sL https://aka.ms/InstallAzureCLIDeb | bash; \
    curl -fsSL -o /tmp/terraform.zip "https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip"; \
    unzip /tmp/terraform.zip -d /usr/local/bin; \
    chmod 0755 /usr/local/bin/terraform; \
    rm -f /tmp/terraform.zip; \
    curl -fsSL -o /tmp/helm.tar.gz "https://get.helm.sh/helm-${HELM_VERSION}-linux-amd64.tar.gz"; \
    tar -xzf /tmp/helm.tar.gz -C /tmp; \
    install -m 0755 /tmp/linux-amd64/helm /usr/local/bin/helm; \
    rm -rf /tmp/helm.tar.gz /tmp/linux-amd64; \
    curl -fsSL -o /usr/local/bin/kubectl "https://dl.k8s.io/release/$(curl -fsSL https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"; \
    chmod 0755 /usr/local/bin/kubectl; \
    apt-get purge -y --auto-remove gnupg; \
    rm -rf /var/lib/apt/lists/*
