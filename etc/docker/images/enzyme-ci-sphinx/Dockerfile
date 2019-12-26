FROM python:3

WORKDIR /usr/src/app

RUN \
    apt update

RUN \
    pip install -U rst2pdf && \
    pip install -U sphinx && \
    pip install -U sphinx_rtd_theme

RUN \
    apt-get install -y latexmk texlive-formats-extra