#!/usr/bin/env bash
# Wait until a TCP port accepts connections (integration service readiness).
set -euo pipefail

host="${1:?host required}"
port="${2:?port required}"
max_wait="${3:-120}"
interval="${4:-2}"

elapsed=0
while [ "${elapsed}" -lt "${max_wait}" ]; do
  if (echo >/dev/tcp/"${host}"/"${port}") 2>/dev/null; then
    echo "Service ready at ${host}:${port} after ${elapsed}s"
    exit 0
  fi
  sleep "${interval}"
  elapsed=$((elapsed + interval))
done

echo "Timeout after ${max_wait}s waiting for ${host}:${port}"
exit 1
