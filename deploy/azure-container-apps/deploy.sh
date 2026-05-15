#!/usr/bin/env bash
# One-shot: build mill-service (complete edition), push to ACR, deploy Azure Container App.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

log() { printf '\n==> %s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "Missing required command: $1"
}

# shellcheck disable=SC1090
load_env() {
  if [[ -f "${ENV_FILE}" ]]; then
    set -a
    # shellcheck source=/dev/null
    source "${ENV_FILE}"
    set +a
  elif [[ -f "${SCRIPT_DIR}/env.example" ]]; then
    die "Create ${ENV_FILE} from env.example and set secrets (DB, Azure storage)."
  else
    die "Missing ${ENV_FILE}"
  fi
}

az_login() {
  require_cmd az
  if [[ -n "${AZURE_SUBSCRIPTION_ID:-}" ]]; then
    az account set --subscription "${AZURE_SUBSCRIPTION_ID}"
  fi
  az account show >/dev/null || die "Run: az login"
}

ensure_resource_group() {
  if ! az group show --name "${AZURE_RESOURCE_GROUP}" >/dev/null 2>&1; then
    log "Creating resource group ${AZURE_RESOURCE_GROUP} in ${AZURE_LOCATION}"
    az group create --name "${AZURE_RESOURCE_GROUP}" --location "${AZURE_LOCATION}" --output none
  fi
}

ensure_acr() {
  if ! az acr show --name "${AZURE_ACR_NAME}" --resource-group "${AZURE_RESOURCE_GROUP}" >/dev/null 2>&1; then
    log "Creating Azure Container Registry ${AZURE_ACR_NAME}"
    az acr create \
      --resource-group "${AZURE_RESOURCE_GROUP}" \
      --name "${AZURE_ACR_NAME}" \
      --sku Basic \
      --admin-enabled true \
      --output none
  fi
}

maybe_deploy_postgres() {
  if [[ "${DEPLOY_POSTGRES:-false}" != "true" ]]; then
    return 0
  fi
  local server="${POSTGRES_SERVER_NAME}"
  if az postgres flexible-server show --name "${server}" --resource-group "${AZURE_RESOURCE_GROUP}" >/dev/null 2>&1; then
    log "PostgreSQL server ${server} already exists"
    return 0
  fi
  log "Creating PostgreSQL Flexible Server ${server} (this can take several minutes)"
  az postgres flexible-server create \
    --resource-group "${AZURE_RESOURCE_GROUP}" \
    --name "${server}" \
    --location "${AZURE_LOCATION}" \
    --admin-user "${POSTGRES_ADMIN_USER}" \
    --admin-password "${POSTGRES_ADMIN_PASSWORD}" \
    --sku-name Standard_B1ms \
    --tier Burstable \
    --storage-size 32 \
    --version 16 \
    --public-access 0.0.0.0 \
    --yes \
    --output none
  az postgres flexible-server db create \
    --resource-group "${AZURE_RESOURCE_GROUP}" \
    --server-name "${server}" \
    --database-name "${POSTGRES_DB_NAME}" \
    --output none
  MILL_DB_URL="jdbc:postgresql://${server}.postgres.database.azure.com:5432/${POSTGRES_DB_NAME}?sslmode=require"
  MILL_DB_USERNAME="${POSTGRES_ADMIN_USER}"
  MILL_DB_PASSWORD="${POSTGRES_ADMIN_PASSWORD}"
  export MILL_DB_URL MILL_DB_USERNAME MILL_DB_PASSWORD
}

build_and_push_image() {
  require_cmd docker
  local edition="${MILL_APP_EDITION:-complete}"
  local image_name="${AZURE_ACR_NAME}.azurecr.io/${AZURE_CONTAINER_APP_NAME}:${MILL_IMAGE_TAG:-latest}"

  log "Building mill-service installBootDist (edition=${edition})"
  (
    cd "${REPO_ROOT}"
    ./gradlew ":apps:mill-service:installBootDist" -Pedition="${edition}" --no-daemon
  )

  log "Building Docker image ${image_name}"
  docker buildx build \
    --platform linux/amd64 \
    --build-arg "APP_EDITION=${edition}" \
    -f "${REPO_ROOT}/apps/mill-service/src/main/docker/base/Dockerfile" \
    -t "${image_name}" \
    "${REPO_ROOT}/apps/mill-service"

  log "Pushing image to ACR"
  az acr login --name "${AZURE_ACR_NAME}"
  docker push "${image_name}"
  export MILL_IMAGE="${image_name}"
}

ensure_container_apps_env() {
  if ! az containerapp env show --name "${AZURE_ACA_ENV_NAME}" --resource-group "${AZURE_RESOURCE_GROUP}" >/dev/null 2>&1; then
    log "Creating Container Apps environment ${AZURE_ACA_ENV_NAME}"
    az containerapp env create \
      --name "${AZURE_ACA_ENV_NAME}" \
      --resource-group "${AZURE_RESOURCE_GROUP}" \
      --location "${AZURE_LOCATION}" \
      --output none
  fi
}

register_secrets() {
  local secret_args=(
    "mill-db-password=${MILL_DB_PASSWORD}"
  )
  if [[ -n "${MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING:-}" ]]; then
    secret_args+=("mill-azure-cs=${MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING}")
  fi
  if [[ -n "${OPENAI_API_KEY:-}" ]]; then
    secret_args+=("openai-api-key=${OPENAI_API_KEY}")
  fi
  az containerapp secret set \
    --name "${AZURE_CONTAINER_APP_NAME}" \
    --resource-group "${AZURE_RESOURCE_GROUP}" \
    --secrets "${secret_args[@]}" \
    --output none
}

build_env_vars() {
  local profiles="${SPRING_PROFILES_ACTIVE:-skymill-azure,postgres,secure}"
  ENV_VARS=(
    "SPRING_PROFILES_ACTIVE=${profiles}"
    "MILL_DB_URL=${MILL_DB_URL}"
    "MILL_DB_USERNAME=${MILL_DB_USERNAME}"
    "MILL_DB_PASSWORD=secretref:mill-db-password"
  )
  if [[ -n "${MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING:-}" ]]; then
    ENV_VARS+=("MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING=secretref:mill-azure-cs")
  fi
  if [[ -n "${MILL_CLOUD_AZURE_ADLS_BLOB_SERVICE_ENDPOINT:-}" ]]; then
    ENV_VARS+=("MILL_CLOUD_AZURE_ADLS_BLOB_SERVICE_ENDPOINT=${MILL_CLOUD_AZURE_ADLS_BLOB_SERVICE_ENDPOINT}")
  fi
  if [[ -n "${OPENAI_API_KEY:-}" ]]; then
    ENV_VARS+=("OPENAI_API_KEY=secretref:openai-api-key")
  fi
}

deploy_container_app() {
  build_env_vars
  local ingress="${ACA_INGRESS_EXTERNAL:-true}"
  local target_port="${ACA_TARGET_PORT:-8080}"

  if az containerapp show --name "${AZURE_CONTAINER_APP_NAME}" --resource-group "${AZURE_RESOURCE_GROUP}" >/dev/null 2>&1; then
    log "Updating Container App ${AZURE_CONTAINER_APP_NAME}"
    register_secrets
    az containerapp update \
      --name "${AZURE_CONTAINER_APP_NAME}" \
      --resource-group "${AZURE_RESOURCE_GROUP}" \
      --image "${MILL_IMAGE}" \
      --set-env-vars "${ENV_VARS[@]}" \
      --output none
  else
    log "Creating Container App ${AZURE_CONTAINER_APP_NAME}"
    local admin_user admin_pass
    admin_user="$(az acr credential show --name "${AZURE_ACR_NAME}" --query username -o tsv)"
    admin_pass="$(az acr credential show --name "${AZURE_ACR_NAME}" --query 'passwords[0].value' -o tsv)"

    local create_secrets=(
      "mill-db-password=${MILL_DB_PASSWORD}"
    )
    [[ -n "${MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING:-}" ]] && create_secrets+=("mill-azure-cs=${MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING}")
    [[ -n "${OPENAI_API_KEY:-}" ]] && create_secrets+=("openai-api-key=${OPENAI_API_KEY}")

    az containerapp create \
      --name "${AZURE_CONTAINER_APP_NAME}" \
      --resource-group "${AZURE_RESOURCE_GROUP}" \
      --environment "${AZURE_ACA_ENV_NAME}" \
      --image "${MILL_IMAGE}" \
      --registry-server "${AZURE_ACR_NAME}.azurecr.io" \
      --registry-username "${admin_user}" \
      --registry-password "${admin_pass}" \
      --cpu "${ACA_CPU:-1.0}" \
      --memory "${ACA_MEMORY:-2.0Gi}" \
      --min-replicas "${ACA_MIN_REPLICAS:-1}" \
      --max-replicas "${ACA_MAX_REPLICAS:-1}" \
      --ingress "${ingress}" \
      --target-port "${target_port}" \
      --secrets "${create_secrets[@]}" \
      --env-vars "${ENV_VARS[@]}" \
      --output none
  fi

  if [[ "${ACA_MANAGED_IDENTITY:-false}" == "true" ]]; then
    log "Enabling system-assigned managed identity (grant Storage Blob Data Reader on your account)"
    az containerapp identity assign \
      --name "${AZURE_CONTAINER_APP_NAME}" \
      --resource-group "${AZURE_RESOURCE_GROUP}" \
      --system-assigned \
      --output none || true
  fi

  local fqdn
  fqdn="$(az containerapp show \
    --name "${AZURE_CONTAINER_APP_NAME}" \
    --resource-group "${AZURE_RESOURCE_GROUP}" \
    --query 'properties.configuration.ingress.fqdn' -o tsv)"
  log "Deployed. URL: https://${fqdn}"
  log "Health:  https://${fqdn}/actuator/health"
  log "If OAuth is enabled, register redirect URI: https://${fqdn}/login/oauth2/code/authentik"
}

validate_config() {
  [[ -n "${MILL_DB_URL:-}" ]] || die "Set MILL_DB_URL in .env"
  [[ -n "${MILL_DB_USERNAME:-}" ]] || die "Set MILL_DB_USERNAME in .env"
  [[ -n "${MILL_DB_PASSWORD:-}" ]] || die "Set MILL_DB_PASSWORD in .env"
  if [[ -z "${MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING:-}" && -z "${MILL_CLOUD_AZURE_ADLS_BLOB_SERVICE_ENDPOINT:-}" ]]; then
    die "Set MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING or MILL_CLOUD_AZURE_ADLS_BLOB_SERVICE_ENDPOINT (with managed identity)"
  fi
}

main() {
  load_env
  validate_config
  az_login
  ensure_resource_group
  maybe_deploy_postgres
  ensure_acr
  build_and_push_image
  ensure_container_apps_env
  deploy_container_app
}

main "$@"
