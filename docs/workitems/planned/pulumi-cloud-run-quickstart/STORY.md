# pulumi-cloud-run-quickstart

## Goal

Add a **cross-platform, minimal-effort quick start** to run Mill on **Google Cloud Run** using **Pulumi (Python)** and a **single commented YAML** stack config. This is explicitly **demo / try-it scope**, not production hardening. It lives **alongside** the existing bash-based flow under [`deploy/google-cloud-run/`](../../../../deploy/google-cloud-run/) (which remains the richer, gcloud-oriented path).

Outcomes:

- Operators on **Windows, macOS, or Linux** can follow one short README: install deps → `pulumi login` → GCP ADC → `pulumi up` (no bash, no Make required for the Pulumi path).
- One **`quickstart.yaml`** template documents non-secret settings; secrets via `pulumi config` (documented).
- Optional parity hooks (optional GCS bucket, optional `application.yml` overlay) stay **behind YAML flags** so the default path stays small.

## Work Items

- [ ] WI-280 — Pulumi scaffold and quickstart YAML (`WI-280-pulumi-scaffold-and-quickstart-yaml.md`)
- [ ] WI-281 — Minimal GCP resources (APIs, optional bucket, Cloud Run) (`WI-281-pulumi-minimal-gcp-resources.md`)
- [ ] WI-282 — Quickstart CLI (`copy-example`, `validate`, printed next steps) (`WI-282-pulumi-quickstart-cli.md`)
- [ ] WI-283 — README quick start and cross-links (`WI-283-pulumi-docs-and-cross-links.md`)

postgresql://neondb_owner:npg_ylrV9J4UxRGd@ep-misty-art-aj1h9s5f-pooler.c-3.us-east-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require