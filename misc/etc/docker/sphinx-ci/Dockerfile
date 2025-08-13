FROM gitlab.qpointz.io/qpointz/dependency_proxy/containers/python:3

RUN \    
    pip install rst2pdf sphinx sphinx-multiversion sphinx_rtd_theme sphinx-material sphinx-pdj-theme sphinx-tabs myst-parser semver && \ 
    apt update && \
    apt-get install -y latexmk texlive-formats-extra  jq && \
    apt-get clean autoclean && \
    apt-get autoremove --yes && \
    rm -rf /var/lib/{apt,dpkg,cache,log}/

WORKDIR /usr/src/app

COPY multi-version-info.py /usr/bin
COPY versioner.py /usr/local/bin

RUN chmod +x /usr/bin/multi-version-info.py && \
    chmod +x /usr/local/bin/versioner.py 