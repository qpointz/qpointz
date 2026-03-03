FROM golang:latest

ENV TEST='TestPass1234'

# /certs/srv/_.local/cert.pem + /certs/srv/_.local/key.pem -> server certificate/key (*.local)
# /certs/client/test.pem + /certs/client/test.key (+ test.pfx) -> client test certificate/key
# /certs/ca/ca.pem + /certs/ca/ca.key -> certificate authority (CA)
RUN git clone https://github.com/jsha/minica.git && \
    cd minica && \
    go install && \
    mkdir -p /certs/srv /certs/client /certs/ca && \
    cd /certs && \
    # Generate local CA and wildcard server cert/key for *.local (from minica).
    minica -ca-alg RSA -ca-cert ca.pem -ca-key ca.key -domains '*.local' && \
    mv ca.pem /certs/ca/ca.pem && \
    mv ca.key /certs/ca/ca.key && \
    mv _.local /certs/srv/_.local && \
    # Generate self-signed test user cert/key (client-style test identity).
    openssl req -x509 -out /certs/client/test.pem -keyout /certs/client/test.key -newkey rsa:2048 -nodes -sha256 -subj '/CN=TESTUSER' &&  \
    # Bundle test cert/key into PKCS#12 and include ca.pem as chain.
    openssl pkcs12 -in /certs/client/test.pem -inkey /certs/client/test.key -export -out /certs/client/test.pfx -password pass:${TEST} -name 'test certificate' -certfile /certs/ca/ca.pem
    
VOLUME /certs

WORKDIR /certs
 