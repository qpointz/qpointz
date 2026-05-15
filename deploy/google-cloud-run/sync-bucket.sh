#!/usr/bin/env bash
# Upload local files to the configured GCS bucket (project + bucket created if configured).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

log() { printf '\n==> %s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "Missing required command: $1"
}

# shellcheck source=lib/load-dotenv.sh
source "${SCRIPT_DIR}/lib/load-dotenv.sh"

# shellcheck disable=SC1090
load_env() {
  if [[ -f "${ENV_FILE}" ]]; then
    load_dotenv "${ENV_FILE}"
  else
    die "Create ${ENV_FILE} from env.example"
  fi
}

# shellcheck source=lib/gcp-provision.sh
source "${SCRIPT_DIR}/lib/gcp-provision.sh"

main() {
  load_env
  [[ -n "${GCP_PROJECT_ID:-}" ]] || die "Set GCP_PROJECT_ID"
  [[ -n "${GCS_BUCKET_NAME:-}" ]] || die "Set GCS_BUCKET_NAME"
  GCS_SYNC_ENABLED=true

  require_cmd gcloud
  gcloud auth list --filter=status:ACTIVE --format='value(account)' | grep -q . \
    || die "Run: gcloud auth login"

  provision_gcp_resources
  log "Sync complete: gs://${GCS_BUCKET_NAME}"
}

main "$@"
