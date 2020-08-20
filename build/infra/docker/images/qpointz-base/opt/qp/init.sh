#!/usr/bin/env bash
for f in /opt/qp/init/[0-9][0-9]_*; do
  source $f
done

if [ -f /opt/qp/run.sh ]; then
    source /opt/qp/run.sh
fi