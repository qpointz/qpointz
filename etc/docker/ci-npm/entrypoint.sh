#!/usr/bin/env bash

npm_global=/data/.npm-global

echo "Preparing"
if [ ! -d ${npm_global} ]; then
    mkdir -p ${npm_global}
    chmod -R o+w ${npm_global}
    chmod -R o+r ${npm_global}
fi

npm config set prefix ${npm_global}
npm config set unsafe-perm true

echo "Executing command $@"
exec "$@"

/bin/sh