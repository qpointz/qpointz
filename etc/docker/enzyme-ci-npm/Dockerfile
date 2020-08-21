#
# Scala and sbt Dockerfile
#
# https://github.com/hseeberger/scala-sbt
#

# Pull base image
FROM node:10.16.0

ENV ANGULAR_VERSION=8.1.2

RUN \
  echo >> /root/.bashrc && \
  echo 'export PATH=/data/.npm-global/bin:$PATH'>>/root/.bashrc

RUN \
    npm config set unsafe-perm true && \
    npm install -g @angular/cli@${ANGULAR_VERSION} && \
    ng --version && \
    which ng

COPY entrypoint.sh /root/entrypoint.sh

RUN \
    chmod +x /root/entrypoint.sh

VOLUME /data

# Define working directory
WORKDIR /root

CMD /root/entrypoint.sh

