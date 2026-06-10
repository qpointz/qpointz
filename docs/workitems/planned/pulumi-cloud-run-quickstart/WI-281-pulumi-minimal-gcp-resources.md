# WI-281 — Minimal GCP resources (quick start)

## Goal

Implement the smallest **GCP** stack that proves Mill on Cloud Run: **enable required APIs**, **optional** GCS bucket (with deploy labels compatible with [`deploy/google-cloud-run/lib/gcp-labels.sh`](../../../../deploy/google-cloud-run/lib/gcp-labels.sh) constants), and a **Cloud Run (v2)** service pulling the **Docker Hub** image from YAML.

## Acceptance

1. **APIs** — Same service enablement intent as [`deploy/google-cloud-run/lib/gcp-provision.sh`](../../../../deploy/google-cloud-run/lib/gcp-provision.sh) `enable_gcp_apis` (Run, Secret Manager, Storage, CRM as needed for the chosen resources).
2. **Optional bucket** — Controlled by YAML (e.g. `bucket.enabled`); uniform access + labels when created.
3. **Cloud Run** — Image, CPU/memory/concurrency from YAML; env vars for `SPRING_PROFILES_ACTIVE`, `MILL_DB_*` etc. as needed for a **documented** demo profile combination that reaches `/actuator/health`.
4. **Public invoker** — For quick start only, document IAM choice (e.g. unauthenticated `roles/run.invoker` for demo) and warn it is **not** production.
5. **Optional overlay** — If YAML enables custom Spring YAML, mirror bash behaviour: secret-as-file at **`/mill-config/application.yml`** and `SPRING_CONFIG_ADDITIONAL_LOCATION` (see [`deploy/google-cloud-run/deploy.sh`](../../../../deploy/google-cloud-run/deploy.sh)); if deferred, document as follow-up in STORY or README.

## Out of scope (v1 quick start)

- GCS **rsync** of local datasets (document manual `gcloud storage cp` or defer).
- Cloud SQL / VPC / org project factory.
