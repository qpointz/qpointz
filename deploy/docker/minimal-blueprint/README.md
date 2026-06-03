# Mill minimal blueprint (Docker)

Run a **minimal Mill** service locally with Docker: Flow backend over files on disk, schema cache, and optional demo basic-auth config. Data and YAML config are bind-mounted directly.

---

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) (Docker Desktop on Windows/macOS)
- Optional: `run.sh` (bash) or `run.ps1` (PowerShell)

---

## Quick start (scripts)

```bash
cd deploy/docker/minimal-blueprint
cp env.example .env   # optional

# Data files under data/ (created automatically; or use --data-dir):
#   data/cities/cities.parquet
#   data/orders/orders.csv

./run.sh
```

```powershell
.\run.ps1
```

```bash
curl -sS http://localhost:8080/actuator/health
```

---

## Quick start (`docker run`)

From this directory (use absolute paths if your shell does not expand `$(pwd)`):

```bash
cd deploy/docker/minimal-blueprint
mkdir -p data

docker run --rm -it \
  --name mill-minimal-blueprint \
  -p 8080:8080 \
  -v "$(pwd)/data:/data:ro" \
  -v "$(pwd)/config/application.yml:/app/config/application.yml:ro" \
  -v "$(pwd)/config/flow.yml:/app/config/flow/flow.yml:ro" \
  -v "$(pwd)/config/auth.yml:/app/config/auth/auth.yml:ro" \
  qpointz/mill-service-minimal:latest
```

Windows (PowerShell):

```powershell
$Root = (Resolve-Path .).Path
New-Item -ItemType Directory -Force -Path (Join-Path $Root "data") | Out-Null
docker run --rm -it `
  --name mill-minimal-blueprint `
  -p 8080:8080 `
  -v "${Root}\data:/data:ro" `
  -v "${Root}\config\application.yml:/app/config/application.yml:ro" `
  -v "${Root}\config\flow.yml:/app/config/flow/flow.yml:ro" `
  -v "${Root}\config\auth.yml:/app/config/auth/auth.yml:ro" `
  qpointz/mill-service-minimal:latest
```

Background: add `-d`, remove `-it`. Other data directory: change the first `-v` source path. Pin image tag as needed.

---

## Data layout

Host **`data/`** directory → container **`/data`** (read-only). Scripts default to `<blueprint>/data` and create it if missing.

```text
data/cities/cities.parquet
data/orders/orders.csv
```

Table name = parent folder; reader = file extension (see [`config/flow.yml`](config/flow.yml)).

```bash
./run.sh --data-dir /path/to/datasets
```

---

## Configuration

Edit files under [`config/`](config/) — they are mounted as-is:

| File | Container path | Purpose |
|------|------------------|---------|
| [`application.yml`](config/application.yml) | `/app/config/application.yml` | Mill app, Flow backend, schema cache (`enabled: true`, `ttl: 5m`) |
| [`flow.yml`](config/flow.yml) | `/app/config/flow/flow.yml` | Local storage at `/data`, parquet/csv/avro/excel readers |
| [`auth.yml`](config/auth.yml) | `/app/config/auth/auth.yml` | Demo users (inactive until `mill.security.enable: true`) |

Change Flow schema name in `flow.yml` (`name:`). Tune cache in `application.yml` under `mill.data.backend.flow.cache.schema`.

---

## Script options

[`env.example`](env.example): `MILL_IMAGE`, `MILL_DATA_DIR`, `MILL_HOST_PORT`, `MILL_CONTAINER_NAME`.

**Override image** (precedence: CLI flag → shell env → `.env` → default):

```bash
export MILL_IMAGE=qpointz/mill-service-minimal:0.8.0rc3
./run.sh

# or in .env:  MILL_IMAGE=my-registry/mill-service-minimal:tag
# or:          ./run.sh --image qpointz/mill-service-minimal:0.8.0rc3
```

```powershell
$env:MILL_IMAGE = "qpointz/mill-service-minimal:0.8.0rc3"
.\run.ps1
# or: .\run.ps1 -Image "qpointz/mill-service-minimal:0.8.0rc3"
```

| `run.sh` | `run.ps1` |
|----------|-----------|
| `--data-dir` | `-DataDir` |
| `--image` | `-Image` |
| `--port` | `-Port` |
| `--detach` / `-d` | `-Detach` |

---

## Enabling HTTP Basic auth

Edit [`config/auth.yml`](config/auth.yml), then in [`config/application.yml`](config/application.yml):

```yaml
mill:
  security:
    enable: true
    authentication:
      basic:
        enable: true
        store: file:/app/config/auth/auth.yml
```

Restart the container.

---

## Troubleshooting

**Empty `/data` in the container** — The bind mount is working; the host folder is empty. Add files under **`data/`** on the host (not the blueprint root), e.g. `data\cities\cities.parquet`, then restart. `run.ps1` / `run.sh` print a warning when no `*.{parquet,csv,avro,xlsx}` files are found.

**No tables** — Files must be `{table}/{file}.{parquet|csv|avro|xlsx}` under the mounted data path (`data/` by default).

**New files not visible** — Shorten `cache.schema.ttl` in `application.yml` or set `enabled: false`, then restart.

**Port in use** — `docker rm -f mill-minimal-blueprint` or `./run.sh --port 18080`.

---

## Related

- [`../../README.md`](../../README.md) — deploy overview
