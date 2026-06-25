# Mill on GCP Cloud Run (OpenTofu)

Single **OpenTofu** root module for Cloud Run v2, Secret Manager, optional GCS, and workload identity.

Shared GCP logic: [`../modules/`](../modules/) (`mill-labels`, `mill-runtime-sa`).

## Prerequisites

- [OpenTofu](https://opentofu.org/) or Terraform **≥ 1.6**
- `gcloud auth application-default login` (or `GOOGLE_APPLICATION_CREDENTIALS`)

## Quick start

```bash
cd deploy/gcp/cloud-run
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars

export TF_VAR_mill_db_password='...'
tofu init
tofu plan
tofu apply
```

Health check: `tofu output -raw cloud_run_uri` → `{uri}/actuator/health`.

## Editions (profiles)

Same module; overlay edition-specific settings:

```bash
tofu apply -var-file=profiles/minimal.tfvars
tofu apply -var-file=profiles/ai.tfvars    # also: export TF_VAR_openai_api_key='sk-...'
tofu apply -var-file=profiles/complete.tfvars
```

Base project/region/DB values still come from `terraform.tfvars`.

| Profile | Intent |
|---------|--------|
| `minimal` | No GCS bucket, default Compute SA |
| `ai` | `ai` Spring profile + OpenAI secret |
| `complete` | GCS + `skymill-gcs` + optional YAML mount |

## Secrets

```bash
export TF_VAR_mill_db_password='...'
export TF_VAR_openai_api_key='sk-...'   # optional
```

## GCS object upload (after apply)

```bash
cp sync.env.example sync.env   # match gcp_project_id / gcs_bucket_name from tfvars
chmod +x sync-bucket.sh
./sync-bucket.sh
```

Mappings: [`config/gcs-sync.conf`](config/gcs-sync.conf).

## Destroy

```bash
tofu destroy
```

## Variables

See [`variables.tf`](variables.tf) and [`terraform.tfvars.example`](terraform.tfvars.example).

## Related

- [`../../README.md`](../../README.md) — deploy layout
- Legacy path `deploy/google-cloud-run/` redirects here
