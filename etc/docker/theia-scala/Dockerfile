FROM openjdk:11-jdk

# See : https://github.com/theia-ide/theia-apps/issues/34
### RUN adduser --disabled-password --gecos '' theia
### RUN chmod g+rw /home && \
###     mkdir -p /home/project && \
###     chown -R theia:theia /home/theia && \
###     chown -R theia:theia /home/project;

VOLUME /ws

WORKDIR /home/theia

### USER theia

ADD package.json ./package.json
ADD home /root/

ARG GITHUB_TOKEN

RUN \
    apt-get update && \
    apt-get install -y curl apt-transport-https && \
    curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
    curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - && \
    echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list && \
    echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update && \
    apt-get install -y nodejs yarn sbt maven gradle make g++ git openssh-client nano vim  && \
    npm install -g node-gyp && \
    rm -rf /var/lib/apt/lists/* && \
    sbt version && \
    mkdir -p /ws && \
    yarn --cache-folder ./ycache && rm -rf ./ycache && \
    NODE_OPTIONS="--max_old_space_size=4096" yarn theia build ; \
    yarn theia download:plugins

EXPOSE 3000
ENV SHELL=/bin/bash

ENTRYPOINT [ "yarn", "theia", "start", "/ws", "--hostname=0.0.0.0", "--plugins=local-dir:/home/theia/plugins" ]