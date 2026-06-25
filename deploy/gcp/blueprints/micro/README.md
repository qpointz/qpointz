# Mill micro blueprint (GCP)

Terraform blueprint that deploys a **complete Mill stack** on Google Cloud: Cloud Run service, GCS bucket (config + Flow data), Cloud SQL PostgreSQL, VPC networking, Secret Manager, optional HTTP Basic auth, and optional OpenAI-backed AI/MCP.

Use this when you want a single `terraform apply` to stand up Mill against parquet/CSV/Avro/Excel data in GCS, with metadata seeds, UI, SQL/HTTP/OData APIs, and optional chat/MCP.

---

## Architecture

```text
                         ┌─────────────────────────────────────────┐
                         │              Cloud Run                   │
  Internet ─────────────►│  qpointz/mill-service-complete:<version> │
  (public invoker)       │  Spring profiles: cloud-run, backend-flow│
                         │    cloud-sql-pg, auth-basic?, ai?        │
                         └──────┬──────────────┬──────────┬────────┘
                                │              │          │
                    Secret Mgr  │         GCS  │          │ VPC
                    (configs) │    (data/)   │          │
                                ▼              ▼          ▼
                         /app/config/*   gs://data-*   Cloud SQL
                         application.yml  config/       PostgreSQL 16
                         flow.yml         data/<table>/  (private IP)
                         auth.yml?
```

| Component | Resource naming | Purpose |
|-----------|-----------------|---------|
| Cloud Run | `{deployment_name}-run-service` | Mill HTTP service (port 8080) |
| GCS bucket | `data-{deployment_name}` | Flow data (`data/`), config objects (`config/`) |
| Cloud SQL | `mill-db-{deployment_name}` | JPA/Flyway, AI pgvector when AI enabled |
| Runtime SA | `{deployment_name}-runtime` | Bucket read, secret access, Cloud SQL client |
| VPC | `{deployment_name}-vpc` | Private connectivity to Cloud SQL |
| Secrets | `{deployment_name}-config-file-*`, `{deployment_name}-db-password`, `{deployment_name}-ai-open-ai-key` | Mounted or env-injected config |

Reusable building blocks live under [`../../resources/`](../../resources/).

---

## Prerequisites

### Tools

- [Terraform](https://developer.hashicorp.com/terraform/downloads) **≥ 1.5** (or OpenTofu with equivalent provider support)
- [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) (`gcloud`) authenticated to your target project
- Billing enabled on the GCP project

### GCP permissions

The identity running Terraform needs permission to enable APIs and create:

- Cloud Run, Cloud SQL, VPC, Secret Manager, IAM service accounts, GCS buckets

A project **Owner** or **Editor** role is sufficient for lab deployments. Production setups should use a dedicated deployer SA with least privilege.

### Application credentials

- **Docker Hub**: the default image `qpointz/mill-service-complete:<mill_version>` is pulled by Cloud Run from Docker Hub (no extra Terraform config).
- **OpenAI** (optional): API key for `ai_enable = true`. Pass via `TF_VAR_ai_openai_key` rather than committing it to tfvars.

---

## Quick start

From this directory:

```bash
cd deploy/gcp/blueprints/micro

cp example.tfvars terraform.tfvars
# Edit terraform.tfvars: project_id, region, deployment_name, mill_version, data paths, auth passwords

export TF_VAR_ai_openai_key='sk-...'   # if ai_enable = true

terraform init
terraform plan  -var-file=terraform.tfvars
terraform apply -var-file=terraform.tfvars
```

First apply typically takes **10–20 minutes** (Cloud SQL + VPC peering dominate).

### Get the service URL

The blueprint does not yet expose `service_uri` as a root output. After apply:

```bash
gcloud run services describe "${DEPLOYMENT_NAME}-run-service" \
  --project="${PROJECT_ID}" \
  --region="${REGION}" \
  --format='value(status.url)'
```

Or read it from state:

```bash
terraform state show 'module.service.google_cloud_run_v2_service.mill' | grep uri
```

Replace `DEPLOYMENT_NAME`, `PROJECT_ID`, and `REGION` with your tfvars values.

---

## Configuration reference

All variables are declared in [`variables.tf`](variables.tf). [`example.tfvars`](example.tfvars) is a documented starting point derived from the internal [`test.tfvars`](test.tfvars) prototype.

### Required variables

| Variable | Description |
|----------|-------------|
| `project_id` | GCP project ID |
| `region` | Region for Cloud Run, GCS bucket, and related resources |
| `mill_version` | Mill release tag (image `qpointz/mill-service-complete:<tag>`, dashes removed from tag) |
| `ai_enable` | Enable AI Spring profile, OpenAI secret, and MCP endpoints |
| `ai_openai_key` | OpenAI API key (sensitive; use env var in production) |
| `ai_openai_model` | Chat model name (e.g. `gpt-4o-mini`) |
| `ai_openai_embed_model` | Embedding model (e.g. `text-embedding-3-small`) |
| `ai_openai_embed_dim` | Embedding vector dimension (e.g. `1536`) |
| `auth_basic_store` | User store: `file` (Secret Manager mount) or `jpa` (PostgreSQL) |

When `ai_enable = false`, you still must supply the AI variables in tfvars (use empty strings / dummy values) unless you add defaults in a wrapper module.

### Naming and labels

| Variable | Default | Description |
|----------|---------|-------------|
| `deployment_name` | `bp-micro-dep` | Prefix for Cloud Run service, bucket, secrets, runtime SA |
| `deployment_stack_name` | `bp-micro` | Stack label (`mill-stack`); used in application display name |

Resources are labeled with `mill-stack`, `mill-deployment`, and `mill-version`.

### Flow backend

| Variable | Default | Description |
|----------|---------|-------------|
| `backend` | `flow` | Data backend type (only Flow is implemented) |
| `schema_name` | `minimal` | Calcite/Flow schema name in `flow.yml` |
| `schema_cache_enabled` | `true` | Reuse resolved schemas across requests |
| `schema_cache_ttl` | `5m` | Cache TTL (Spring duration, e.g. `30s`, `1m`) |
| `backend_flow_config_as_secret` | `false` | `false` → `flow.yml` in GCS; `true` → Secret Manager volume mount |
| `flow_sample` | `null` | Map of table name → list of **local** file paths to upload to `gs://.../data/<table>/` |
| `metadata_seeds` | `[]` | List of local YAML paths copied to `gs://.../config/seeds/` |

**Data layout in GCS** (after upload):

```text
gs://data-<deployment_name>/
  config/
    seeds/          # metadata seed YAML (from metadata_seeds)
    flow/flow.yml   # Flow source config (when backend_flow_config_as_secret = false)
  data/
    <table>/        # parquet, csv, avro, xlsx — table name = folder name
      0_<file>.parquet
```

Flow reader patterns are defined in [`../../resources/backend-flow/config/flow.tpl.yml`](../../resources/backend-flow/config/flow.tpl.yml).

### Authentication

| Variable | Default | Description |
|----------|---------|-------------|
| `auth_enable` | `true` | Master security switch (`mill.security.enable`) |
| `auth_basic_enable` | `false` | HTTP Basic auth |
| `auth_basic_store` | — | `file` or `jpa` |
| `auth_basic_seed_users` | admin demo user | List of `{ user, password?, groups? }`; empty `password` generates a random password at apply time |

Passwords in tfvars are **bcrypt-hashed** during apply. Store generated passwords securely if you leave `password = ""`.

### Database

| Variable | Default | Description |
|----------|---------|-------------|
| `db` | `in-memory` | **Reserved** — Cloud SQL PostgreSQL is always provisioned today regardless of this value |

Cloud SQL instance: PostgreSQL 16, `db-f1-micro`, private IP only, database/user `mill`. Password is stored in Secret Manager (`{deployment_name}-db-password`) and injected as `MILL_DB_PASSWORD`.

### Cloud Run scaling

| Variable | Default |
|----------|---------|
| `service_min_instance_count` | `0` (scale to zero) |
| `service_max_instance_count` | `3` |
| `service_max_instance_request_concurrency` | `100` |
| `service_limits_cpu` | `"1"` |
| `service_limits_memory` | `"2Gi"` |

---

## Operator guide

### Verify deployment

```bash
URL=$(gcloud run services describe "${DEPLOYMENT_NAME}-run-service" \
  --project="${PROJECT_ID}" --region="${REGION}" --format='value(status.url)')

# Health (no auth)
curl -sS "${URL}/.well-known/mill"

# UI (browser)
echo "${URL}/"
```

With Basic auth enabled:

```bash
curl -sS -u 'admin:your-password' "${URL}/actuator/health"
```

### Using Mill after deploy

| Surface | Path | Notes |
|---------|------|-------|
| Web UI | `/` | Enabled via `mill.ui.enabled` |
| Health | `/.well-known/mill` | Cloud Run liveness probe |
| SQL / query HTTP API | `/services/query` (and related) | HTTP data services enabled |
| OData | OData endpoints | EDM cache TTL 2m |
| MCP (AI) | `/services/mcp` | When `ai_enable = true` |
| gRPC | — | Disabled in cloud-run profile |

Exact REST paths follow Mill’s standard service layout; use the UI or OpenAPI docs for your `mill_version`.

### Upload or refresh data

**At deploy time:** set `flow_sample` and `metadata_seeds` to local paths; Terraform uploads objects on apply.

**After deploy:** upload directly to the bucket (runtime SA has object viewer only on the bucket Terraform created):

```bash
BUCKET=$(terraform output -raw bucket_name)
gsutil cp local.parquet "gs://${BUCKET}/data/mytable/0_local.parquet"
```

Then either wait for schema cache TTL (`schema_cache_ttl`) or temporarily set a shorter TTL and redeploy.

### Rotate OpenAI key

```bash
gcloud secrets versions add "${DEPLOYMENT_NAME}-ai-open-ai-key" \
  --project="${PROJECT_ID}" \
  --data-file=- <<< 'sk-new-key'
```

Redeploy or restart Cloud Run so the service picks up `latest` (default secret version reference).

### View sensitive outputs

```bash
terraform output -json sql | jq .
```

Contains DB host, database name, username, and password (sensitive).

---

## Terraform outputs

| Output | Sensitive | Description |
|--------|-----------|-------------|
| `project` | no | GCP project ID |
| `region` | no | Deployed region |
| `bucket_name` | no | GCS bucket for config and Flow data |
| `sql` | yes | Cloud SQL connection details and credentials |

---

## Teardown

```bash
terraform destroy -var-file=terraform.tfvars
```

The GCS bucket uses `force_destroy = true` in the blueprint context, so bucket contents are deleted with the stack. Cloud SQL and VPC resources are removed when destroy succeeds.

---

## Troubleshooting

### Apply fails on API enablement

Ensure billing is active and your credentials can call `serviceusage.services.enable`. Re-run `terraform apply` after APIs propagate (usually under a minute).

### Cloud Run startup timeout

Startup probe allows ~100s before failure. Cloud SQL cold start + Flyway migrations on first boot can be slow. Check logs:

```bash
gcloud run services logs read "${DEPLOYMENT_NAME}-run-service" \
  --project="${PROJECT_ID}" --region="${REGION}" --limit=100
```

### No tables / empty schema

- Confirm objects exist under `gs://<bucket>/data/<table>/`.
- Table name must match the parent folder (see Flow regex in `flow.tpl.yml`).
- Supported extensions: `.parquet`, `.csv`, `.avro`, `.xlsx`.
- If schema cache is on, wait for TTL or lower `schema_cache_ttl` and re-apply.

### 401 on all requests

When `auth_enable` and `auth_basic_enable` are true, pass Basic credentials on every API call. Verify users in `auth_basic_seed_users` and remember empty passwords are randomly generated at apply time.

### AI / MCP errors

- Confirm `TF_VAR_ai_openai_key` or `ai_openai_key` is set and the secret version exists.
- AI uses pgvector in Cloud SQL; first startup runs migrations and embedding refresh (can take several minutes).

### `db = "in-memory"` has no effect

The `db` variable is not yet wired to module selection; Cloud SQL is always created. Use this blueprint when you need PostgreSQL (auth JPA store, AI embeddings, Flyway). For in-memory-only trials, see [`../../../docker/minimal-blueprint/`](../../../docker/minimal-blueprint/).

---

## File layout

```text
deploy/gcp/blueprints/micro/
  main.tf          # Provider + API enablement + GCS folder placeholders
  modules.tf       # Composes ../../resources/* modules
  locals.tf        # Wires Spring profiles, env, volumes across modules
  variables.tf     # Input variables
  output.tf        # Root outputs
  example.tfvars   # Documented example configuration (copy to terraform.tfvars)
  test.tfvars      # Internal/dev prototype (not for production; may contain secrets)
```

---

## Related

- [`../../../README.md`](../../../README.md) — deploy overview (legacy `gcp/cloud-run/`, Docker, Azure)
- [`../../../docker/minimal-blueprint/README.md`](../../../docker/minimal-blueprint/README.md) — local Docker equivalent with bind-mounted data
- [`../../resources/`](../../resources/) — shared Terraform modules (Cloud Run, Flow, auth, AI, Cloud SQL)
