# Azure Container Apps — one-shot Mill deployment

Deploy **mill-service** (Gradle edition `complete`, Spring profiles such as `skymill-azure,postgres,secure`) to [Azure Container Apps](https://learn.microsoft.com/azure/container-apps/) with a single script.

## Prerequisites

- [Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli) (`az login`)
- [Docker](https://docs.docker.com/get-docker/) with `buildx`
- JDK 21 + repo build (script runs `./gradlew :apps:mill-service:installBootDist`)
- An **Azure Storage** account with Skymill config blobs (`skymill-flow-azure.yml`, seeds, etc.) if using profile `skymill-azure`
- **PostgreSQL** reachable from Azure (existing Flexible Server or `DEPLOY_POSTGRES=true` in `.env`)

## Quick start

```bash
cd deploy/azure-container-apps
cp env.example .env
# Edit .env: ACR name (globally unique), DB URL, Azure storage connection string

chmod +x deploy.sh destroy.sh
./deploy.sh
```

On success the script prints the HTTPS URL and `/actuator/health`.

## What `deploy.sh` does

1. Creates resource group (if missing)
2. Optionally creates a small PostgreSQL Flexible Server (`DEPLOY_POSTGRES=true`)
3. Creates ACR (if missing)
4. Builds `mill-service-boot-<edition>` and Docker image (`apps/mill-service/src/main/docker/base/Dockerfile`)
5. Pushes image to ACR
6. Creates Container Apps environment + Container App with env-based secrets

## Configuration (`.env`)

| Variable | Purpose |
|----------|---------|
| `AZURE_RESOURCE_GROUP`, `AZURE_LOCATION` | Azure placement |
| `AZURE_ACR_NAME` | Registry name (globally unique, lowercase) |
| `AZURE_CONTAINER_APP_NAME` | App name |
| `MILL_APP_EDITION` | Gradle edition (default `complete` — includes Azure blob + flow) |
| `SPRING_PROFILES_ACTIVE` | e.g. `skymill-azure,postgres,secure` |
| `MILL_DB_*` | JDBC URL and credentials for profile `postgres` |
| `MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING` | Overrides packaged YAML; required for `azure-blob://` config URLs unless using MI + endpoint |
| `MILL_CLOUD_AZURE_ADLS_BLOB_SERVICE_ENDPOINT` | With `ACA_MANAGED_IDENTITY=true` and Storage RBAC instead of connection string |

**Secrets:** Do not commit `.env`. The packaged `skymill-azure` profile in `application.yml` may contain dev values — **ACA env vars take precedence**; always set `MILL_CLOUD_AZURE_ADLS_CONNECTION_STRING` (or endpoint + managed identity) in `.env` for production.

## Profiles

| Profile | Role |
|---------|------|
| `skymill-azure` | Flow + metadata seeds from `azure-blob://…` |
| `postgres` | JPA metadata + Flyway on PostgreSQL |
| `secure` | Enables Mill security |
| `oauth` | OIDC (configure issuer + redirect URI for the ACA FQDN before enabling) |

Minimal Azure Skymill (no OAuth):

```bash
SPRING_PROFILES_ACTIVE=skymill-azure,postgres,secure
```

## Managed identity (optional)

1. Set `MILL_CLOUD_AZURE_ADLS_BLOB_SERVICE_ENDPOINT=https://<account>.blob.core.windows.net`
2. Leave connection string empty
3. Set `ACA_MANAGED_IDENTITY=true`
4. After deploy, grant the container app identity **Storage Blob Data Reader** on the storage account

## OAuth on ACA

After the first deploy, note the FQDN and configure your IdP redirect URI, for example:

`https://<fqdn>/login/oauth2/code/authentik`

Set `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AUTHENTIK_CLIENT_ID` / `CLIENT_SECRET` and issuer env vars (or mount `config/application-aca.yml`).

## Cleanup

```bash
./destroy.sh   # deletes the entire resource group from .env
```

## Related

- Local Postgres: `make -C deploy local-dev-start`
- Docker image only: `make -C apps build-docker` (uses edition from Gradle default)
- AKS Terraform (full cluster): `misc/infra/terraform/azure-mill-aks/`
- Cloud config loading limits: `docs/public/src/reference/cloud-resource-loading.md`
