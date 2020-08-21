FROM ubuntu:18.04

RUN echo "==== begin build ===="  && \
    echo "==== add extra packages====" && \
    apt update && \
    apt install software-properties-common gosu curl unzip groff less git openjdk-8-jdk nano -y && \
    echo "==== add python repositories====" && \
    add-apt-repository ppa:deadsnakes/ppa && \
    apt update && \
    echo "==== installing python ====" && \
    apt install  python3.7 -y && \
    echo "==== clean apt ====" && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN echo "==== configuring python ====" && \
    update-alternatives --install /usr/bin/python python /usr/bin/python3.6 1 && \
    update-alternatives --install /usr/bin/python python /usr/bin/python3.7 2 && \
    curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py && \
    python get-pip.py && \
    pip install virtualenv

ADD opt /opt

RUN echo "==== configuring user profile ====" && \
    mkdir -p /etc/skel/.local && \
    mv /opt/qp/.bashrc /etc/skel/.local && \
    echo 'if [ -f ~/.local/.bashrc ]; then' >> /etc/skel/.bashrc && \
    echo '. ~/.local/.bashrc' >> /etc/skel/.bashrc && \
    echo 'fi' >> /etc/skel/.bashrc && \
    echo 'export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64' >> /etc/skel/.local/.bashrc && \
    echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/skel/.local/.bashrc && \
    mkdir -p /opt/qp/init

ENTRYPOINT ["/opt/qp/init.sh"]