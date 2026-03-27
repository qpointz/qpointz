# Facet Type Descriptor Formats (JSON/YAML)

**Status:** Baseline contract (WI-094/WI-096)  
**Last updated:** 2026-03-26  
**Related:** `dynamic-facet-types-schema-and-validation.md`, `portal-facet-types-vs-local-metadata.md`

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
- `required` (optional for `OBJECT`): list of required field names
- `items` (required for `ARRAY`): nested schema node
- `values` (required for `ENUM`): non-empty list of enum value entries
- `format` (optional for `STRING`): one of `date | date-time | email | uri`

Object field entry:

- `name` (required): field key
- `schema` (required): nested schema node

Enum value entry:

- `value` (required): enum literal stored in facet payload
- `description` (required): prompt-facing explanation of the enum literal

---

## Strictness Rules

- Unknown field-name aliases are rejected.
- `title` and `description` are required on descriptor and every schema node.
- `OBJECT` field ordering is explicit and preserved via `fields: []` order.
- Duplicate field names in the same object node are rejected.
- `required` entries must reference declared field names.
- `targetCardinality` defaults to `SINGLE` when omitted.
- `STRING.format` is strictly validated to: `date`, `date-time`, `email`, `uri`.
- `format` is rejected for non-`STRING` schema node types.
- `ENUM.values[].description` is required and must not be blank.
- Unsupported composition constructs (`oneOf`, `anyOf`, `allOf`, conditional schemas) are out of scope.

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

## Startup Import Bundle Format (`import-on-startup`)

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

