FROM postgres:14

#RUN apk add --no-cache wget unzip && \
#    mkdir -p /tmp/dvdrental && \
#    wget -O /tmp/dvdrental/dvdrental.zip https://www.postgresqltutorial.com/wp-content/uploads/2019/05/dvdrental.zip && \
#    cd /tmp/dvdrental && \
#    unzip dvdrental.zip && \
#    tar xvf dvdrental.tar && \
#    rm dvdrental.* && \
#    ls -lac && \
#    sed -i 's+\$\$PATH\$\$+/tmp/dvdrental+g' restore.sql && \
#    sed -i 's+DROP DATABASE+DROP DATABASE IF EXISTS+g' restore.sql && \
#    head -n 100 restore.sql && \
#    mv restore.sql /docker-entrypoint-initdb.d/02_dvdrental-restore.sql && \
#    chmod o+r /docker-entrypoint-initdb.d/02_dvdrental-restore.sql && \
#    ls  -lac /docker-entrypoint-initdb.d

ADD 01_init.sql /docker-entrypoint-initdb.d/01_init.sql

EXPOSE 5432