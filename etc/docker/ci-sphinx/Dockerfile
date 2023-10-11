FROM python:3

WORKDIR /usr/src/app

RUN \
    pip install -U rst2pdf sphinx sphinx_rtd_theme sphinx-material sphinx-pdj-theme sphinx-tabs && \ 
    apt update && \
    apt-get install -y latexmk texlive-formats-extra  && \
    apt-get clean autoclean && \
    apt-get autoremove --yes && \
    rm -rf /var/lib/{apt,dpkg,cache,log}/ 