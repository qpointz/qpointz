FROM gitlab.qpointz.io/qpointz/dependency_proxy/containers/golang:latest

ENV TEST='TestPass1234'

RUN git clone https://github.com/jsha/minica.git && \
    cd minica && \
    go install && \
    mkdir -p /certs && \
    cd /certs && \
    minica -ca-alg RSA -ca-cert ca.pem -ca-key ca.key -domains '*.local' && \
    mkdir -p .certs/test && \
    openssl req -x509 -out .certs/test/test.pem -keyout .certs/test/test.key -newkey rsa:2048 -nodes -sha256 -subj '/CN=TESTUSER' &&  \
    openssl pkcs12 -in .certs/test/test.pem -inkey .certs/test/test.key -export -out .certs/test/test.pfx -password pass:${TEST} -name 'test certificate' -certfile ca.pem
    
VOLUME /certs

WORKDIR /certs
 