# WI-282 — Quickstart CLI (validate + copy + hints)

## Goal

Add a **tiny Typer-based** Python CLI under `deploy/pulumi/` that minimizes operator mistakes **without** a long questionnaire: copy the example YAML, validate required fields, and **print** the exact next shell commands (`pulumi login`, GCP ADC, `pulumi config set …`, `pulumi up`).

## Acceptance

1. **Subcommands** (names flexible): e.g. `copy-example`, `validate`, and optionally `print-steps` (or combined `init` that runs copy + validate + print).
2. **Cross-platform** — Uses `pathlib` only; no `bash -c`; works when invoked as `python -m …` from `deploy/pulumi/` on Windows, macOS, Linux.
3. **`validate`** — Reads operator YAML; reports missing/invalid keys with actionable messages (image ref, `gcp:project`, region, service name).
4. **README** — Documents the CLI as part of the 5-step quick start (details can be finalized in WI-283 but CLI must be usable when this WI lands).

## Non-goals

- Full interactive wizard (prompts for every field).
- Wrapping `pulumi` binary (operators still run `pulumi` directly).
