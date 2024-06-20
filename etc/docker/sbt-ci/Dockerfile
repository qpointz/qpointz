ARG BASE_IMAGE=graalvm-ci:latest
FROM $BASE_IMAGE

ENV PATH="/usr/lib/jvm/graalvm/bin:${PATH}"
COPY ivysettings-file.properties /root/.ivy2/ivysettings-file.properties
COPY settings.xml /root/.m2/settings.xml

# Install sbt
RUN \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
  curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import && \
  chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg && \
  apt-get update && \
  apt-get install -yqq sbt && \
  sbt -Dsbt.rootdir=true version && \
  apt-get clean autoclean && \
  apt-get autoremove --yes && \
  rm -rf /var/lib/{apt,dpkg,cache,log}/ 

  
# xsltproc python3 python3-pip zlib1g-dev libjpeg-dev latexmk texlive-formats-extra && \
  #pip3 install -U requests rst2pdf sphinx sphinx_rtd_theme sphinx-material sphinx-tabs sphinx-press-theme && \  

# Define working directory
WORKDIR /root