FROM debian:bookworm-slim 

RUN apt-get update && \
    apt-get install -y gnupg software-properties-common wget apt-transport-https ca-certificates curl gnupg lsb-release jq && \
    wget -O- https://apt.releases.hashicorp.com/gpg | \
            gpg --dearmor | \
            tee /usr/share/keyrings/hashicorp-archive-keyring.gpg > /dev/null && \
    gpg --no-default-keyring --keyring /usr/share/keyrings/hashicorp-archive-keyring.gpg --fingerprint && \
    echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] \
            https://apt.releases.hashicorp.com $(lsb_release -cs) main" | \
            tee /etc/apt/sources.list.d/hashicorp.list && \ 
    curl -sL https://aka.ms/InstallAzureCLIDeb | bash && \
    apt install -y terraform && \ 
    curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 && \
    chmod 700 get_helm.sh && \
    ./get_helm.sh && \
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl && \
    apt-get clean autoclean && \
    apt-get autoremove --yes && \
    rm -rf /var/lib/{apt,dpkg,cache,log}
