# Dynamic facet types — schema, validation, and serialization (open design)

**Status:** Open — decisions pending  
**Last updated:** 2026-03-22  
**Related:** `metadata-service-design.md`, `docs/design/agentic/v3-capability-manifest.md`, `ai/mill-ai-v3/.../CapabilityManifest.kt` (`ToolSchemaYaml`), work item story `docs/workitems/metadata-persistence-and-editing/`, deferred WI-090 (user editing)

---

## Alignment with ai-v3 capability manifests (ToolSchema)

The v3 agent runtime already uses a **declarative schema tree** for tools and protocols: YAML loads into a recursive **`ToolSchemaYaml`** model (`type`, `description`, `properties`, `items`, `required`, `enum`, `additionalProperties`), which compiles to LangChain4j **`JsonSchemaElement`** via `toJsonSchemaElement()` — **without** storing a raw JSON Schema string in the manifest.

**Same idea for metadata facet payloads:** each facet type declares **one root payload schema** using the **same structural shape** as a tool’s `input` / `output` / `finalSchema` block in a capability YAML.

| v3 capability | Metadata facet (analogy) |
|---------------|---------------------------|
| Tool `input` / `output` | **Facet payload** for `urn:mill/metadata/facet-type:…` |
| `ToolSchemaYaml` | **`FacetPayloadSchema`** (or equivalent name in `mill-metadata-core`) |
| Per-capability YAML under `resources/capabilities/` | **Facet type descriptor** — platform: classpath YAML per type or bundled registry; deployment: same JSON blob in DB |
| `CapabilityManifest.load(...)` | **`FacetTypeDescriptor` load** — merge seed + DB + import |
| `toJsonSchemaElement()` | **`toJsonSchemaElement()`** / **`toDraft07JsonSchema()`** for validators, OpenAPI, or LLM interop |
| `request.argumentsAs<T>()` | **Optional** Jackson binding of payload JSON to a Kotlin **data class** for *known* URNs (same pattern as tool args) |

**Wire format unchanged:** persisted and API payloads remain **JSON** (`JsonNode` / `Map` in domain). The descriptor carries **schema as structured data**, not Java classes. Classes are optional projections for known types.

**Reuse vs duplication:** long-term, consider extracting the recursive schema tree + conversion into a **small shared library** (e.g. under `core/`) consumed by both `mill-ai-v3` and metadata. Short-term, **mirror the same field layout** as `ToolSchemaYaml` so YAML authors see one familiar dialect. Track technical debt if two copies exist.

**Optional packaging:**

- **`FacetPayloadSchema`** — Kotlin data classes mirroring `ToolSchemaYaml`.
- **`FacetTypeManifest`** — optional one-YAML-per-type resource for platform seeds (e.g. `resources/facet-types/descriptive.yaml`) with `urn`, `applicableTo`, and a `payload:` schema block; DB-backed custom types store the **same** JSON structure in the catalog row.

**Coverage of goals with this model:**

| Goal | How (v3-style) |
|------|----------------|
| **Limited JSON Schema generation** | Walk `FacetPayloadSchema` → emit draft-07 (or reuse conversion to LangChain4j elements then a second adapter). Support the same **limited** subset v3 uses: object, array, scalars, enum, required, nested properties. |
| **UI rendering** | Walk the **same tree**: descriptions → labels/help; types → controls (enum → select, object → group, array → list). |
| **Serialize / deserialize** | Jackson only on the wire; optional **`payloadAs<T>()`** for known facet URNs aligned with the schema, analogous to tool `argumentsAs<T>()`. |

### JSON Schema per entity type (generated on the fly)

The same **structured payload schema** per facet type makes it possible to **assemble a JSON Schema for a given entity type** without hand-writing a schema per `(entity type × facet)` matrix.

**Inputs:**

- **`FacetCatalog`** — all `FacetTypeDescriptor` rows (platform + custom).
- **Entity type URN** — e.g. `urn:mill/metadata/entity-type:table`, `…:column`, `…:schema` (matches `applicableTo`).

**Algorithm (sketch):**

1. **Select** facet types where `enabled` and (`applicableTo` is null/empty **or** contains the target entity-type URN).
2. For each selected facet type, **materialise** JSON Schema (or structured schema) from its **`FacetPayloadSchema`** (or generated from `contentSchemaJson`).
3. **Compose** a document schema for “metadata for this entity kind” — typical patterns:
   - **Envelope object** — properties keyed by **facet type URN** (string), each value matching that facet’s payload schema; `required` lists facet types marked `mandatory` for that entity type.
   - **Or** `{ "facets": { "<facetTypeUrn>": <payloadSchema>, … } }` if you need a single root property.
4. **Cache** by `(entityTypeUrn, catalogVersion)` if the catalog is large; **invalidate** when facet types change.

**Uses:**

- **UI** — drive a single “edit table metadata” form from one composed schema.
- **LLM / OpenAPI** — describe allowed facet payloads for NL-to-SQL or agents without static Java types.
- **Validation** — validate a **bundle** of facet writes for an entity in one pass (optional; per-facet validation remains the minimal path).

**Note:** If a facet type’s payload shape **varies** by entity type beyond what `applicableTo` expresses, the descriptor needs either **per–entity-type payload variants** (e.g. map entity-type URN → `FacetPayloadSchema`) or separate facet type URNs. The common case — **one payload schema per facet type**, filtered by `applicableTo` — fits the composition above.

### Portal (central) vs instance (local)

Facet **type** definitions (URNs, payload schemas, `applicableTo`, mandatory flags) are intended to be **owned centrally** — e.g. published from the **portal** or a **portal git repo** — and **consumed** by each Mill instance into its local `FacetCatalog`. **Metadata state** (entity rows, facet payloads, scopes, audit) stays **local to the instance**. See `docs/design/portal/portal-facet-types-vs-local-metadata.md`.

---

## Problem statement

Two requirements pull in different directions:

1. **Typed, compile-time facets** — Much of the codebase (UI, schema integration, NL-to-SQL helpers) evolved around **known** facet families with **Kotlin/Java types** (e.g. descriptive / structural / relation payloads as data classes or maps with well-understood shapes). That gives IDE support, safe refactors, and explicit DTOs at REST boundaries.

2. **Deployment-defined facet types** — The platform direction is that **facet types are not a fixed enum**: they are identified by **URN strings** (`urn:mill/metadata/facet-type:…`), registered in a **facet catalog** (file or DB), and may be **added or customised per deployment** (e.g. `governance`, domain-specific extensions). New types must not require a code change for every new facet family.

So: **storage and APIs are generic (JSON payloads keyed by facet-type URN)**, while **callers often want strongly typed views** for the subset of types they know about.

This note lists **what still needs to be designed** and **reasonable design axes** — it does not mandate a final choice.

---

## 1. Define facet type structure

**Goal:** A single, platform-level description of “what this facet type is” that works for both built-in and dynamic types.

**Existing building blocks (implementation direction):**

| Concept | Role |
|--------|------|
| **Facet type URN** | Canonical id (`type_key` in DB, keys in YAML/API after normalisation). |
| **`FacetTypeDescriptor` (core)** | `displayName`, `mandatory`, `enabled`, `applicableTo: Set<String>` (entity-type URNs), optional **`contentSchemaJson`**. |
| **Platform seed types** | Five known types (structural, descriptive, relation, concept, value-mapping) — still described by the same descriptor machinery. |

**Open design work:**

- **Canonical payload description** — Prefer a **structured schema tree** (v3-style; see [Alignment with ai-v3 capability manifests](#alignment-with-ai-v3-capability-manifests-toolschema)) stored in the catalog; **generate** JSON Schema when a string is needed. Alternatively, store **JSON Schema** text only in `contentSchemaJson` (pick dialect: draft-07 vs 2020-12) *or* a smaller Mill-specific subset if full JSON Schema is too heavy for v1.
- **Versioning** — `FacetTypeDescriptor.version` vs schema evolution: when `contentSchemaJson` changes, how do imports and old rows behave?
- **Discovery** — REST `GET /api/v1/metadata/facets` is the registry; clients and tools must use **URN keys**, not Java class names, as the stable identifier.
- **“Known” vs “unknown” types** — Optional **codegen or hand-written adapters** map URN → Kotlin type for *selected* types; everything else is **`Map<String, Any?>`** or **`JsonNode`** at the edge.

Deliverable: a short **facet type contract** document (this file can be promoted once decisions are fixed) plus examples for one platform type and one custom type.

---

## 2. How to validate facets

**Goal:** Writes (API or import) reject invalid payloads **before** persistence; reads may still return stored JSON for backwards compatibility.

**Contrasting forces:**

- **Dynamic types** need **schema-driven validation** driven by `contentSchemaJson` (or equivalent).
- **Built-in types** might still use **Kotlin validation** for speed and clarity where the schema duplicates existing logic — but that duplicates the source of truth unless tests enforce parity.

**Open design work:**

- **Validator implementation** — e.g. networknt/json-schema-validator, everit, or Jackson + JSON Schema; must run **server-side** on `MetadataEditService.setFacet` (deferred WI-090) and on **import**.
- **Error model** — Align with `MillStatuses.unprocessableRuntime` and a structured body (field paths, schema keyword) — see open questions in WI-090.
- **Mandatory / enabled flags** — `FacetTypeDescriptor.mandatory` governs *presence* of a facet for an entity; `contentSchemaJson` governs *shape* of the payload.
- **Scope and security** — Validation is orthogonal to **scope ownership** (who may write which scope); both must pass.

Deliverable: **validation pipeline** diagram: deserialize JSON → validate against `contentSchemaJson` → domain `Map` / typed projection → persist.

---

## 3. How to serialize and deserialize

**Goal:** One **canonical serialized form** for persistence and REST; optional **typed views** at module boundaries.

**Principles:**

| Layer | Representation |
|-------|------------------|
| **Persistence** | `payload_json` / YAML facet values as **JSON** (string in DB). No Java class names in storage. |
| **Domain (`MetadataEntity.facets`)** | `Map<facetTypeUrn, Map<scopeUrn, payload>>` with `payload` typically **`Map<String, Any?>`** or a **Jackson `JsonNode`** — **not** a sealed hierarchy of Java facet classes per type. |
| **API** | Request/response bodies use **JSON**; facet type identified by **URN** (and path slug where applicable). |
| **Clients (UI, AI)** | May define **TypeScript interfaces** or Kotlin data classes for **known** URNs only; for unknown URNs, use **generic JSON** or a form renderer driven by `contentSchemaJson` (deferred UI). |

**Open design work:**

- **Jackson configuration** — Polymorphic deserialization by **URN** is fragile; prefer **explicit mapping** per known URN in a small registry, and **generic `Map`/`JsonNode`** for the rest.
- **Import/export** — YAML import normalises keys to URNs; round-trip must preserve **unknown** facet types without dropping them.
- **UI binding** — For dynamic editing, **schema-driven forms** read `contentSchemaJson`; no requirement that every facet maps to a Java class.

Deliverable: **serialization ADR**: canonical JSON, allowed DTO projections, and where strong typing is allowed without breaking dynamic registration.

---

## Resolution strategy (recommended direction)

1. **Source of truth for structure** — `FacetTypeDescriptor` in the catalog holds a **structured payload schema** (same recursive model as v3 `ToolSchemaYaml`). Persist as JSON in DB/YAML; optionally **materialise** JSON Schema from it for validators that expect a string. Raw **`contentSchemaJson`** alone is not required if the structured model is present.
2. **Strong typing is optional** — A **facet adapter** or **projection** layer may map URN → typed model for known types; the **repository and REST core** stay schema-agnostic.
3. **Validation** — Validate facet JSON against the **structured schema** (direct walk) or against **generated JSON Schema** (dialect TBD); keep optional Kotlin checks only where they mirror schema and are covered by tests.
4. **Track deferred work** — User editing (WI-090) and generic UI renderer depend on locking **§1–§3** above and the **v3-aligned** schema tree in this section.

---

## References

- `docs/design/agentic/v3-capability-manifest.md` — YAML `ToolSchema` reference, recipes, load/bind patterns.
- `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/core/capability/CapabilityManifest.kt` — `ToolSchemaYaml`, `toJsonSchemaElement()`.
- `docs/workitems/metadata-persistence-and-editing/WI-086-metadata-rest-controller-redesign.md` — URN convention, `FacetTypeDescriptor`, import format.
- `docs/workitems/metadata-persistence-and-editing/WI-090-metadata-user-editing.md` — editing, validation, and UI open questions.
- `docs/design/data/schema-facet-service.md` — schema layer typed `SchemaFacets`; may need alignment notes once dynamic facet story is closed.
