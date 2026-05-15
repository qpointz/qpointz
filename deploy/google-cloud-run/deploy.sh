#!/usr/bin/env bash
# One-shot: provision GCP (optional), sync GCS (optional), deploy mill-service to Cloud Run from Docker Hub.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"
# Mount path for CUSTOM_APPLICATION_YML (Secret Manager file mount). Must NOT be under
# /config — the mill-service image declares VOLUME /config, which masks sub-path mounts on Cloud Run.
MILL_RUNTIME_CONFIG_FILE="/mill-config/application.yml"

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
  elif [[ -f "${SCRIPT_DIR}/env.example" ]]; then
    die "Create ${ENV_FILE} from env.example and set GCP_PROJECT_ID, DB credentials, image tag."
  else
    die "Missing ${ENV_FILE}"
  fi
}

# shellcheck source=lib/gcp-provision.sh
source "${SCRIPT_DIR}/lib/gcp-provision.sh"

gcp_login() {
  require_cmd gcloud
  gcloud auth list --filter=status:ACTIVE --format='value(account)' | grep -q . \
    || die "Run: gcloud auth login"
}

resolve_service_account() {
  if [[ -n "${CLOUD_RUN_SERVICE_ACCOUNT:-}" ]]; then
    RUN_SA="${CLOUD_RUN_SERVICE_ACCOUNT}"
    return 0
  fi
  local project_number
  project_number="$(gcloud projects describe "${GCP_PROJECT_ID}" --format='value(projectNumber)')"
  RUN_SA="${project_number}-compute@developer.gserviceaccount.com"
}

grant_secret_accessor() {
  local secret_name="$1"
  local project_number
  project_number="$(gcloud projects describe "${GCP_PROJECT_ID}" --format='value(projectNumber)')"
  local run_agent="service-${project_number}@serverless-robot-prod.iam.gserviceaccount.com"
  for member in "serviceAccount:${RUN_SA}" "serviceAccount:${run_agent}"; do
    gcloud secrets add-iam-policy-binding "${secret_name}" \
      --project "${GCP_PROJECT_ID}" \
      --member "${member}" \
      --role "roles/secretmanager.secretAccessor" \
      --quiet >/dev/null 2>&1 || true
  done
}

ensure_secret_from_value() {
  local secret_name="$1"
  local secret_value="$2"
  if gcloud secrets describe "${secret_name}" --project="${GCP_PROJECT_ID}" >/dev/null 2>&1; then
    printf '%s' "${secret_value}" | gcloud secrets versions add "${secret_name}" \
      --project="${GCP_PROJECT_ID}" --data-file=- >/dev/null
  else
    printf '%s' "${secret_value}" | gcloud secrets create "${secret_name}" \
      --project="${GCP_PROJECT_ID}" \
      --replication-policy=automatic \
      --labels="$(mill_deploy_labels_csv)" \
      --data-file=- >/dev/null
  fi
  grant_secret_accessor "${secret_name}"
  mill_tag_secret "${secret_name}"
}

ensure_secret_from_file() {
  local secret_name="$1"
  local file_path="$2"
  if gcloud secrets describe "${secret_name}" --project="${GCP_PROJECT_ID}" >/dev/null 2>&1; then
    gcloud secrets versions add "${secret_name}" \
      --project="${GCP_PROJECT_ID}" \
      --data-file="${file_path}" >/dev/null
  else
    gcloud secrets create "${secret_name}" \
      --project="${GCP_PROJECT_ID}" \
      --replication-policy=automatic \
      --labels="$(mill_deploy_labels_csv)" \
      --data-file="${file_path}" >/dev/null
  fi
  grant_secret_accessor "${secret_name}"
  mill_tag_secret "${secret_name}"
}

maybe_register_application_yml() {
  APPLICATION_YML_MOUNT=""
  if [[ -z "${CUSTOM_APPLICATION_YML:-}" ]]; then
    return 0
  fi
  local yml_path="${CUSTOM_APPLICATION_YML}"
  if [[ "${yml_path}" != /* ]]; then
    yml_path="${SCRIPT_DIR}/${yml_path}"
  fi
  [[ -f "${yml_path}" ]] || die "CUSTOM_APPLICATION_YML not found: ${yml_path}"

  local secret_name="mill-${CLOUD_RUN_SERVICE_NAME}-application-yml"
  log "Uploading ${yml_path} to Secret Manager (${secret_name})"
  ensure_secret_from_file "${secret_name}" "${yml_path}"
  APPLICATION_YML_MOUNT="${MILL_RUNTIME_CONFIG_FILE}=${secret_name}:latest"
}

register_env_secrets() {
  SECRET_REFS=()
  ensure_secret_from_value "mill-${CLOUD_RUN_SERVICE_NAME}-db-password" "${MILL_DB_PASSWORD}"
  SECRET_REFS+=("MILL_DB_PASSWORD=mill-${CLOUD_RUN_SERVICE_NAME}-db-password:latest")

  if [[ -n "${OPENAI_API_KEY:-}" ]]; then
    ensure_secret_from_value "mill-${CLOUD_RUN_SERVICE_NAME}-openai-key" "${OPENAI_API_KEY}"
    SECRET_REFS+=("OPENAI_API_KEY=mill-${CLOUD_RUN_SERVICE_NAME}-openai-key:latest")
  fi

  if [[ -n "${APPLICATION_YML_MOUNT:-}" ]]; then
    SECRET_REFS+=("${APPLICATION_YML_MOUNT}")
  fi
}

build_env_vars_file() {
  local profiles="${SPRING_PROFILES_ACTIVE:-skymill,postgres,secure}"
  ENV_VARS_FILE="$(mktemp)"

  cat >"${ENV_VARS_FILE}" <<EOF
SPRING_PROFILES_ACTIVE: '${profiles}'
MILL_DB_URL: '${MILL_DB_URL}'
MILL_DB_USERNAME: '${MILL_DB_USERNAME}'
EOF

  if [[ -n "${CUSTOM_APPLICATION_YML:-}" ]]; then
    printf 'SPRING_CONFIG_ADDITIONAL_LOCATION: "file:%s"\n' "${MILL_RUNTIME_CONFIG_FILE}" >>"${ENV_VARS_FILE}"
  fi

  if [[ -n "${MILL_CLOUD_GCP_GCS_PROJECT_ID:-}" ]]; then
    printf "MILL_CLOUD_GCP_GCS_PROJECT_ID: '%s'\n" "${MILL_CLOUD_GCP_GCS_PROJECT_ID}" >>"${ENV_VARS_FILE}"
  elif [[ -n "${GCS_BUCKET_NAME:-}" ]]; then
    printf "MILL_CLOUD_GCP_GCS_PROJECT_ID: '%s'\n" "${GCP_PROJECT_ID}" >>"${ENV_VARS_FILE}"
  fi

  if [[ -n "${GCS_BUCKET_NAME:-}" ]]; then
    printf "GCS_BUCKET_NAME: '%s'\n" "${GCS_BUCKET_NAME}" >>"${ENV_VARS_FILE}"
  fi
}

join_by_comma() {
  local IFS=,
  echo "$*"
}

deploy_cloud_run() {
  build_env_vars_file
  register_env_secrets

  local image="${MILL_DOCKER_IMAGE:?Set MILL_DOCKER_IMAGE in .env}"
  local secret_csv
  secret_csv="$(join_by_comma "${SECRET_REFS[@]}")"

  local deploy_args=(
    run deploy "${CLOUD_RUN_SERVICE_NAME}"
    --project "${GCP_PROJECT_ID}"
    --region "${GCP_REGION}"
    --image "${image}"
    --port "${CLOUD_RUN_PORT:-8080}"
    --cpu "${CLOUD_RUN_CPU:-1}"
    --memory "${CLOUD_RUN_MEMORY:-2Gi}"
    --min-instances "${CLOUD_RUN_MIN_INSTANCES:-0}"
    --max-instances "${CLOUD_RUN_MAX_INSTANCES:-3}"
    --concurrency "${CLOUD_RUN_CONCURRENCY:-80}"
    --env-vars-file "${ENV_VARS_FILE}"
    --set-secrets "${secret_csv}"
    --labels "$(mill_deploy_labels_csv)"
  )

  if [[ -n "${CLOUD_RUN_SERVICE_ACCOUNT:-}" ]]; then
    deploy_args+=(--service-account "${CLOUD_RUN_SERVICE_ACCOUNT}")
  fi

  if [[ -n "${CLOUD_SQL_CONNECTION_NAME:-}" ]]; then
    deploy_args+=(--add-cloudsql-instances "${CLOUD_SQL_CONNECTION_NAME}")
  fi

  if [[ "${CLOUD_RUN_ALLOW_UNAUTHENTICATED:-true}" == "true" ]]; then
    deploy_args+=(--allow-unauthenticated)
  else
    deploy_args+=(--no-allow-unauthenticated)
  fi

  log "Deploying Cloud Run service ${CLOUD_RUN_SERVICE_NAME} (${image})"
  gcloud "${deploy_args[@]}"
  rm -f "${ENV_VARS_FILE}"

  local url
  url="$(gcloud run services describe "${CLOUD_RUN_SERVICE_NAME}" \
    --project "${GCP_PROJECT_ID}" \
    --region "${GCP_REGION}" \
    --format 'value(status.url)')"
  log "Deployed. URL: ${url}"
  log "Health:  ${url}/actuator/health"
  if [[ "${SPRING_PROFILES_ACTIVE:-}" == *oauth* ]]; then
    log "If OAuth is enabled, register redirect URI: ${url}/login/oauth2/code/authentik"
  fi
  if [[ -n "${CUSTOM_APPLICATION_YML:-}" ]]; then
    log "Custom config mounted from Secret Manager at ${MILL_RUNTIME_CONFIG_FILE}"
  fi
  if [[ -n "${GCS_BUCKET_NAME:-}" ]]; then
    log "GCS config bucket: gs://${GCS_BUCKET_NAME}"
  fi
}

validate_config() {
  [[ -n "${GCP_PROJECT_ID:-}" ]] || die "Set GCP_PROJECT_ID in .env"
  [[ -n "${GCP_REGION:-}" ]] || die "Set GCP_REGION in .env"
  [[ -n "${CLOUD_RUN_SERVICE_NAME:-}" ]] || die "Set CLOUD_RUN_SERVICE_NAME in .env"
  [[ -n "${MILL_DOCKER_IMAGE:-}" ]] || die "Set MILL_DOCKER_IMAGE (e.g. docker.io/qpointz/mill-service-complete:latest)"
  [[ -n "${MILL_DB_URL:-}" ]] || die "Set MILL_DB_URL in .env"
  [[ -n "${MILL_DB_USERNAME:-}" ]] || die "Set MILL_DB_USERNAME in .env"
  [[ -n "${MILL_DB_PASSWORD:-}" ]] || die "Set MILL_DB_PASSWORD in .env"
  if [[ "${GCS_SYNC_ENABLED:-false}" == "true" && -z "${GCS_BUCKET_NAME:-}" ]]; then
    die "Set GCS_BUCKET_NAME when GCS_SYNC_ENABLED=true"
  fi
  if [[ "${CREATE_GCP_PROJECT:-false}" == "true" && -z "${GCP_BILLING_ACCOUNT_ID:-}" ]]; then
    die "Set GCP_BILLING_ACCOUNT_ID when CREATE_GCP_PROJECT=true"
  fi
}

main() {
  load_env
  validate_config
  gcp_login
  provision_gcp_resources
  resolve_service_account
  grant_gcs_bucket_reader
  maybe_register_application_yml
  deploy_cloud_run
  mill_tag_all_deploy_resources
}

main "$@"
