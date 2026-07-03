# Facet Type Descriptor Formats (JSON/YAML)

**Status:** Baseline contract (WI-094/WI-096)  
**Last updated:** 2026-03-30  
**Related:** `dynamic-facet-types-schema-and-validation.md`, `portal-facet-types-vs-local-metadata.md`, `mill-ui-facet-stereotypes.md` (UI-known tags)

---

## Purpose

Define the canonical facet type descriptor (manifest) payload format used by metadata facet type
management.

- Canonical runtime/storage format: **JSON**
- YAML examples are provided as an authoring/reference equivalent
- Identifiers are URN-normalized at service boundaries

---

## Canonical Contract

Top-level descriptor fields:

- `typeKey` (required): facet type URN or slug
- `title` (required): descriptor title
- `description` (required): descriptor description
- `category` (optional): grouping label for UI/tab presentation; defaults to `general`
- `enabled` (required): boolean
- `mandatory` (required): boolean
- `targetCardinality` (optional): `SINGLE | MULTIPLE` (`SINGLE` default)
- `applicableTo` (optional): list of entity-type URNs or slugs; empty/omitted means any entity type
- `schemaVersion` (optional): descriptor schema version
- `payload` (required): typed payload schema node

Payload schema node fields:

- `type` (required): `OBJECT | ARRAY | STRING | NUMBER | BOOLEAN | ENUM`
- `title` (required): node title
- `description` (required): node description
- `fields` (required for `OBJECT`): ordered list of field entries
- `required` (optional for `OBJECT`): normalized compatibility list of required field names; derived
  from field-level `required` flags when manifests are normalized
- `items` (required for `ARRAY`): nested schema node
- `values` (required for `ENUM`): non-empty list of enum value entries
- `format` (optional for `STRING`): one of `date | date-time | email | uri`
- `default` (optional): hint for execution / authoring when a facet instance omits the field. **Not applied to BOOLEAN controls in mill-ui entity facet forms** (omitted booleans render unchecked). The **facet type admin editor** exposes this for scalar nodes (`STRING`, `NUMBER`, `BOOLEAN`, `ENUM`). Prefer explicit values in stored payloads.

Object field entry:

- `name` (required): field key
- `schema` (required): nested schema node
- `required` (optional): boolean; whether this property is required in facet payloads (`true` default when omitted)
- `stereotype` (optional): UI-only presentation hints — ordered list of **tags** (e.g. `tags`, `hyperlink`, `email`). The metadata service **does not** validate or interpret stereotypes; Mill UI uses them to pick controls and light validation (e.g. email format on save). Same stereotypes apply to **`STRING`** fields and **`ARRAY` whose `items` are `STRING`**. **Wire JSON:** comma-separated string when the field’s value `schema.type` is not `ARRAY`; JSON array of strings when the value schema is `ARRAY` (see `FacetPayloadField` / `FacetPayloadFieldJsonSerde` in `mill-metadata-core`).

Enum value entry:

- `value` (required): enum literal stored in facet payload
- `description` (required): prompt-facing explanation of the enum literal

---

## Strictness Rules

- Unknown field-name aliases are rejected.
- `title` and `description` are required on descriptor and every schema node.
- `OBJECT` field ordering is explicit and preserved via `fields: []` order.
- Duplicate field names in the same object node are rejected.
- `required` entries must reference declared field names; normalized output derives the list from
  field-level `required` flags.
- `targetCardinality` defaults to `SINGLE` when omitted.
- `STRING.format` is strictly validated to: `date`, `date-time`, `email`, `uri`.
- `format` is rejected for non-`STRING` schema node types.
- `ENUM.values[].description` is required and must not be blank.
- Unsupported composition constructs (`oneOf`, `anyOf`, `allOf`, conditional schemas) are out of scope.

---

## JSON Schema Projection

Facet type manifests remain the canonical runtime and storage contract. Mill can also generate a
draft-07-compatible JSON Schema projection for external consumers that need basic payload shape
validation or a model/tool-readable schema document.

- Endpoint: `GET /api/v1/metadata/facets/{typeKey}/schema`.
- Source of truth: `FacetTypeManifest.contentSchema` / `FacetPayloadSchema`.
- Scope: one facet payload instance. For `targetCardinality: MULTIPLE`, cardinality is exposed as
  annotation metadata, not by wrapping the payload schema in an array.

Projection mapping:

| Mill schema | JSON Schema |
|-------------|-------------|
| `OBJECT` | `type: object`, `properties`, derived `required`, `additionalProperties: true` |
| `ARRAY` | `type: array`, `items` |
| `STRING` | `type: string`, optional `format` |
| `NUMBER` | `type: number` |
| `BOOLEAN` | `type: boolean` |
| `ENUM` | `type: string`, `enum` |

Common metadata (`title`, `description`, `default`) is preserved. Mill-specific context is carried
as annotation keywords such as `x-mill-facetTypeUrn`, `x-mill-targetCardinality`,
`x-mill-applicableTo`, `x-mill-category`, `x-mill-schemaVersion`, `x-mill-stereotype`, and
`x-mill-enumDescriptions`.

The JSON Schema projection validates **shape only**. Mill semantics such as `applicableTo`,
`mandatory`, `enabled`, scope ownership, merge behaviour, and `targetCardinality` remain service/UI
policy outside JSON Schema validation.

---

## URN Normalization Rules

At API/import boundaries:

- `typeKey` slug example:
  - `governance` -> `urn:mill/metadata/facet-type:governance`
- `applicableTo` slug example:
  - `table` -> `urn:mill/metadata/entity-type:table`

Persisted and returned descriptors are URN-normalized.

---

## JSON Example (Canonical)

```json
{
  "typeKey": "governance",
  "title": "Governance",
  "description": "Governance metadata attached to entities.",
  "category": "general",
  "enabled": true,
  "mandatory": false,
  "targetCardinality": "SINGLE",
  "applicableTo": ["table", "attribute"],
  "schemaVersion": "1.0",
  "payload": {
    "type": "OBJECT",
    "title": "Governance payload",
    "description": "Fields for governance facet.",
    "fields": [
      {
        "name": "owner",
        "schema": {
          "type": "STRING",
          "title": "Owner",
          "description": "Accountable owner."
        }
      },
      {
        "name": "status",
        "schema": {
          "type": "ENUM",
          "title": "Status",
          "description": "Governance status.",
          "values": [
            { "value": "draft", "description": "Work in progress and not approved yet." },
            { "value": "approved", "description": "Reviewed and accepted for production use." },
            { "value": "deprecated", "description": "Kept for compatibility; avoid for new usage." }
          ]
        }
      },
      {
        "name": "effectiveFrom",
        "schema": {
          "type": "STRING",
          "title": "Effective from",
          "description": "Activation date.",
          "format": "date"
        }
      }
    ],
    "required": ["status"]
  }
}
```

---

## YAML Example (Equivalent Authoring Form)

```yaml
typeKey: governance
title: Governance
description: Governance metadata attached to entities.
category: general
enabled: true
mandatory: false
applicableTo:
  - table
  - attribute
schemaVersion: "1.0"
payload:
  type: OBJECT
  title: Governance payload
  description: Fields for governance facet.
  fields:
    - name: owner
      schema:
        type: STRING
        title: Owner
        description: Accountable owner.
    - name: status
      schema:
        type: ENUM
        title: Status
        description: Governance status.
        values:
          - value: draft
            description: Work in progress and not approved yet.
          - value: approved
            description: Reviewed and accepted for production use.
          - value: deprecated
            description: Kept for compatibility; avoid for new usage.
    - name: effectiveFrom
      schema:
        type: STRING
        title: Effective from
        description: Activation date.
        format: date
  required:
    - status
```

---

## Notes

- Current facet type management API accepts JSON payloads.
- YAML representation is documented for descriptor authoring parity and potential import workflows.
- Validation of metadata values against descriptor payload schemas is intentionally local to each Mill instance.

---

## Startup seed / import bundle format (`mill.metadata.seed.resources`)

Ordered resources listed under **`mill.metadata.seed.resources`** are imported at startup (MERGE mode, actor `system`). The same **canonical YAML** envelope applies to manual import/export via the metadata API.

Recommended canonical YAML envelope for mixed facet-type + metadata import:

```yaml
version: 1
facet-types:
  - typeKey: governance
    title: Governance
    description: Governance metadata attached to entities.
    enabled: true
    mandatory: false
    payload:
      type: OBJECT
      title: Governance payload
      description: Governance schema.
      fields: []
      required: []
entities:
  - id: moneta
    type: SCHEMA
    schemaName: moneta
    facets: {}
```

Compatibility mode:

- Multi-document YAML with `---` separators is supported.
- A document may contain `facet-types`, `entities`, or both.

Safety behavior:

- If no `facet-types` section is provided in the import bundle, the importer ensures all known
  platform facet types exist before completing import.

