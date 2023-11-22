FROM debian:11-slim

RUN \
  apt-get update && \
  apt-get install -y wget zip gnupg protobuf-compiler python3-protobuf protoc-gen-go && \  
  apt-get clean autoclean && \
  apt-get autoremove --yes && \
  rm -rf /var/lib/{apt,dpkg,cache,log}
