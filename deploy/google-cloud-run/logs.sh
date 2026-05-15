#!/usr/bin/env bash
# Tail Cloud Run logs for the service defined in .env
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"
LIMIT="${1:-100}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "${ENV_FILE}"
  set +a
else
  echo "Missing ${ENV_FILE}" >&2
  exit 1
fi

: "${GCP_PROJECT_ID:?}"
: "${GCP_REGION:?}"
: "${CLOUD_RUN_SERVICE_NAME:?}"

gcloud config set project "${GCP_PROJECT_ID}" >/dev/null

echo "==> Service status"
gcloud run services describe "${CLOUD_RUN_SERVICE_NAME}" \
  --region "${GCP_REGION}" \
  --format='table(status.conditions.type,status.conditions.status,status.conditions.message)'

echo ""
echo "==> Revisions"
gcloud run revisions list \
  --service "${CLOUD_RUN_SERVICE_NAME}" \
  --region "${GCP_REGION}" \
  --format='table(name,active,ready,status.conditions.status)'

LATEST_REV="$(gcloud run revisions list \
  --service "${CLOUD_RUN_SERVICE_NAME}" \
  --region "${GCP_REGION}" \
  --format='value(name)' \
  --limit=1)"

echo ""
echo "==> Application logs (stdout/stderr, last ${LIMIT} lines)"
if [[ -n "${LATEST_REV}" ]]; then
  gcloud run services logs read "${CLOUD_RUN_SERVICE_NAME}" \
    --region "${GCP_REGION}" \
    --limit "${LIMIT}" 2>/dev/null || true
  gcloud logging read \
    "resource.type=cloud_run_revision AND resource.labels.service_name=${CLOUD_RUN_SERVICE_NAME} AND resource.labels.revision_name=${LATEST_REV} AND (textPayload:* OR jsonPayload.message:*)" \
    --project "${GCP_PROJECT_ID}" \
    --limit "${LIMIT}" \
    --format='table(timestamp,severity,textPayload,jsonPayload.message)' \
    --freshness=7d 2>/dev/null || true
else
  echo "(no revisions)"
fi

echo ""
echo "==> Platform / audit events (deploy failures)"
gcloud logging read \
  "resource.type=cloud_run_revision AND resource.labels.service_name=${CLOUD_RUN_SERVICE_NAME} AND severity>=WARNING" \
  --project "${GCP_PROJECT_ID}" \
  --limit 20 \
  --format='table(timestamp,severity,protoPayload.status.message,textPayload)' \
  --freshness=7d

if [[ -n "${LATEST_REV}" ]]; then
  echo ""
  echo "Console: https://console.cloud.google.com/run/detail/${GCP_REGION}/${CLOUD_RUN_SERVICE_NAME}/logs?project=${GCP_PROJECT_ID}"
fi
