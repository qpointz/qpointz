#!/bin/sh 

CLIENT_ID=def
CLIENT_SERIAL=01

mkdir -p _.local
mkdir -p backend.local
rm -Rf _.local
rm -Rf backend.local 

docker run -v ./ssl:/certs minica minica -ca-alg RSA -ca-cert ca.pem -ca-key ca.key -domains 'backend.local'
docker run -v ./ssl:/certs minica minica -ca-alg RSA -ca-cert ca.pem -ca-key ca.key -domains '*.local'

sudo chown -R $USER:$USER ./

cat ./ssl/_.local/cert.pem ./ssl/ca.pem > ./ssl/_.local/cert-full.pem

sudo cp -f ./ssl/ca.pem /usr/local/share/ca-certificates/minica.crt
sudo update-ca-certificates --verbose 

mkdir -p ssl/client
rm -Rf ssl/client 
mkdir ssl/client

# rsa
#openssl genrsa -aes256 -passout pass:xxxx -out ssl/client/${CLIENT_ID}.pass.key 4096
#openssl rsa -passin pass:xxxx -in ssl/client/${CLIENT_ID}.pass.key -out ssl/client/${CLIENT_ID}.key
#rm ssl/client/${CLIENT_ID}.pass.key

#openssl req -new -key ssl/client/${CLIENT_ID}.key -out ssl/client/${CLIENT_ID}.csr
#openssl x509 -req -days 3650 -in ssl/client/${CLIENT_ID}.csr -CA ssl/ca.pem -CAkey ssl/ca.key -set_serial ${CLIENT_SERIAL} -out ssl/client/${CLIENT_ID}.pem

#cat ssl/client/${CLIENT_ID}.key ssl/client/${CLIENT_ID}.pem ssl/ca.pem > ssl/client/${CLIENT_ID}.full.pem

openssl req -x509 -out ssl/client/client.pem -keyout ssl/client/client.key \
  -newkey rsa:2048 -nodes -sha256 \
  -subj '/CN=TESTUSER'