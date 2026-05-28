# Mill deployments

## GCP (OpenTofu)

**Entry point:** [`gcp/cloud-run/`](gcp/cloud-run/)

```bash
cd deploy/gcp/cloud-run
cp terraform.tfvars.example terraform.tfvars
export TF_VAR_mill_db_password='...'
tofu init && tofu apply
```

| Edition | Command |
|---------|---------|
| Minimal | `tofu apply -var-file=profiles/minimal.tfvars` |
| AI | `tofu apply -var-file=profiles/ai.tfvars` + `TF_VAR_openai_api_key` |
| Complete (GCS) | `tofu apply -var-file=profiles/complete.tfvars` then `./sync-bucket.sh` |

## Azure

[`azure-container-apps/`](../azure-container-apps/) — separate stack (unchanged).

## Layout

```text
deploy/
  gcp/
    modules/       # reusable OpenTofu modules
    cloud-run/     # Mill on Cloud Run (root .tf files)
    lib/           # GCS sync helpers
  azure/
    container-apps/
```

## Legacy

[`google-cloud-run/`](google-cloud-run/) — retired; use `gcp/cloud-run/` instead.
