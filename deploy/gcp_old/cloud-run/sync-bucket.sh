#!/usr/bin/env bash
# Upload Skymill config files to gs:// per config/gcs-sync.conf (post-apply helper).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/sync.env"

log() { printf '\n==> %s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

# shellcheck source=../lib/load-dotenv.sh
source "${SCRIPT_DIR}/../lib/load-dotenv.sh"
# shellcheck source=../lib/gcs-sync.sh
source "${SCRIPT_DIR}/../lib/gcs-sync.sh"

main() {
  [[ -f "${ENV_FILE}" ]] || die "Create sync.env from sync.env.example (match terraform.tfvars gcs/project)"
  load_dotenv "${ENV_FILE}"
  [[ -n "${GCP_PROJECT_ID:-}" ]] || die "Set GCP_PROJECT_ID in sync.env"
  [[ -n "${GCS_BUCKET_NAME:-}" ]] || die "Set GCS_BUCKET_NAME in sync.env"

  command -v gcloud >/dev/null 2>&1 || die "Install gcloud"
  gcloud auth list --filter=status:ACTIVE --format='value(account)' | grep -q . \
    || die "Run: gcloud auth login"

  gcp_sync_init
  GCP_SYNC_CLOUD_RUN_DIR="${SCRIPT_DIR}"
  export GCP_SYNC_CLOUD_RUN_DIR

  gcloud config set project "${GCP_PROJECT_ID}" --quiet
  run_gcs_sync
  log "Done: gs://${GCS_BUCKET_NAME}"
}

main "$@"
