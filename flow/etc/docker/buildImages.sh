#!/bin/bash

rootDir=`dirname $0`

function buildImg {
    local imgName=${1}
    local tagName=${2}
    docker build --tag ${tagName} ${rootDir}/images/${imgName}
}

function pushImg {
    local tagName=${1}
    docker push ${tagName}
}

function buildAndPushImg {
    local imgName=${1}
    local tagName="qpointz/${imgName}"
    buildImg ${imgName} ${tagName}
    pushImg ${tagName}
}


buildAndPushImg enzyme-ci-npm
buildAndPushImg enzyme-ci-sbt
buildAndPushImg enzyme-ci-sphinx
#buildAndPushImg postgres-test