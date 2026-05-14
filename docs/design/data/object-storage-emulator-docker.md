# Object storage emulators — Docker for integration tests

This document lists **Docker** (and **Docker Compose**) recipes for the object-store **emulators** Mill’s **cloud blob-source** integration tests (`testIT`) expect: **S3-compatible** (**MinIO**, optional **LocalStack**), **GCS-compatible** (**fake-gcs-server**), and **Azure Blob** (**Azurite**).

**Related:** Flow + descriptor seam — [`../source/cloud-blob-flow-sources.md`](../source/cloud-blob-flow-sources.md); frozen **`storage.auth`** shapes — [`cloud-blob-storage-auth-descriptors.md`](cloud-blob-storage-auth-descriptors.md).

**Normative product scope** for which emulators apply: [`docs/workitems/planned/cloud-blob-source/STORY.md`](../../workitems/planned/cloud-blob-source/STORY.md) § **Integration testing** and **WI-265** emulator matrix. Implementations should prefer **Testcontainers** in Gradle `testIT` so CI and laptops share the **same images and ports** as documented here; this page is the **operator / contributor** reference for **manual** containers and **compose**-based local stacks.

## Goals

- One place for **image names**, **ports**, **environment variables**, and **health / smoke** checks.
- Parity with **`testIT`**: whatever Testcontainers starts should match these defaults unless a test class documents a deliberate override.
- **No live cloud** required for blob-source **list / stream / seek** proofs (**Skymill** fixtures — see story).

## Prerequisites

- [Docker Engine](https://docs.docker.com/engine/install/) or Docker Desktop, with the daemon running.
- Shell examples use **bash**; on Windows, run them in **Git Bash**, **WSL**, or translate paths for PowerShell.

**Pinning:** Examples use **floating** tags (`latest`, `stable`) for readability. **CI** should pin **digest** or **immutable release tags** and bump them deliberately (see each vendor’s release notes).

## Port and service matrix

| Emulator | Role | Host ports (default) | Typical Mill use |
|----------|------|----------------------|------------------|
| **MinIO** | S3 API + console | **9000** (S3), **9001** (console) | Default **AWS S3-compatible** `testIT` (**WI-262**). |
| **LocalStack** | Multi-service cloud mock | **4566** (edge / gateway) | Optional **S3-only** runs (`SERVICES=s3`). |
| **fake-gcs-server** | GCS HTTP API | **4443** (HTTP) | **GCS** `testIT` (**WI-263**). |
| **Azurite** | Azure Storage APIs | **10000** blob, **10001** queue, **10002** table | **Azure Blob** `testIT` (**WI-264**); blob tests need **10000**. |

Avoid colliding with **Mill** (**8080**), **Postgres** dev (**5432**), or **Authentik** (**19000**) on the same host.

---

## MinIO (S3-compatible — default for AWS blob `testIT`)

**Image:** `minio/minio`  
**Docs:** [MinIO Server](https://min.io/docs/minio/container/index.html)

### `docker run`

```bash
docker run -d --name mill-emulator-minio \
  -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  minio/minio server /data --console-address ":9001"
```

- **S3 endpoint (SDKs):** `http://127.0.0.1:9000` (or `http://localhost:9000`).
- **Console:** `http://127.0.0.1:9001` (dev only).
- **Credentials:** `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD` map to **static “delegated”** access for emulator tests (see story: one stable emulator auth recipe).

### Smoke check

```bash
curl -sf "http://127.0.0.1:9000/minio/health/live"
```

Create buckets with **AWS SDK**, **mc** (installed separately), or **`testIT`** bootstrap code.

### Stop / remove

```bash
docker rm -f mill-emulator-minio
```

---

## LocalStack (optional S3-only)

**Image:** `localstack/localstack`  
**Docs:** [LocalStack](https://docs.localstack.cloud/)

Use when you need **AWS-style** edge behaviour distinct from MinIO. For **S3 blob** proofs, **MinIO** remains the **default** story target.

### `docker run` (S3 only)

```bash
docker run -d --name mill-emulator-localstack \
  -p 4566:4566 \
  -e SERVICES=s3 \
  -e DEBUG=0 \
  localstack/localstack
```

- **Endpoint:** `http://127.0.0.1:4566`.
- **Region / credentials:** follow LocalStack docs (often test keys `test` / `test` and region `us-east-1`); align **`testIT`** fixtures with **WI-265**.

### Stop / remove

```bash
docker rm -f mill-emulator-localstack
```

---

## fake-gcs-server (GCS-compatible)

**Image:** `fsouza/fake-gcs-server`  
**Project:** [fsouza/fake-gcs-server](https://github.com/fsouza/fake-gcs-server)

### `docker run` (HTTP)

```bash
docker run -d --name mill-emulator-gcs \
  -p 4443:4443 \
  fsouza/fake-gcs-server \
  -scheme http -port 4443 -external-url "http://127.0.0.1:4443"
```

- **Base URL:** `http://127.0.0.1:4443`.
- **`-external-url`:** should match what client libraries use to build object URLs (adjust host if tests run inside another container).

### Smoke check

```bash
curl -sf "http://127.0.0.1:4443/storage/v1/b" | head
```

Create buckets via **REST**, **client library**, or test fixture code as in **`testIT`**.

### Stop / remove

```bash
docker rm -f mill-emulator-gcs
```

---

## Azurite (Azure Blob / Queue / Table)

**Image:** `mcr.microsoft.com/azure-storage/azurite`  
**Docs:** [Azurite](https://github.com/Azure/Azurite)

### `docker run`

```bash
docker run -d --name mill-emulator-azurite \
  -p 10000:10000 \
  -p 10001:10001 \
  -p 10002:10002 \
  mcr.microsoft.com/azure-storage/azurite:latest
```

| Port | Service |
|------|---------|
| **10000** | Blob — **required** for **`BlobSource`** / ADLS-compatible paths in tests |
| **10001** | Queue (optional unless tests use it) |
| **10002** | Table (optional unless tests use it) |

- **Blob endpoint:** `http://127.0.0.1:10000/devstoreaccount1` (default well-known **Azurite** account; see Microsoft docs for connection-string format).

### Stop / remove

```bash
docker rm -f mill-emulator-azurite
```

---

## All emulators — single Compose file (local dev)

Save as **`docker-compose-object-storage-emulators.yml`** (any path) and run:

```bash
docker compose -f docker-compose-object-storage-emulators.yml up -d
docker compose -f docker-compose-object-storage-emulators.yml ps
```

Example **compose** definition (same images/ports as above):

```yaml
services:
  minio:
    image: minio/minio:latest
    container_name: mill-emulator-minio
    command: server /data --console-address ":9001"
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin

  localstack:
    image: localstack/localstack:latest
    container_name: mill-emulator-localstack
    ports:
      - "4566:4566"
    environment:
      SERVICES: s3
      DEBUG: "0"

  fake-gcs-server:
    image: fsouza/fake-gcs-server:latest
    container_name: mill-emulator-gcs
    command: ["-scheme", "http", "-port", "4443", "-external-url", "http://127.0.0.1:4443"]
    ports:
      - "4443:4443"

  azurite:
    image: mcr.microsoft.com/azure-storage/azurite:latest
    container_name: mill-emulator-azurite
    ports:
      - "10000:10000"
      - "10001:10001"
      - "10002:10002"
```

**Note:** Starting **all** emulators at once is optional; most developers run **one** vendor stack matching the module they are working on (**WI-262–264**).

---

## Testcontainers and Gradle `testIT`

- **Integration tests** should **start and stop** containers via **Testcontainers** (or test fixtures that wrap the same Docker images) so **`./gradlew testIT`** does not depend on manually launched daemons.
- **This document** still matters for: debugging failed `testIT`, reproducing issues without Gradle, and **CI** agents that allow Docker but do not use Testcontainers for a one-off job.
- Ensure **`DOCKER_HOST`** / Docker-in-Docker settings match your platform (Linux CI, macOS **Colima**, Windows **WSL2** backend).

---

## Skymill fixtures

Blob `testIT` seed **Parquet** and **Avro** from repo paths under **`test/datasets/skymill/`** driven by **`test/skymill.yaml`** (see **cloud-blob-source** story). Emulator buckets/containers must be created and objects uploaded by **test setup** or documented **make** / Gradle tasks — not repeated in full here.

---

## Related

- [`docs/workitems/planned/cloud-blob-source/STORY.md`](../../workitems/planned/cloud-blob-source/STORY.md) — § **Integration testing**, **Cold start**.
- [`docs/workitems/planned/cloud-blob-source/WI-265-cloud-storage-wiring-docs.md`](../../workitems/planned/cloud-blob-source/WI-265-cloud-storage-wiring-docs.md) — emulator matrix, CI tagging.
- [`docs/design/platform/local-dev-authentik.md`](../platform/local-dev-authentik.md) — pattern for **compose**-documented local stacks (different domain).
