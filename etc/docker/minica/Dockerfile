FROM golang:latest

RUN git clone https://github.com/jsha/minica.git && \
    cd minica && \
    go install && \
    mkdir -p /certs

RUN cd /certs && \
    minica -ca-alg RSA -ca-cert ca.pem -ca-key ca.key -domains '*.local' && \
    ls /certs

VOLUME /certs

WORKDIR /certs
 