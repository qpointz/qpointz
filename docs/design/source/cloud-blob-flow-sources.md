# Cloud blob sources in the Flow descriptor model

This note ties **Flow** (`FlowContextFactory`, `SourceDefinitionRepository`) to **cloud object storage**: the same **`SourceDescriptor`** surface that today drives **`storage.type: local`** will accept **S3**, **Google Cloud Storage**, and **Azure Blob Storage** (**ADLS Gen2-compatible paths**) discriminators once the **`cloud-blob-source`** modules land. Operators still author **one YAML file per logical schema**; **readers**, **table mapping**, **conflicts**, and **attributes** stay unchanged—the **only** branching is **`storage`**.

---

## Relationship to Flow

1. **`SourceDefinitionRepository`** yields **`SourceDescriptor`** instances (YAML → bound model).
2. **`SourceSchemaManager`** / **`FlowContextFactory`** register each descriptor as a **Calcite schema** backed by **`FlowSchema`** tables.
3. **Materialisation** resolves **logical table inputs** by **listing** and **opening** blobs under a **root**: for **`local`** that is **`rootPath`** recursion; for cloud types it is **bucket / container + optional prefix** semantics defined per provider (**WI-262–264**).

The Flow stack does **not** fork per storage kind at the Calcite boundary: **`FlowSchema`** and format readers consume an abstraction that hides **POSIX paths vs object keys**. The Kotlin-side **storage abstraction** in **`flow-kt-design.md`** (`BlobSource`-style enumeration) is the intended seam; cloud implementations plug listing and range reads behind that seam.

---

## Descriptor shape

### What stays identical

- **`name`**, **`readers`** (formats, per-reader **`table.mapping`** overrides),
- **`table`** defaults (mapping, attributes),
- **`conflicts`**,
- **`metadata`** overrides (when the story wires per-descriptor **`metadata.enabled`**/**`metadata.redact`** next to **`mill.data.backend.metadata`**),

…all mirror **`local`** sources.

### What changes (`storage`)

- **`storage.type`** discriminates **`local`**, **`s3`**, **`gcs`**, **`adls`** (frozen — consistent lowercase abbreviation style matching all existing `@JsonTypeName` values in the codebase).
- **Provider-specific fields** ( **`bucket`**, **`region`** / **`endpoint`**, **prefix**, container names, etc.) live **alongside **`type`**; they are validated in **`VerificationIssue`** **`STORAGE`** phase together with **`storage.auth`**.
- **`storage.auth`** is **provider-specific**. There is **no** single auth map shared across clouds: **`auth`** keys allowed for **`storage.type: s3`** differ from GCP and Azure. Wrong keys ⇒ **fail-closed verifier** (**GAP-4**).

Normative **`storage.auth`** bundles, **`preferAmbientCredentials`**, and error messages:

- **[`cloud-blob-storage-auth-descriptors.md`](../data/cloud-blob-storage-auth-descriptors.md)**

Ambient vs delegated inference (**SDK default chains**, workload identity):

- Same document — **omit `auth`** (or omit credential-bearing keys) for **ambient**; non-blank **delegated-bundle** fields select explicit credential material.

---

## Backend metadata (flow facets)

**`flow-schema` / `flow-table` / `flow-column`** facet types stay **stable**; **new storage kinds** extend **payload `params`** (polymorphic storage summary) rather than new facet type keys. See **[`flow-facet-projection-extensibility.md`](../data/flow-facet-projection-extensibility.md)** and **`flow-backend.md`** → *Data Model — flow inferred facets*.

---

## Integration tests and emulators

**`testIT`** against real cloud accounts is optional; the repository standardises **Docker / Compose** emulators (**MinIO**, **fake-gcs-server**, **Azurite**, optional **LocalStack**) with **credential alignment** to descriptor examples:

- **[`object-storage-emulator-docker.md`](../data/object-storage-emulator-docker.md)**

Skymill fixtures under **`test/datasets/skymill/`** (Parquet + Avro) pair with **`test/skymill.yaml`** for **local** and **emulator-backed** flows; cloud modules reuse the **same** reader stack once keys resolve to emulator endpoints.

---

## Related documents

| Document | Role |
|---------|------|
| [`flow-backend.md`](flow-backend.md) | Repository → **`FlowContextFactory`** pipeline |
| [`flow-kt-design.md`](flow-kt-design.md) | File-backed provider + storage abstraction |
| [`cloud-blob-storage-auth-descriptors.md`](../data/cloud-blob-storage-auth-descriptors.md) | Frozen **`storage.auth`** (GAP-4) |
| [`object-storage-emulator-docker.md`](../data/object-storage-emulator-docker.md) | Emulator containers for **`testIT`** |
| **[`docs/workitems/planned/cloud-blob-source/STORY.md`](../../workitems/planned/cloud-blob-source/STORY.md)** | Story checklist and provider WIs |

---
