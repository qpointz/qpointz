FROM openjdk:11-jdk

RUN \
    apt-get update && \
    apt-get install -y curl apt-transport-https && \
    curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
    curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - && \
    echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list && \
    apt-get update && \
    apt-get install -y nodejs yarn make g++ git openssh-client nano vim  && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /home/theia

ADD package.json ./package.json

RUN \
     npm install -g node-gyp && \
     mkdir -p /ws && \
     yarn --cache-folder ./ycache && rm -rf ./ycache && \
     NODE_OPTIONS="--max_old_space_size=4096" yarn theia build && \
     yarn theia download:plugins

EXPOSE 3000

ENV SHELL=/bin/bash

VOLUME /ws

ADD home /root/

RUN \
    echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update && \
    apt-get install -y sbt maven gradle  && \
    rm -rf /var/lib/apt/lists/*


ENTRYPOINT [ "yarn", "theia", "start", "/ws", "--hostname=0.0.0.0", "--plugins=local-dir:/home/theia/plugins" ]