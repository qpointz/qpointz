FROM python:3

WORKDIR /usr/src/app

COPY multi-version-info.py /usr/bin

RUN \
    chmod +x /usr/bin/multi-version-info.py && \
    pip install rst2pdf sphinx sphinx-multiversion sphinx_rtd_theme sphinx-material sphinx-pdj-theme sphinx-tabs && \ 
    apt update && \
    apt-get install -y latexmk texlive-formats-extra  && \
    apt-get clean autoclean && \
    apt-get autoremove --yes && \
    rm -rf /var/lib/{apt,dpkg,cache,log}/ 

RUN \
    ls /usr/bin