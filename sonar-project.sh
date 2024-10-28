#!/bin/sh 

cat << EOF > sonar-project.properties
sonar.projectKey=qpointz-delta
sonar.projectName=qpointz-delta
sonar.qualitygate.wait=true
sonar.token=
sonar.host.url=https://sonarqube.qpointz.io
sonar.tests=mill/clients/mill-py/tests,$(find . -wholename '*/src/test' -printf '%P\n' | xargs | sed 's/ /,/g')
sonar.sources=mill/clients/mill-py/millclient,$(find . -wholename '*/src/main' -printf '%P\n' | xargs | sed 's/ /,/g')
sonar.java.binaries=$(find . -wholename '*/build/classes/java/main' -printf '%P\n' | xargs | sed 's/ /,/g')
sonar.java.libraries=$(find .gradle_home/caches -name '*.jar' -print | xargs | sed 's/ /,/g')
sonar.java.test.binaries=$(find . -wholename '*/build/classes/java/main' -printf '%P\n' | xargs | sed 's/ /,/g')
sonar.java.test.libraries=$(find .gradle_home/caches -name '*.jar' -print | xargs | sed 's/ /,/g')
sonar.junit.reportPaths=$(find . -wholename '*/build/test-results/test' -printf '%P\n' | xargs | sed 's/ /,/g')
sonar.python.version=3.12
sonar.coverage.jacoco.xmlReportPaths=$(find . -wholename '*/build/reports/jacoco/*/*.xml' -printf '%P\n' | xargs | sed 's/ /,/g')
sonar.python.coverage.reportPaths=mill/clients/mill-py/coverage.xml
EOF


cat sonar-project.properties

docker run -v "/home/vm/wip/qpointz/qpointz:/usr/src" sonarsource/sonar-scanner-cli