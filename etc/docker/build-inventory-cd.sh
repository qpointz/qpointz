#!/usr/bin/env bash

CI_COMMIT_REF_NAME=dev


tag=inv-ui-$CI_COMMIT_REF_NAME
if [ $(docker ps -q --filter "ancestor=${tag}") ]; then 
  docker stop $(docker ps -q --filter "ancestor=${tag}")
fi 

if [ $(docker ps -q -a --filter "name=${tag}") ]; then 
  docker rm $(docker ps -q -a --filter "name=${tag}")
fi 

docker build --tag ${tag} inventory-cd/
docker run -dit --name ${tag} -p 8090:8080 ${tag}
