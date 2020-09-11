FROM openjdk:8-jdk

ENV SPARK_VERSION="2.4.5"
ENV HADOOP_VERSION="2.7"
ENV SPARK_FILE_NAME="spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION"
ENV SPARK_DL_LINK="https://downloads.apache.org/spark/spark-$SPARK_VERSION/$SPARK_FILE_NAME.tgz"
ENV SPARK_PATH="/spark"

RUN apt update && \
    apt upgrade -y && \
    wget -O $SPARK_FILE_NAME.tgz $SPARK_DL_LINK

RUN tar zxvf $SPARK_FILE_NAME.tgz && \
    mv $SPARK_FILE_NAME $SPARK_PATH && \
    rm $SPARK_FILE_NAME.tgz && \
    ls $SPARK_PATH/bin