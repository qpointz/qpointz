# Metadata — operator guide

This page is for **people who run Mill**: what must be configured so metadata appears in the UI, how **YAML seeds** and the **startup seed runner** behave, and where to look when metadata is empty or edits fail.

End-user concepts are in [Concepts](concepts.md); the big picture in [Metadata in Mill](system.md); UI behaviour in [Using metadata in Mill UI](mill-ui.md).

---

## Persistence backend

Metadata content lives in the **metadata repository** selected at runtime:

| Setting | Effect |
|---------|--------|
| **`mill.metadata.repository.type=jpa`** | Relational store (typical production). Requires metadata persistence modules on the classpath, Flyway migrations applied (**DDL only** for greenfield metadata tables), and usually **`mill.metadata.seed.resources`** including **`classpath:metadata/platform-bootstrap.yaml`** so the global scope and standard facet types exist. |
| **`mill.metadata.repository.type=file`** | In-process repository backed by **canonical YAML** files. **`mill.metadata.repository.file.path`** is a comma-separated list of Spring resource locations (`classpath:…`, `file:…`, globs where supported) when you use a persistent file store. Startup requires **either** a non-blank **`path`** **or** at least one entry in **`mill.metadata.seed.resources`**. |
| **`mill.metadata.repository.type=noop`** | Explicit no-op repositories (empty reads). |
| **JPA beans missing and file not configured** | No-op fallbacks may apply for optional consumers; the Model view will show empty metadata until a real backend is wired. |

Related keys (see also [Mill UI — Server configuration](../mill-ui.md#server-configuration-operators)):

- **`mill.metadata.seed.resources`** — **Only** supported startup path for loading seed YAML: global scope, standard facet types, and any follow-on datasets. List **`classpath:metadata/platform-bootstrap.yaml` first** in production so the global scope and built-in facet types exist (Flyway creates metadata **tables only** — no data inserts). Each entry is imported at startup in **MERGE** mode with actor `system`. An empty list means the seed runner applies no files (metadata stays empty until API/import unless you rely on pre-existing DB rows). When JPA and a seed ledger are present, completion is recorded (see design SPEC §14.1).
- **`mill.metadata.seed.on-failure`** — `fail-fast` (default) stops startup if a seed fails; `continue` logs and proceeds.
- **`mill.metadata.facet-type-registry.type`** — How facet type definitions are loaded (`inMemory`, `local`, etc.).

Always validate changes in a non-production profile first.

---

## YAML seeds

Bulk metadata is usually maintained as **multi-document YAML** following the **canonical** layout (facet-types and/or entities per document). The normative reference for field names and normalisation is the design document **metadata-canonical-yaml-spec.md** in the repository (`docs/design/metadata/`).

**Practical rules for operators:**

1. **Entity ids** in YAML must be valid **Mill metadata URNs** (see **`docs/design/metadata/metadata-urn-platform.md`** in the source tree). For schemas, tables, and columns, use the typed **`urn:mill/model/…`** forms (e.g. `urn:mill/model/schema:myschema`, `urn:mill/model/table:myschema.orders`, `urn:mill/model/attribute:myschema.orders.id`). The importer **canonicalises** inputs.
2. **Import order** matters across documents in one stream and across **`mill.metadata.seed.resources`** entries; keep platform bootstrap **before** environment-specific seeds.
3. **MERGE** imports combine with existing rows per service rules; plan backups before changing production seed lists or repository files.

---

## Backups and exports

Use the metadata **export** API or service tooling your team standardises on to dump **canonical YAML** for backup and review. Exports are typically **scope-filtered** (often global scope); compare like with like when diffing.

Store secrets and environment-specific overlays outside public docs.

---

## Schema explorer and bindings

The **physical schema** comes from the data backend (e.g. JDBC / Calcite). The **schema explorer** API enriches nodes with **`metadataEntityId`** when a metadata row exists for that path.

If users see tables but **no** metadata line and **no** facet edits:

- Confirm **`mill.metadata.repository.type=jpa`** (or `file` with valid `path`) is set.
- Confirm seed or import created entities whose **identity** matches what the explorer expects for that catalog path.
- Check case and catalog naming against your backend (binding is case-insensitive on the data side but ids are canonicalised in metadata).

---

## REST surface (high level)

Applications integrate with **`/api/v1/metadata/**`** for entities, scopes, facet types, and import/export. The physical tree lives under **`/api/v1/schema/**`**. OpenAPI/Swagger is available when enabled on your deployment.

Details belong in API reference; operators only need to know **firewalls and TLS** must allow the UI and clients to reach these routes.

---

## Troubleshooting

| Symptom | Check |
|---------|-------|
| Empty facets everywhere | `mill.metadata.repository.type`, DB connectivity (JPA), `repository.file.path` or `seed.resources` (file), Flyway history, and whether **`mill.metadata.seed.resources`** includes platform bootstrap (`classpath:metadata/platform-bootstrap.yaml`). |
| Tree works, edits disabled | User auth, `metadataEntityId` present on explorer DTOs, facet type enabled. |
| Seed appears to do nothing | Logs for `MetadataSeedStartup`; YAML validity; `on-failure` policy. |
| Stale facet after delete | Ensure clients refetch entity facets; verify write API completed. |

---

## See also

- [Metadata in Mill](system.md)
- [Concepts](concepts.md)
- [Using metadata in Mill UI](mill-ui.md)
- [Mill UI](../mill-ui.md)
