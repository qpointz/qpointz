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

## Docker (minimal blueprint)

**Entry point:** [`docker/minimal-blueprint/`](docker/minimal-blueprint/)

```bash
cd deploy/docker/minimal-blueprint
cp env.example .env   # optional
./run.sh              # or: make docker-minimal-run
```

Host data is mounted at `/data`; Flow reads parquet, CSV, Avro, and Excel from the local filesystem.

## Azure (OpenTofu)

**Entry point:** [`azure/minimal-blueprint/`](azure/minimal-blueprint/)

```bash
cd deploy/azure/minimal-blueprint
cp terraform.tfvars.example terraform.tfvars
az login
tofu init && tofu apply -var-file=terraform.tfvars
```

Mill on **Azure Container Apps** with blob-backed Flow data and config in **Key Vault** (mounted via managed identity). See the module README for blob upload and health-check steps.

## Layout

```text
deploy/
  docker/
    minimal-blueprint/   # local Docker run scripts + config
  gcp/
    modules/             # reusable OpenTofu modules
    cloud-run/           # Mill on Cloud Run (root .tf files)
    minimal-blueprint/   # minimal Mill on Cloud Run
  azure/
    minimal-blueprint/   # minimal Mill on Container Apps
```

## Legacy

[`google-cloud-run/`](google-cloud-run/) — retired; use `gcp/cloud-run/` instead.
