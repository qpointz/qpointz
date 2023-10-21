ARG BASE_IMAGE=base-ci:latest

FROM $BASE_IMAGE

#image build using graalvm ce binaries 
ENV GRAAL_CE_VERSION="21.0.1"
ENV JAVA_HOME="/usr/lib/jvm/graalvm"
ENV PATH="/usr/lib/jvm/graalvm/bin:${PATH}"

RUN \    
  wget https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-${GRAAL_CE_VERSION}/graalvm-community-jdk-${GRAAL_CE_VERSION}_linux-x64_bin.tar.gz -O graalvm.tar.gz && \
  mkdir -p ${JAVA_HOME} && \
  tar -C ${JAVA_HOME} -xvzf graalvm.tar.gz --strip-components=1 && \
  rm -Rf graalvm.tar.gz && \
  ls -lac ${JAVA_HOME}
 

# Define working directory
WORKDIR /root