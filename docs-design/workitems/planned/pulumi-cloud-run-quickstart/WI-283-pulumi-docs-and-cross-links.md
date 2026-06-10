# WI-283 — README quick start and cross-links

## Goal

Ship a **single** operator-facing **`deploy/pulumi/README.md`** that is sufficient for a new user on **Windows, macOS, or Linux** to complete a demo deploy without reading the Pulumi program. Cross-link the existing bash-based GCP deploy for operators who need **sync / project create / destroy scopes**.

## Acceptance

1. **Quick start** — Numbered steps (target **≤ 7**): install Python deps → `pulumi login` → GCP Application Default Credentials → select/create stack → set `pulumi config` (project + secrets) → `pulumi up` → verify `/actuator/health` URL.
2. **YAML table** — Short inline table or subsection listing **quickstart.yaml** keys (name, purpose, default, secret?).
3. **Cross-links** — From README to [`deploy/google-cloud-run/README.md`](../../../../deploy/google-cloud-run/README.md) with one sentence on when to prefer bash vs Pulumi.
4. **Optional** — One line in root or [`deploy/README`](../../../../deploy/README.md) if it exists, or [`deploy/Makefile`](../../../../deploy/Makefile) help text pointing to `deploy/pulumi/README.md` (Makefile remains optional per story goal).

## Notes

- **No** requirement for a separate `CONFIG.md` or MkDocs page in v1; fold reference into README to keep doc surface small.
