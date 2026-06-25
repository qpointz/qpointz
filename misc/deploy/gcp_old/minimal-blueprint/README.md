# Mill minimal blueprint (GCP Cloud Run)

OpenTofu root module that deploys a **minimal Mill service** on **Google Cloud Run** with:

- a dedicated **GCS bucket** for query data,
- **Flow** backend reading Parquet, CSV, Avro, and Excel from that bucket,
- Mill **application**, **flow**, and **auth** YAML rendered from templates into **Secret Manager** and mounted into the container,
- a runtime **service account** with bucket read access and secret access (plus **serverless robot** secret access so Cloud Run can mount volumes).

This stack is intentionally smaller than [`../cloud-run/`](../cloud-run/README.md): no PostgreSQL, no OpenAI, no Skymill seed sync scripts. Use it to try Mill on GCP with file-backed data only.

---

## What gets created

For `deployment_name = "mill-minimal-blpr"` (default):

| Resource | Name / pattern |
|----------|----------------|
| Cloud Run service | `{deployment_name}-run-service` |
| GCS bucket | `{deployment_name}-bucket` |
| Runtime service account | `{deployment_name}-runtime@…` |
| Secret Manager secrets | `{deployment_name}-application-yml`, `-flow-yml`, `-auth-yml` |

**APIs enabled:** Cloud Run, Secret Manager, Cloud Storage, Cloud Resource Manager, IAM.

**Container image:** `var.image_version` (default `qpointz/mill-service-minimal:latest` in [`variables.tf`](variables.tf)).

**Ingress:** public HTTPS when `allow_unauthenticated = true` (default) — `roles/run.invoker` for `allUsers`. Set `allow_unauthenticated = false` for IAM-only invoke.

---

## Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│  Cloud Run ({deployment_name}-run-service)                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  mill-service-minimal container                       │  │
│  │  /app/config/application.yml  ← Secret (application)  │  │
│  │  /app/config/flow/flow.yml    ← Secret (flow)         │  │
│  │  /app/config/auth/auth.yml    ← Secret (auth)         │  │
│  └───────────────────────────────────────────────────────┘  │
│         │ ADC (runtime SA)                                  │
└─────────┼───────────────────────────────────────────────────┘
          │
          ▼
   GCS bucket  gs://{deployment}-bucket/data/{table}/…
```

Flow descriptor ([`config/flow.tpl.yml`](config/flow.tpl.yml)) scans the `data/` prefix in the bucket. Each **table** is the **parent folder** of a file; the **reader** is chosen by extension:

```text
gs://…/data/cities/cities.parquet   →  schema {schema_name}, table `cities` (parquet)
gs://…/data/orders/orders.csv       →  schema {schema_name}, table `orders` (csv)
```

Regex pattern (per reader): `(?<table>[^/]+)/[^/]+\.{ext}$` — do **not** prefix with greedy `.*` before the capture group (that breaks names like `cities/cities.parquet`).

GCS auth uses **ambient credentials** (`preferAmbientCredentials: true`) — the Cloud Run runtime service account must have `roles/storage.objectViewer` on the bucket (granted by this module).

---

## Prerequisites

- [OpenTofu](https://opentofu.org/) or Terraform **≥ 1.6**
- An existing GCP **project** with billing enabled
- Permission to enable APIs, create Cloud Run services, buckets, secrets, and service accounts
- Authentication:

  ```bash
  gcloud auth application-default login
  # or export GOOGLE_APPLICATION_CREDENTIALS=/path/to/sa.json
  ```

- [`gcloud`](https://cloud.google.com/sdk) and [`gsutil`](https://cloud.google.com/storage/docs/gsutil) for upload and URL lookup

---

## Quick start

```bash
cd deploy/gcp/minimal-blueprint

cp terraform.tfvars.example terraform.tfvars
# Edit project_id, region, and optionally image_version / schema_name

tofu init
tofu plan -var-file=terraform.tfvars
tofu apply -var-file=terraform.tfvars
```

`terraform.tfvars` is gitignored (see repo root `.gitignore`). Do not commit it.

See [`terraform.tfvars.example`](terraform.tfvars.example) for all inputs and commented defaults.

### 1. Upload sample data

```bash
BUCKET=$(tofu output -raw gcs_bucket_name)
PREFIX=$(tofu output -raw gcs_data_prefix)

# Parquet example → table "cities"
gsutil cp ./local/cities.parquet "gs://${BUCKET}/${PREFIX}cities/cities.parquet"

# CSV example (semicolon delimiter matches flow.tpl.yml) → table "orders"
gsutil cp ./local/orders.csv "gs://${BUCKET}/${PREFIX}orders/orders.csv"
```

### 2. Health check

```bash
curl -sS "$(tofu output -raw health_check_url)"
```

Service URL (for JDBC / HTTP clients):

```bash
tofu output -raw cloud_run_uri
```

Flow schema name:

```bash
tofu output -raw flow_schema_name
```

Default **`minimal`** unless you set `schema_name` in tfvars.

---

## Configuration templates

Terraform renders these into Secret Manager on each apply:

| Template | Mount path | Purpose |
|----------|------------|---------|
| [`config/application.tpl.yml`](config/application.tpl.yml) | `/app/config/application.yml` | Mill app: Flow backend, UI off, gRPC off, configurable Flow **schema** cache; `mill.security.enable: false` by default |
| [`config/flow.tpl.yml`](config/flow.tpl.yml) | `/app/config/flow/flow.yml` | GCS storage + multi-format readers |
| [`config/auth.tpl.yml`](config/auth.tpl.yml) | `/app/config/auth/auth.yml` | Basic-auth user store (mounted; inactive until security is enabled) |

Mounts use **separate paths** (`/app/config/`, `/app/config/flow/`, `/app/config/auth/`) because Cloud Run does not allow two volume mounts on the same `mount_path`.

### Customizing config

1. Edit the relevant `config/*.tpl.yml`.
2. Run `tofu apply -var-file=terraform.tfvars`.
3. Trigger a **new Cloud Run revision** so mounted secrets reload (e.g. bump a label on the service, or `gcloud run services update … --region …`).

Template variables (passed from [`main.tf`](main.tf) via `templatefile`):

**`config/flow.tpl.yml`**

| Template arg | Source | Meaning |
|--------------|--------|---------|
| `schema_name` | `var.schema_name` | Flow / Calcite schema name |
| `bucket_name` | created bucket | GCS bucket for data |
| `project_id` | `var.project_id` | GCP project for GCS client |

**`config/application.tpl.yml`**

| Template arg | Source | YAML path | Meaning |
|--------------|--------|-----------|---------|
| `schema_cache_enabled` | `var.schema_cache_enabled` | `mill.data.backend.flow.cache.schema.enabled` | Reuse resolved Flow schemas across requests |
| `schema_cache_ttl` | `var.schema_cache_ttl` | `mill.data.backend.flow.cache.schema.ttl` | Expiry for schema cache (e.g. `5m`, `30s`) |

Facet inference cache (`cache.facets` in the same file) stays fixed at `enabled: true`, `ttl: 30s` — tune in `application.tpl.yml` if needed.

Change `schema_name`, `schema_cache_enabled`, or `schema_cache_ttl` in `terraform.tfvars` without editing `main.tf`.

### Enabling HTTP Basic auth

[`config/auth.tpl.yml`](config/auth.tpl.yml) ships a **demo** user (`admin` / `{noop}admin`). Replace before any shared environment.

Basic auth is only enforced when Mill security is enabled. In [`config/application.tpl.yml`](config/application.tpl.yml), set:

```yaml
mill:
  security:
    enable: true
    authentication:
      basic:
        enable: true
        store: file:/app/config/auth/auth.yml
```

Then update `auth.tpl.yml` with your users (prefer `{bcrypt}…` or another delegating prefix, not `{noop}` in production).

---

## Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `project_id` | yes | — | GCP project ID |
| `region` | yes | — | Cloud Run region (bucket location matches) |
| `deployment_name` | no | `mill-minimal-blpr` | Prefix for resource names |
| `image_version` | no | `qpointz/mill-service-minimal:latest` | Container image |
| `schema_name` | no | `minimal` | Flow schema name in `flow.tpl.yml` |
| `schema_cache_enabled` | no | `true` | Flow schema cache on/off (`application.tpl.yml`) |
| `schema_cache_ttl` | no | `"5m"` | Schema cache TTL (Spring duration string) |
| `service_max_instance_request_concurrency` | no | `100` | Per-instance concurrency |
| `service_min_instance_count` | no | `0` | Min instances (0 = scale to zero) |
| `service_max_instance_count` | no | `3` | Max instances |
| `service_limits_cpu` | no | `"0.5"` | CPU limit string |
| `service_limits_memory` | no | `"1Gi"` | Memory limit |
| `allow_unauthenticated` | no | `true` | Public `run.invoker` for `allUsers` |
| `gcs_force_destroy` | no | `true` | Empty bucket on `tofu destroy` |

Full descriptions: [`variables.tf`](variables.tf). Outputs: [`outputs.tf`](outputs.tf).

---

## Security notes (demo defaults)

| Setting | Default | Action for production |
|---------|---------|------------------------|
| Cloud Run invoker | `allow_unauthenticated = true` | Set `false` or restrict IAM principals |
| Basic auth password | `{noop}admin` in `auth.tpl.yml` | Change users/passwords; use strong encoding |
| Bucket | `gcs_force_destroy = true` | Set `false` when bucket holds real data |
| Image | `mill-service-minimal:latest` | Pin `image_version` to a tag or digest |

Secrets (DB password, API keys) are **not** part of this blueprint. Commented env blocks in [`main.tf`](main.tf) show how to extend for DB/OpenAI.

---

## Destroy

```bash
tofu destroy -var-file=terraform.tfvars
```

With `gcs_force_destroy = true`, objects are deleted when the bucket is destroyed.

---

## Limitations vs full Cloud Run module

| Feature | Minimal blueprint | [`../cloud-run/`](../cloud-run/) |
|---------|-------------------|----------------------------------|
| PostgreSQL / metadata DB | No (in-memory H2 in default image) | Yes |
| OpenAI / `ai` profile | No | Optional |
| GCS config sync script | Manual `gsutil` | `./sync-bucket.sh` |
| Image selection | `image_version` variable | `mill_docker_image` + profiles |
| Edition profiles | Single layout | `profiles/*.tfvars` |
| Shared modules | Inline SA/bucket | `deploy/gcp/modules/` |

---

## Troubleshooting

**No tables / empty schema**

- Confirm objects live under `gs://{bucket}/data/{table}/file.{parquet|csv|avro|xlsx}`.
- Check runtime SA has `roles/storage.objectViewer` on the bucket (module grants this).
- Verify regex in `flow.tpl.yml` matches your paths (parent folder = table name).

**New files uploaded but tables still missing**

- With `schema_cache_enabled = true`, listing results are cached until `schema_cache_ttl` elapses. After `gsutil` uploads, wait for TTL, set a shorter `schema_cache_ttl` and `tofu apply`, or set `schema_cache_enabled = false` for debugging.

**Secret mount / revision fails**

- Runtime SA and **serverless robot** (`service-{project_number}@serverless-robot-prod.iam.gserviceaccount.com`) need `roles/secretmanager.secretAccessor` — both are granted by this module.

**401 / auth unexpected**

- Default deploy has `mill.security.enable: false` — no auth until you enable it (see above).
- Demo credentials are `admin` / `admin` when basic auth is enabled.

**Config change not picked up**

- New Secret Manager versions from Terraform do not always roll Cloud Run automatically; deploy a new revision.

**Wrong table names (e.g. `s_pq` instead of `cities`)**

- Ensure flow regex does not use a leading greedy `.*` before `(?<table>…)`; use `(?<table>[^/]+)/[^/]+\.{ext}$`.

**GCS errors on Cloud Run (`handshake_failure`, etc.)**

- If `gcloud storage ls` works from your workstation but the service fails, compare the **same image** locally with ADC (runtime SA or user credentials). Minimal images use a jlink JRE; TLS/CA issues may differ from full `mill-service-complete`.

---

## Related

- [`../cloud-run/README.md`](../cloud-run/README.md) — full Mill on Cloud Run (DB, AI, GCS sync)
- [`../README.md`](../README.md) — GCP deploy layout
- [`../../README.md`](../../README.md) — deploy overview
- [`docs/design/source/`](../../../docs/design/source/) — Flow source descriptors and table mapping
- [`docs/public/src/sources/configuration.md`](../../../docs/public/src/sources/configuration.md) — user-facing source YAML reference
