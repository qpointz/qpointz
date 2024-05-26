FROM python:3

RUN \    
    pip install rst2pdf sphinx sphinx-multiversion sphinx_rtd_theme sphinx-material sphinx-pdj-theme sphinx-tabs myst-parser && \ 
    apt update && \
    apt-get install -y latexmk texlive-formats-extra  && \
    apt-get clean autoclean && \
    apt-get autoremove --yes && \
    rm -rf /var/lib/{apt,dpkg,cache,log}/

WORKDIR /usr/src/app

COPY multi-version-info.py /usr/bin

RUN chmod +x /usr/bin/multi-version-info.py