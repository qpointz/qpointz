# Shared GCP provisioning helpers for Cloud Run deploy.
# Source from deploy.sh / sync-bucket.sh — do not execute directly.

# shellcheck source=gcp-labels.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/gcp-labels.sh"

gcp_provision_init() {
  GCP_PROVISION_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
  GCP_PROVISION_REPO_ROOT="$(cd "${GCP_PROVISION_SCRIPT_DIR}/../.." && pwd)"
}

gcp_resolve_path() {
  local path="$1"
  if [[ "${path}" == /* ]]; then
    printf '%s' "${path}"
  elif [[ "${path}" == ../* ]] || [[ "${path}" == "../"* ]]; then
    printf '%s' "${GCP_PROVISION_REPO_ROOT}/${path#../}"
  else
    printf '%s' "${GCP_PROVISION_REPO_ROOT}/${path}"
  fi
}

# Cloud Resource Manager expects numeric IDs for --folder and --organization.
# Accepts "123456789", "folders/123456789", or "organizations/123456789".
mill_normalize_folder_id() {
  local raw="$1"
  raw="${raw#"${raw%%[![:space:]]*}"}"
  raw="${raw%"${raw##*[![:space:]]}"}"
  raw="${raw#folders/}"
  raw="${raw#Folders/}"
  raw="${raw#FOLDERS/}"
  if [[ "${raw}" =~ ^[0-9]+$ ]]; then
    printf '%s' "${raw}"
    return 0
  fi
  return 1
}

mill_normalize_organization_id() {
  local raw="$1"
  raw="${raw#"${raw%%[![:space:]]*}"}"
  raw="${raw%"${raw##*[![:space:]]}"}"
  raw="${raw#organizations/}"
  raw="${raw#Organizations/}"
  raw="${raw#ORGANIZATIONS/}"
  if [[ "${raw}" =~ ^[0-9]+$ ]]; then
    printf '%s' "${raw}"
    return 0
  fi
  return 1
}

ensure_gcp_project() {
  if gcloud projects describe "${GCP_PROJECT_ID}" >/dev/null 2>&1; then
    log "GCP project ${GCP_PROJECT_ID} already exists"
    return 0
  fi

  if [[ "${CREATE_GCP_PROJECT:-false}" != "true" ]]; then
    die "GCP project '${GCP_PROJECT_ID}' not found. Create it in the console or set CREATE_GCP_PROJECT=true and GCP_BILLING_ACCOUNT_ID in .env"
  fi

  [[ -n "${GCP_BILLING_ACCOUNT_ID:-}" ]] \
    || die "Set GCP_BILLING_ACCOUNT_ID (gcloud billing accounts list) to create project ${GCP_PROJECT_ID}"

  local project_name="${GCP_PROJECT_NAME:-Mill Cloud Run}"
  log "Creating GCP project ${GCP_PROJECT_ID} (${project_name})"
  if [[ -n "${GCP_FOLDER_ID:-}" ]]; then
    local folder_id
    if ! folder_id="$(mill_normalize_folder_id "${GCP_FOLDER_ID}")"; then
      die "GCP_FOLDER_ID must be a numeric folder ID (e.g. 123456789) or folders/123456789. Got: ${GCP_FOLDER_ID}"
    fi
    gcloud projects create "${GCP_PROJECT_ID}" \
      --name="${project_name}" \
      --folder="${folder_id}" \
      --quiet
  elif [[ -n "${GCP_ORGANIZATION_ID:-}" ]]; then
    local org_id
    if ! org_id="$(mill_normalize_organization_id "${GCP_ORGANIZATION_ID}")"; then
      die "GCP_ORGANIZATION_ID must be a numeric organization ID (e.g. 123456789) or organizations/123456789. Got: ${GCP_ORGANIZATION_ID}"
    fi
    gcloud projects create "${GCP_PROJECT_ID}" \
      --name="${project_name}" \
      --organization="${org_id}" \
      --quiet
  else
    gcloud projects create "${GCP_PROJECT_ID}" \
      --name="${project_name}" \
      --quiet
  fi

  log "Linking billing account ${GCP_BILLING_ACCOUNT_ID}"
  gcloud billing projects link "${GCP_PROJECT_ID}" \
    --billing-account="${GCP_BILLING_ACCOUNT_ID}" \
    --quiet
  mill_tag_project
}

enable_gcp_apis() {
  log "Enabling required APIs on ${GCP_PROJECT_ID}"
  gcloud services enable \
    run.googleapis.com \
    secretmanager.googleapis.com \
    storage.googleapis.com \
    cloudresourcemanager.googleapis.com \
    --project "${GCP_PROJECT_ID}" \
    --quiet
}

ensure_gcs_bucket() {
  [[ -n "${GCS_BUCKET_NAME:-}" ]] || return 0

  local bucket_uri="gs://${GCS_BUCKET_NAME}"
  if gcloud storage buckets describe "${bucket_uri}" --project "${GCP_PROJECT_ID}" >/dev/null 2>&1; then
    log "GCS bucket ${bucket_uri} already exists"
    mill_tag_gcs_bucket "${GCS_BUCKET_NAME}"
    return 0
  fi

  local location="${GCS_BUCKET_LOCATION:-${GCP_REGION}}"
  log "Creating GCS bucket ${bucket_uri} in ${location}"
  # Note: gcloud storage buckets create does not accept --labels; tag after create.
  gcloud storage buckets create "${bucket_uri}" \
    --project="${GCP_PROJECT_ID}" \
    --location="${location}" \
    --uniform-bucket-level-access \
    --quiet
  mill_tag_gcs_bucket "${GCS_BUCKET_NAME}"
}

grant_gcs_bucket_reader() {
  [[ -n "${GCS_BUCKET_NAME:-}" ]] || return 0

  local bucket_uri="gs://${GCS_BUCKET_NAME}"
  local members=()

  if [[ -n "${CLOUD_RUN_SERVICE_ACCOUNT:-}" ]]; then
    members+=("serviceAccount:${CLOUD_RUN_SERVICE_ACCOUNT}")
  fi
  if [[ -n "${RUN_SA:-}" ]]; then
    members+=("serviceAccount:${RUN_SA}")
  fi

  local member
  for member in "${members[@]}"; do
    log "Granting Storage Object Viewer on ${bucket_uri} to ${member}"
    gcloud storage buckets add-iam-policy-binding "${bucket_uri}" \
      --member="${member}" \
      --role="roles/storage.objectViewer" \
      --quiet >/dev/null 2>&1 || true
  done
}

# Sync local paths into gs://BUCKET/ per config/gcs-sync.conf (see file header).
run_gcs_sync() {
  if [[ "${GCS_SYNC_ENABLED:-false}" != "true" ]]; then
    return 0
  fi
  [[ -n "${GCS_BUCKET_NAME:-}" ]] \
    || die "Set GCS_BUCKET_NAME when GCS_SYNC_ENABLED=true"

  local sync_config="${GCS_SYNC_CONFIG:-config/gcs-sync.conf}"
  if [[ "${sync_config}" != /* ]]; then
    sync_config="${GCP_PROVISION_SCRIPT_DIR}/${sync_config}"
  fi
  [[ -f "${sync_config}" ]] || die "GCS sync config not found: ${sync_config}"

  local bucket_uri="gs://${GCS_BUCKET_NAME}"
  log "Syncing local files to ${bucket_uri} (config: ${sync_config})"

  local line local_path gcs_path resolved_local dest_uri
  while IFS= read -r line || [[ -n "${line}" ]]; do
    line="${line%%#*}"
    line="$(printf '%s' "${line}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    [[ -z "${line}" ]] && continue

    if [[ "${line}" != *"|"* ]]; then
      die "Invalid gcs-sync line (expected 'local_path | gcs_path'): ${line}"
    fi

    local_path="${line%%|*}"
    gcs_path="${line#*|}"
    local_path="$(printf '%s' "${local_path}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    gcs_path="$(printf '%s' "${gcs_path}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    gcs_path="${gcs_path#/}"

    resolved_local="$(gcp_resolve_path "${local_path}")"
    dest_uri="${bucket_uri}/${gcs_path}"

    if [[ -d "${resolved_local}" ]]; then
      log "  rsync ${resolved_local}/ -> ${dest_uri}/"
      gcloud storage rsync "${resolved_local}" "${dest_uri}" \
        --recursive \
        --delete-unmatched-destination-objects="${GCS_SYNC_DELETE_EXTRA:-false}" \
        --quiet
    elif [[ -f "${resolved_local}" ]]; then
      log "  cp ${resolved_local} -> ${dest_uri}"
      gcloud storage cp "${resolved_local}" "${dest_uri}" --quiet
    else
      die "GCS sync source not found: ${resolved_local} (from ${local_path})"
    fi
  done <"${sync_config}"
}

provision_gcp_resources() {
  gcp_provision_init
  ensure_gcp_project
  gcloud config set project "${GCP_PROJECT_ID}" >/dev/null
  enable_gcp_apis
  ensure_gcs_bucket
  run_gcs_sync
  mill_tag_project
}
