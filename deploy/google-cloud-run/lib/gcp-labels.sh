# Mill Cloud Run deploy labels (GCP resource labels).
# Sourced by gcp-provision.sh, deploy.sh, destroy.sh.

# shellcheck disable=SC2034
MILL_LABEL_MANAGED_KEY=mill-managed-by
MILL_LABEL_MANAGED_VALUE=google-cloud-run
MILL_LABEL_STACK_KEY=mill-deploy-stack

mill_deploy_stack_id() {
  printf '%s' "${MILL_DEPLOY_STACK_ID:-${CLOUD_RUN_SERVICE_NAME:-mill}}"
}

mill_deploy_labels_csv() {
  printf '%s=%s,%s=%s' \
    "${MILL_LABEL_MANAGED_KEY}" "${MILL_LABEL_MANAGED_VALUE}" \
    "${MILL_LABEL_STACK_KEY}" "$(mill_deploy_stack_id)"
}

mill_deploy_list_filter() {
  printf 'labels.%s=%s AND labels.%s=%s' \
    "${MILL_LABEL_MANAGED_KEY}" "${MILL_LABEL_MANAGED_VALUE}" \
    "${MILL_LABEL_STACK_KEY}" "$(mill_deploy_stack_id)"
}

mill_deploy_run_list_filter() {
  printf 'metadata.labels.%s=%s AND metadata.labels.%s=%s' \
    "${MILL_LABEL_MANAGED_KEY}" "${MILL_LABEL_MANAGED_VALUE}" \
    "${MILL_LABEL_STACK_KEY}" "$(mill_deploy_stack_id)"
}

mill_tag_project() {
  [[ -n "${GCP_PROJECT_ID:-}" ]] || return 0
  gcloud projects update "${GCP_PROJECT_ID}" \
    --update-labels="$(mill_deploy_labels_csv)" \
    --quiet >/dev/null 2>&1 || true
}

mill_tag_gcs_bucket() {
  local bucket_name="$1"
  [[ -n "${bucket_name}" ]] || return 0
  gcloud storage buckets update "gs://${bucket_name}" \
    --project="${GCP_PROJECT_ID}" \
    --update-labels="$(mill_deploy_labels_csv)" \
    --quiet >/dev/null 2>&1 || true
}

mill_tag_secret() {
  local secret_name="$1"
  [[ -n "${secret_name}" ]] || return 0
  gcloud secrets update "${secret_name}" \
    --project="${GCP_PROJECT_ID}" \
    --update-labels="$(mill_deploy_labels_csv)" \
    --quiet >/dev/null 2>&1 || true
}

# Tag well-known secrets for this stack (by naming convention).
mill_tag_stack_secrets() {
  local base="mill-${CLOUD_RUN_SERVICE_NAME:-}"
  [[ -n "${base}" && "${base}" != "mill-" ]] || return 0
  local secret
  for secret in \
    "${base}db-password" \
    "${base}openai-key" \
    "${base}application-yml"; do
    if gcloud secrets describe "${secret}" --project="${GCP_PROJECT_ID}" >/dev/null 2>&1; then
      mill_tag_secret "${secret}"
    fi
  done
}

mill_tag_all_deploy_resources() {
  mill_tag_project
  if [[ -n "${GCS_BUCKET_NAME:-}" ]]; then
    mill_tag_gcs_bucket "${GCS_BUCKET_NAME}"
  fi
  mill_tag_stack_secrets
}
