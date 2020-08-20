#!/usr/bin/env bash

docker run  \
--name hadoop-unit \
-v /tmp/hadoop-unit:/root/hadoop-unit-data \
-v /tmp/hadoop-unit-m2:/root/.m2 \
-ti \
-d \
-p 127.0.0.1:22010:22010 \
-p 127.0.0.1:20112:20112 \
-p 127.0.0.1:50070:50070 \
-p 127.0.0.1:50010:50010 \
-p 127.0.0.1:50075:50075 \
-p 127.0.0.1:50020:50020 \
-p 127.0.0.1:20111:20111 \
-p 127.0.0.1:25111:25111 \
-p 127.0.0.1:28000:28000 \
-p 127.0.0.1:28080:28080 \
-p 127.0.0.1:8983:8983 \
-p 127.0.0.1:13533:13533 \
-p 127.0.0.1:13433:13433 \
-p 127.0.0.1:20102:20102 \
-p 127.0.0.1:20103:20103 \
hadoop-unit