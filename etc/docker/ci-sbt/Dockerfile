#
# Scala and sbt Dockerfile
#
# https://github.com/hseeberger/scala-sbt
#

# Pull base image
FROM openjdk:11-jdk

# Env variables
ENV SBT_VERSION 1.6.1

# Scala expects this file
# RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

# Install sbt
RUN \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
  curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
  apt-get update && \
  apt-get install -y sbt xsltproc python3 python3-pip zlib1g-dev libjpeg-dev latexmk texlive-formats-extra && \
  sbt -Dsbt.rootdir=true version && \
  rm -rf /var/lib/apt/lists/* && \
  mkdir -p /data/.sbt && \
  mkdir -p /data/.ivy2 && \
  ln -s /data/.ivy2 /root/.ivy2 && \
  ln -s /data/.sbt /root/.sbt && \
  pip3 install -U requests rst2pdf sphinx sphinx_rtd_theme sphinx-material sphinx-tabs

VOLUME /data

# Define working directory
WORKDIR /root