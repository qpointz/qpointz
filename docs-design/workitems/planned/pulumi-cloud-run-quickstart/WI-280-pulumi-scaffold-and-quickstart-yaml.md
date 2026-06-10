# WI-280 — Pulumi scaffold and quickstart YAML

## Goal

Create the `deploy/pulumi/` tree with a runnable Pulumi **Python** project and a **single** heavily commented **`config/quickstart.yaml`** (or equivalent name) that encodes non-secret defaults for a demo deploy (existing GCP project, region, image, service name, optional feature flags).

## Acceptance

1. **`deploy/pulumi/Pulumi.yaml`** — names the Pulumi project, `runtime: python`, documents expected stack/backend (short comment).
2. **`deploy/pulumi/pyproject.toml`** (or `requirements.txt` if team prefers) — pins `pulumi` and `pulumi-gcp` (and PyYAML / Typer as needed); README documents **one** install path using **`uv`** or **`pip`** that works on Windows, macOS, and Linux.
3. **`deploy/pulumi/__main__.py`** — minimal entry that loads the quickstart YAML (path relative to project dir) and fails fast with a clear error if required keys are missing.
4. **`deploy/pulumi/config/quickstart.yaml`** — commented template only (no secrets); documents which values are set via `pulumi config`.
5. **`.gitignore`** under `deploy/pulumi/` — ignores operator copies such as `config/my.*.yaml` or `*.local.yaml` as documented in README (exact pattern in WI-283 can align).

## Notes

- **No** automated GCP project creation or billing linkage in this story slice (covered by story goal / WI-281 assumptions).
- Keep file layout ready for WI-281 modules (e.g. `infra/` package) even if WI-280 lands with stubs.
