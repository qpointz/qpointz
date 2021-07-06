FROM python:3

WORKDIR /usr/src/app

RUN \
    apt update

RUN \
    pip install -U rst2pdf sphinx sphinx_rtd_theme sphinx-material sphinx-tabs


RUN \
    apt-get install -y latexmk texlive-formats-extra