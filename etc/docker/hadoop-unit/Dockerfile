FROM maven:3.6.3-jdk-8

RUN cd /root && \
    wget https://repo1.maven.org/maven2/fr/jetoile/hadoop/hadoop-unit-standalone/3.7/hadoop-unit-standalone-3.7.tar.gz && \
    tar xzf hadoop-unit-standalone-3.7.tar.gz && \
    rm hadoop-unit-standalone-3.7.tar.gz && \
    mv hadoop-unit-standalone-3.7 hadoop-unit-standalone && \
    cd hadoop-unit-standalone && \
    ls -lac

COPY hadoop.properties /root/hadoop-unit-standalone/conf
COPY hadoop-unit-default.properties /root/hadoop-unit-standalone/conf
COPY wrapper-hadoop-unit-standalone.conf /root/hadoop-unit-standalone/conf
#COPY settings.xml /root/.m2


VOLUME /data

#zookeper
EXPOSE 22010

#hdfs
EXPOSE 20112
EXPOSE 50070
EXPOSE 50010
EXPOSE 50075
EXPOSE 50020

#kafka
EXPOSE 20111

#hbase
EXPOSE 25111
EXPOSE 28000
EXPOSE 28080

#solr
#EXPOSE 8983

#neo4j
#EXPOSE 13533

#cassandra
#EXPOSE 13433

#hivemeta
#EXPOSE 20102

#hive2
#EXPOSE 20103

WORKDIR /root/hadoop-unit-standalone

ENTRYPOINT ["bin/hadoop-unit-standalone" , "console"]
