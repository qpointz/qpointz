# Destroy helpers for Mill Cloud Run deploy.
# Requires: gcp-labels.sh sourced, .env loaded, log/die defined.

destroy_confirm() {
  local message="$1"
  if [[ "${DESTROY_YES:-false}" == "true" ]]; then
    return 0
  fi
  read -r -p "${message} [y/N] " reply
  [[ "${reply}" == "y" || "${reply}" == "Y" ]]
}

destroy_cloud_run_service() {
  local name="$1"
  local region="$2"
  if ! gcloud run services describe "${name}" \
    --project "${GCP_PROJECT_ID}" \
    --region "${region}" >/dev/null 2>&1; then
    log "Cloud Run service ${name} (${region}) not found — skip"
    return 0
  fi
  log "Deleting Cloud Run service ${name} (${region})"
  gcloud run services delete "${name}" \
    --project "${GCP_PROJECT_ID}" \
    --region "${region}" \
    --quiet
}

destroy_scope_service() {
  log "Destroy scope: service (${CLOUD_RUN_SERVICE_NAME} in ${GCP_REGION})"
  destroy_confirm "Delete Cloud Run service '${CLOUD_RUN_SERVICE_NAME}' in ${GCP_REGION}?" \
    || { log "Aborted."; return 0; }
  destroy_cloud_run_service "${CLOUD_RUN_SERVICE_NAME}" "${GCP_REGION}"
}

destroy_secret_by_name() {
  local secret_name="$1"
  if gcloud secrets describe "${secret_name}" --project="${GCP_PROJECT_ID}" >/dev/null 2>&1; then
    log "Deleting secret ${secret_name}"
    gcloud secrets delete "${secret_name}" --project="${GCP_PROJECT_ID}" --quiet
  fi
}

destroy_gcs_bucket_by_name() {
  local bucket_name="$1"
  local bucket_uri="gs://${bucket_name}"
  if ! gcloud storage buckets describe "${bucket_uri}" --project "${GCP_PROJECT_ID}" >/dev/null 2>&1; then
    return 0
  fi
  log "Deleting GCS bucket ${bucket_uri} (objects + bucket)"
  gcloud storage rm --recursive "${bucket_uri}" --quiet 2>/dev/null || true
  gcloud storage buckets delete "${bucket_uri}" --quiet
}

destroy_scope_tagged() {
  local filter list_filter
  filter="$(mill_deploy_list_filter)"
  list_filter="$(mill_deploy_run_list_filter)"
  local stack
  stack="$(mill_deploy_stack_id)"

  log "Destroy scope: tagged (labels ${MILL_LABEL_MANAGED_KEY}=${MILL_LABEL_MANAGED_VALUE}, ${MILL_LABEL_STACK_KEY}=${stack})"

  destroy_confirm "Delete ALL resources in project ${GCP_PROJECT_ID} with deploy labels for stack '${stack}'?" \
    || { log "Aborted."; return 0; }

  log "Cloud Run services"
  local name region
  while read -r name region; do
    [[ -z "${name}" ]] && continue
    destroy_cloud_run_service "${name}" "${region}"
  done < <(gcloud run services list \
    --project "${GCP_PROJECT_ID}" \
    --platform managed \
    --filter "${list_filter}" \
    --format='value(metadata.name,region)' 2>/dev/null || true)

  destroy_cloud_run_service "${CLOUD_RUN_SERVICE_NAME}" "${GCP_REGION}"

  log "Secret Manager secrets"
  local secret
  while read -r secret; do
    [[ -z "${secret}" ]] && continue
    destroy_secret_by_name "${secret}"
  done < <(gcloud secrets list \
    --project "${GCP_PROJECT_ID}" \
    --filter "${filter}" \
    --format='value(name)' 2>/dev/null || true)

  local base="mill-${CLOUD_RUN_SERVICE_NAME}"
  for secret in "${base}-db-password" "${base}-openai-key" "${base}-application-yml"; do
    destroy_secret_by_name "${secret}"
  done

  log "GCS buckets"
  local bucket
  while read -r bucket; do
    [[ -z "${bucket}" ]] && continue
    destroy_gcs_bucket_by_name "${bucket}"
  done < <(gcloud storage buckets list \
    --project "${GCP_PROJECT_ID}" \
    --filter "${filter}" \
    --format='value(name)' 2>/dev/null || true)

  if [[ -n "${GCS_BUCKET_NAME:-}" ]]; then
    local bucket_stack
    bucket_stack="$(gcloud storage buckets describe "gs://${GCS_BUCKET_NAME}" \
      --project "${GCP_PROJECT_ID}" \
      --format="value(labels.${MILL_LABEL_STACK_KEY})" 2>/dev/null || true)"
    if [[ "${bucket_stack}" == "$(mill_deploy_stack_id)" ]]; then
      destroy_gcs_bucket_by_name "${GCS_BUCKET_NAME}"
    else
      log "Skipping gs://${GCS_BUCKET_NAME} (not labeled for stack $(mill_deploy_stack_id))"
    fi
  fi

  log "Tagged resources removed for stack ${stack}"
}

destroy_scope_project() {
  log "Destroy scope: project (${GCP_PROJECT_ID})"
  destroy_confirm "DELETE ENTIRE GCP PROJECT '${GCP_PROJECT_ID}'? This cannot be undone." \
    || { log "Aborted."; return 0; }
  if [[ "${DESTROY_YES:-false}" != "true" ]]; then
    read -r -p "Type project id '${GCP_PROJECT_ID}' to confirm: " typed
    [[ "${typed}" == "${GCP_PROJECT_ID}" ]] || die "Project id mismatch — aborted"
  fi
  log "Deleting project ${GCP_PROJECT_ID} (async)"
  gcloud projects delete "${GCP_PROJECT_ID}" --quiet
  log "Project delete initiated for ${GCP_PROJECT_ID}"
}

run_destroy_scope() {
  local scope="${1:-${DESTROY_SCOPE:-service}}"
  case "${scope}" in
    service) destroy_scope_service ;;
    tagged) destroy_scope_tagged ;;
    project) destroy_scope_project ;;
    *)
      die "Unknown destroy scope '${scope}'. Use: service | tagged | project"
      ;;
  esac
}
