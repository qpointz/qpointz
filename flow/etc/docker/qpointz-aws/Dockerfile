FROM docker.qpointz.io/qpointz-base:latest

#AWS CLI
RUN mkdir -p /opt && \
    cd /opt && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm awscliv2.zip && \
    pip install boto3

ADD opt /opt