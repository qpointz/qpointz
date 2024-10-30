FROM gitlab.qpointz.io/qpointz/dependency_proxy/containers/debian:11-slim

RUN \
  apt-get update && \  
  apt-get install -yqq wget zip gnupg protobuf-compiler python3-protobuf protoc-gen-go curl apt-transport-https && \
  apt-get clean autoclean && \
  apt-get autoremove --yes && \
  rm -rf /var/lib/{apt,dpkg,cache,log}
