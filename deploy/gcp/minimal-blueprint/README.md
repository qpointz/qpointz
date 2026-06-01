# Mill minimal blueprint (GCP Cloud Run)

OpenTofu module that deploys a **minimal Mill service** on **Google Cloud Run** with:

- a dedicated **GCS bucket** for query data,
- **Flow** backend reading Parquet, CSV, Avro, and Excel from that bucket,
- Mill **application**, **flow**, and **auth** YAML pushed to **Secret Manager** and mounted into the container,
- a runtime **service account** with bucket read access and secret access.

This stack is intentionally smaller than [`../cloud-run/`](../cloud-run/README.md): no PostgreSQL, no OpenAI, no Skymill seed sync scripts. Use it to try Mill on GCP with file-backed data only.

---

## What gets created

For `deployment_name = "min-bp-1"` (example):

| Resource | Name / pattern |
|----------|----------------|
| Cloud Run service | `{deployment_name}-run-service` |
| GCS bucket | `{deployment_name}-bucket` |
| Runtime service account | `{deployment_name}-runtime@…` |
| Secret Manager secrets | `{deployment_name}-application-yml`, `-flow-yml`, `-auth-yml` |

**APIs enabled:** Cloud Run, Secret Manager, Cloud Storage, Cloud Resource Manager, IAM.

**Container image (default):** `qpointz/mill-test-complete:latest` (set in [`main.tf`](main.tf); change there or extend the module with a variable for production).

**Ingress:** public HTTPS — `roles/run.invoker` is granted to `allUsers`. Restrict this for non-demo use.

---

## Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│  Cloud Run (mill-*-run-service)                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  mill-test-complete container                         │  │
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

Flow descriptor ([`config/flow.tpl.yml`](config/flow.tpl.yml)) scans `data/` in the bucket. Each **table** is the **parent folder** of a file; the **reader** is chosen by extension:

```text
gs://…/data/cities/cities.parquet   →  schema `minimal`, table `cities` (parquet)
gs://…/data/orders/orders.csv       →  schema `minimal`, table `orders` (csv)
```

Regex pattern (per reader): `(?<table>[^/]+)/[^/]+\.{ext}$` — do **not** prefix with greedy `.*` before the capture group (that breaks names like `cities/cities.parquet`).

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
# Edit project_id and region

tofu init
tofu plan -var-file=terraform.tfvars
tofu apply -var-file=terraform.tfvars
```

Example `terraform.tfvars`:

```hcl
project_id      = "your-gcp-project"
region          = "europe-west1"
deployment_name = "mill-minimal-blpr"   # optional; default shown
```

`terraform.tfvars` is gitignored (see repo root `.gitignore`). Do not commit it.

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

Flow schema name: `tofu output -raw flow_schema_name` (default `minimal`).

Use Mill’s HTTP SQL API or JDBC against the service URL (exact paths depend on the image edition). Flow schema name is from `tofu output -raw flow_schema_name` (default **`minimal`**).

---

## Configuration templates

Terraform renders these into Secret Manager on each apply:

| Template | Mount path | Purpose |
|----------|------------|---------|
| [`config/application.tpl.yml`](config/application.tpl.yml) | `/app/config/application.yml` | Mill app: Flow backend, UI off, gRPC off, cache TTLs |
| [`config/flow.tpl.yml`](config/flow.tpl.yml) | `/app/config/flow/flow.yml` | GCS storage + multi-format readers |
| [`config/auth.tpl.yml`](config/auth.tpl.yml) | `/app/config/auth/auth.yml` | Basic-auth user store (demo only) |

### Customizing config

1. Edit the relevant `config/*.tpl.yml`.
2. Run `tofu apply -var-file=terraform.tfvars`.
3. Trigger a **new Cloud Run revision** so mounted secrets reload (e.g. change an annotation in `main.tf`, or run `gcloud run services update … --region …`).

Flow template variables (set in [`main.tf`](main.tf)):

| Variable | Default in module | Meaning |
|----------|-------------------|---------|
| `schema_name` | `minimal` | Flow / Calcite schema name |
| `bucket_name` | created bucket | GCS bucket for data |
| `project_id` | `var.project_id` | GCP project for GCS client |

To change schema name or add template variables, extend the `templatefile(…)` block in `main.tf`.

### Enabling HTTP Basic auth

[`config/auth.tpl.yml`](config/auth.tpl.yml) ships a **demo** user (`admin` / `{noop}admin`). Replace before any shared environment.

Basic auth is only active when Mill security is enabled and the auth file is wired. In [`config/application.tpl.yml`](config/application.tpl.yml), set:

```yaml
mill:
  security:
    enable: true
    authentication:
      basic:
        enable: true
        file-store: file:/app/config/auth/auth.yml
```

Then update `auth.tpl.yml` with your users (prefer `{bcrypt}…` or another delegating prefix, not `{noop}` in production).

---

## Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `project_id` | yes | — | GCP project ID |
| `region` | yes | — | Cloud Run region (bucket location matches) |
| `deployment_name` | no | `mill-minimal-blpr` | Prefix for resource names |

See [`variables.tf`](variables.tf) and [`outputs.tf`](outputs.tf).

---

## Security notes (demo defaults)

| Setting | Default | Action for production |
|---------|---------|------------------------|
| Cloud Run invoker | `allUsers` | Remove `google_cloud_run_v2_service_iam_member.invoker_public` or restrict to your IAM principals |
| Basic auth password | `{noop}admin` in `auth.tpl.yml` | Change users/passwords; use strong encoding |
| Bucket | `force_destroy = true` | Set `false` when bucket holds real data |
| Image | Public Docker Hub tag | Pin a version; use your registry |

Secrets (DB password, API keys) are **not** part of this blueprint. Add env vars and Secret Manager resources in `main.tf` if you extend the stack (see commented blocks in that file).

---

## Destroy

```bash
tofu destroy -var-file=terraform.tfvars
```

With `force_destroy = true` on the bucket, objects are deleted when the bucket is destroyed.

---

## Limitations vs full Cloud Run module

| Feature | Minimal blueprint | [`../cloud-run/`](../cloud-run/) |
|---------|-------------------|----------------------------------|
| PostgreSQL / metadata DB | No (in-memory H2 in default image) | Yes |
| OpenAI / `ai` profile | No | Optional |
| GCS config sync script | Manual `gsutil` | `./sync-bucket.sh` |
| Image selection | Hardcoded in `main.tf` | `mill_docker_image` variable |
| Terraform outputs | `cloud_run_uri`, `gcs_bucket_name`, secret IDs, … | Same + DB/OpenAI secret IDs |
| Edition profiles | Single layout | `profiles/*.tfvars` |

---

## Troubleshooting

**No tables / empty schema**

- Confirm objects live under `gs://{bucket}/data/{table}/file.{parquet|csv|avro|xlsx}`.
- Check runtime SA has `roles/storage.objectViewer` on the bucket (module grants this).
- Verify regex in `flow.tpl.yml` matches your paths (parent folder = table name).

**401 / auth unexpected**

- Confirm `mill.security.enable: true` and `file-store` points at `/app/config/auth/auth.yml`.
- Remember demo credentials are `admin` / `admin` until you change `auth.tpl.yml`.

**Config change not picked up**

- Secret Manager version updated by Terraform does not always roll Cloud Run automatically; deploy a new revision.

**Wrong table names (e.g. `s_pq` instead of `cities`)**

- Ensure flow regex does not use a leading greedy `.*` before `(?<table>…)`; use `(?<table>[^/]+)/[^/]+\.{ext}$`.

---

## Related

- [`../cloud-run/README.md`](../cloud-run/README.md) — full Mill on Cloud Run (DB, AI, GCS sync)
- [`../../README.md`](../../README.md) — deploy layout
- [`docs/design/source/`](../../../docs/design/source/) — Flow source descriptors and table mapping
- [`docs/public/src/sources/configuration.md`](../../../docs/public/src/sources/configuration.md) — user-facing source YAML reference
