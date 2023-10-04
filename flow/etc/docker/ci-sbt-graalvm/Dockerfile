FROM debian:11-slim

ENV GRAAL_VERSION="22.0.0.2"
ENV JAVA_VERSION="11"
ENV JAVA_HOME="/usr/lib/jvm/graalvm"
ENV PATH="/usr/lib/jvm/graalvm/bin:${PATH}"


COPY ivysettings-file.properties /root/.ivy2/ivysettings-file.properties
COPY settings.xml /root/.m2/settings.xml

# Install sbt
RUN \
  apt-get update && \
  apt-get install -y wget gnupg && \
  wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAAL_VERSION}/graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz && \
  tar -xvzf graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz && \
  mkdir -p /usr/lib/jvm && \
  mv graalvm-ce-java${JAVA_VERSION}-${GRAAL_VERSION}/ /usr/lib/jvm/graalvm && \
  gu install python  && \
  rm graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz && \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
  wget "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" -O key.gpg  && \
  apt-key add key.gpg && \
  rm key.gpg && \
  apt-get update && \
  apt-get install -y sbt xsltproc python3 python3-pip zlib1g-dev libjpeg-dev latexmk texlive-formats-extra && \
  sbt -Dsbt.rootdir=true version && \
  rm -rf /var/lib/apt/lists/* && \
  pip3 install -U requests rst2pdf sphinx sphinx_rtd_theme sphinx-material sphinx-tabs sphinx-press-theme && \
  sbt -Dsbt.rootdir=true version

VOLUME /data

# Define working directory
WORKDIR /root