#!/usr/bin/env bash
# Tear down ACA resources in the resource group (destructive).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "${ENV_FILE}"
  set +a
else
  echo "Missing ${ENV_FILE}" >&2
  exit 1
fi

: "${AZURE_RESOURCE_GROUP:?Set AZURE_RESOURCE_GROUP in .env}"

read -r -p "Delete entire resource group '${AZURE_RESOURCE_GROUP}'? [y/N] " confirm
if [[ "${confirm}" != "y" && "${confirm}" != "Y" ]]; then
  echo "Aborted."
  exit 0
fi

az group delete --name "${AZURE_RESOURCE_GROUP}" --yes --no-wait
echo "Delete initiated for ${AZURE_RESOURCE_GROUP}"
