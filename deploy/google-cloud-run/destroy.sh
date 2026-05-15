#!/usr/bin/env bash
# Tear down Mill Cloud Run deploy resources.
#
# Usage:
#   ./destroy.sh [service|tagged|project] [-y]
#
# Scopes:
#   service  — Cloud Run service only (default)
#   tagged   — All resources labeled mill-managed-by=google-cloud-run + mill-deploy-stack
#   project  — Delete entire GCP project (destructive)
#
# Env: DESTROY_SCOPE, DESTROY_YES=true (skip prompts), MILL_DEPLOY_STACK_ID (label value)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

log() { printf '\n==> %s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

DESTROY_SCOPE="${DESTROY_SCOPE:-service}"
DESTROY_YES="${DESTROY_YES:-false}"

usage() {
  cat <<EOF
Usage: $(basename "$0") [service|tagged|project] [-y]

  service   Delete Cloud Run service '${CLOUD_RUN_SERVICE_NAME:-<name>}' only
  tagged    Delete labeled resources (Cloud Run, secrets, GCS bucket) for this stack
  project   Delete entire GCP project '${GCP_PROJECT_ID:-<id>}'

Options:
  -y, --yes     Skip confirmation prompts (DESTROY_YES=true)
  -h, --help    Show this help

Labels applied by deploy.sh (for tagged scope):
  mill-managed-by=google-cloud-run
  mill-deploy-stack=<MILL_DEPLOY_STACK_ID or CLOUD_RUN_SERVICE_NAME>
EOF
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      service|tagged|project) DESTROY_SCOPE="$1" ;;
      -y|--yes) DESTROY_YES=true ;;
      -h|--help) usage; exit 0 ;;
      *) die "Unknown argument: $1 (use -h for help)" ;;
    esac
    shift
  done
}

# shellcheck source=lib/load-dotenv.sh
source "${SCRIPT_DIR}/lib/load-dotenv.sh"

# shellcheck disable=SC1090
load_env() {
  if [[ -f "${ENV_FILE}" ]]; then
    load_dotenv "${ENV_FILE}"
  else
    die "Missing ${ENV_FILE}"
  fi
}

# shellcheck source=lib/gcp-labels.sh
source "${SCRIPT_DIR}/lib/gcp-labels.sh"
# shellcheck source=lib/gcp-destroy.sh
source "${SCRIPT_DIR}/lib/gcp-destroy.sh"

main() {
  parse_args "$@"
  load_env
  : "${GCP_PROJECT_ID:?Set GCP_PROJECT_ID in .env}"
  : "${GCP_REGION:?Set GCP_REGION in .env}"
  : "${CLOUD_RUN_SERVICE_NAME:?Set CLOUD_RUN_SERVICE_NAME in .env}"

  command -v gcloud >/dev/null 2>&1 || die "Missing gcloud"
  gcloud config set project "${GCP_PROJECT_ID}" >/dev/null

  run_destroy_scope "${DESTROY_SCOPE}"
}

main "$@"
