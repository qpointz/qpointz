# Google Cloud Run — one-shot Mill deployment

Deploy **mill-service** to [Google Cloud Run](https://cloud.google.com/run) using a **Docker Hub** image (no local build). Optionally mount a custom `application.yml` via Secret Manager.

## Prerequisites

- [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) (`gcloud auth login`)
- Billing enabled on your GCP project
- **PostgreSQL** reachable from Cloud Run (Cloud SQL, AlloyDB, or external)
- A published image on Docker Hub, e.g. `qpointz/mill-service-complete:<version>` from release CI
- For `gs://` config URLs: GCS buckets with Skymill YAML; grant the Cloud Run service account **Storage Object Viewer**

## Quick start

```bash
cd deploy/google-cloud-run
cp env.example .env
# Edit .env: GCP_PROJECT_ID, MILL_DOCKER_IMAGE, MILL_DB_*

chmod +x deploy.sh destroy.sh
./deploy.sh
```

On success the script prints the service URL and `/actuator/health`.

## What `deploy.sh` does

1. Validates `.env` and active `gcloud` credentials
2. **Optionally creates** the GCP project (`CREATE_GCP_PROJECT=true`) and **GCS bucket** (`GCS_BUCKET_NAME`)
3. **Optionally syncs** local files to the bucket (`GCS_SYNC_ENABLED=true`, mappings in `config/gcs-sync.conf`)
4. Optionally uploads `CUSTOM_APPLICATION_YML` to Secret Manager and mounts it at **`/mill-config/application.yml`** (not under `/config`: the Mill image declares `VOLUME /config`, which would hide a secret mount there on Cloud Run)
5. Stores DB password (and optional OpenAI key) in Secret Manager
6. Deploys (or updates) the Cloud Run service from `MILL_DOCKER_IMAGE`

**No image build** — point `MILL_DOCKER_IMAGE` at an existing tag on Docker Hub.

### GCS + Skymill quick path

```bash
cp env.example .env
# Set GCP_PROJECT_ID, GCS_BUCKET_NAME, MILL_DOCKER_IMAGE, MILL_DB_*
# GCS_SYNC_ENABLED=true
# CUSTOM_APPLICATION_YML=config/application-cloudrun-gcs.yml
# SPRING_PROFILES_ACTIVE=skymill-gcs,postgres,secure

./deploy.sh
```

Sync only (no Cloud Run deploy):

```bash
./sync-bucket.sh
```

## Configuration (`.env`)

| Variable | Purpose |
|----------|---------|
| `GCP_PROJECT_ID`, `GCP_REGION` | Target project and region |
| `CREATE_GCP_PROJECT` | Create project if missing (`GCP_BILLING_ACCOUNT_ID` required) |
| `GCP_FOLDER_ID` | Optional **numeric** folder id, or `folders/123456789` (not a project name) |
| `GCP_ORGANIZATION_ID` | Optional **numeric** org id, or `organizations/123456789` (use instead of folder) |
| `GCS_BUCKET_NAME` | Config bucket (created if missing) |
| `GCS_SYNC_ENABLED` | Run `config/gcs-sync.conf` uploads before deploy |
| `GCS_SYNC_CONFIG` | Path to sync mapping file (default `config/gcs-sync.conf`) |
| `CLOUD_RUN_SERVICE_NAME` | Cloud Run service name |
| `MILL_DOCKER_IMAGE` | Full image ref, e.g. `docker.io/qpointz/mill-service-complete:1.2.3` |
| `SPRING_PROFILES_ACTIVE` | e.g. `skymill,postgres,secure` |
| `MILL_DB_*` | JDBC URL and credentials for profile `postgres` |
| `CUSTOM_APPLICATION_YML` | Local file path mounted via Secret Manager at `/mill-config/application.yml` |
| `CLOUD_SQL_CONNECTION_NAME` | Optional `project:region:instance` for Cloud SQL socket |
| `MILL_CLOUD_GCP_GCS_PROJECT_ID` | Optional GCS project for `mill.cloud.gcp.gcs.project-id` |
| `CLOUD_RUN_SERVICE_ACCOUNT` | Optional dedicated SA with GCS/Secret access |

**Secrets:** Do not commit `.env`. DB password and API keys are stored in Secret Manager, not plain env vars on the revision.

## Custom `application.yml`

Use this when you need GCS-backed Skymill config (`gs://…` flow descriptors and seeds), OAuth settings, or any overlay beyond env vars.

1. Copy and edit the sample:

   ```bash
   cp config/application-cloudrun.yml config/my-application.yml
   ```

2. In `.env`:

   ```bash
   CUSTOM_APPLICATION_YML=config/my-application.yml
   ```

3. Run `./deploy.sh`

The script sets `SPRING_CONFIG_ADDITIONAL_LOCATION=file:/mill-config/application.yml`. Spring merges this file **after** the image’s packaged config; environment variables still override individual properties.

Use the bundled overlay `config/application-cloudrun-gcs.yml` (profile `skymill-gcs`, `gs://${GCS_BUCKET_NAME}/…` URLs). Set `GCS_BUCKET_NAME` in `.env` — deploy passes it as an env var to Cloud Run.

### `config/gcs-sync.conf` format

One mapping per line: `local_path | gcs_path_under_bucket`

```
apps/mill-service/config/skymill/skymill-flow.yml | skymill/config/skymill-flow.yml
test/datasets/skymill/skymill-canonical.yaml | skymill/config/skymill-canonical.yaml
```

Paths are relative to the **repository root** unless absolute. Directories use recursive `gcloud storage rsync` when the local path is a folder (trailing `/` recommended).

## Docker Hub images

Release CI publishes `qpointz/mill-service-<edition>:<version>`. For cloud + Skymill Azure/GCP modules use edition **complete**:

```bash
MILL_DOCKER_IMAGE=docker.io/qpointz/mill-service-complete:latest
```

Public images need no registry credentials on Cloud Run.

## Cloud SQL

1. Set `CLOUD_SQL_CONNECTION_NAME=project:region:instance`
2. Use a JDBC URL with the Cloud SQL socket factory, for example:

   ```bash
   MILL_DB_URL=jdbc:postgresql:///mill?cloudSqlInstance=project:region:instance&socketFactory=com.google.cloud.sql.postgres.SocketFactory
   ```

3. Grant the Cloud Run service account **Cloud SQL Client** on the instance

## GCS config loading

On Cloud Run, `mill.cloud.gcp.gcs` uses **Application Default Credentials** (the runtime service account). Set `MILL_CLOUD_GCP_GCS_PROJECT_ID` if needed and grant **Storage Object Viewer** on config buckets. See [Cloud resource loading](../../docs/public/src/reference/cloud-resource-loading.md).

## Profiles

| Profile | Role |
|---------|------|
| `skymill` | Flow backend + classpath/local Skymill paths (image default) |
| `skymill-azure` | Azure blob config URLs (packaged in image; prefer env + custom YAML on GCP) |
| `postgres` | JPA metadata + Flyway on PostgreSQL |
| `secure` | Enables Mill security |
| `oauth` | OIDC — set issuer/redirect for the Cloud Run URL |

## Cleanup

`destroy.sh` supports three scopes. All resources created by `deploy.sh` are labeled:

| Label | Value |
|-------|--------|
| `mill-managed-by` | `google-cloud-run` |
| `mill-deploy-stack` | `MILL_DEPLOY_STACK_ID` or `CLOUD_RUN_SERVICE_NAME` |

```bash
./destroy.sh service    # Cloud Run service only (default)
./destroy.sh tagged     # Labeled Cloud Run + secrets + GCS bucket for this stack
./destroy.sh project    # Delete entire GCP project (destructive)

./destroy.sh tagged -y  # Non-interactive
# or: DESTROY_SCOPE=tagged DESTROY_YES=true ./destroy.sh
```

| Scope | Deletes |
|-------|---------|
| `service` | `CLOUD_RUN_SERVICE_NAME` in `GCP_REGION` |
| `tagged` | All resources with deploy labels for this stack (+ known `mill-<service>-*` secrets) |
| `project` | Whole `GCP_PROJECT_ID` (use only for throwaway projects) |

## Related

- Azure Container Apps: `deploy/azure-container-apps/`
- `make -C deploy gcp-cloudrun-deploy`
- Cloud config limits: `docs/public/src/reference/cloud-resource-loading.md`
