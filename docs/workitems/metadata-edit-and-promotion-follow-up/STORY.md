# Metadata — Edit and Promotion Follow-up

Deliver deferred metadata write workflows after schema explorer completion:
- WI-094: facet type descriptor model and validation foundation
- WI-095: facet type management UI in `ui/mill-ui` admin
- WI-096: migration of standard facet descriptors and facet type registry strategy
- WI-090: metadata user editing (entity/facet write path)
- WI-091: metadata promotion workflow (request/review/approve/reject)

This story starts from the deferred WI definitions created in the schema explorer story and carries
them as active planning/implementation scope.

## Work Items

- [ ] WI-090 — Metadata User Editing (`WI-090-metadata-user-editing.md`)
- [ ] WI-091 — Metadata Promotion Workflow (`WI-091-metadata-promotion-workflow.md`)
- [ ] WI-094 — Facet Type Descriptor Foundation (`WI-094-facet-type-descriptor-foundation.md`)
- [ ] WI-095 — Facet Type Management UI (`WI-095-facet-type-management-ui.md`)
- [ ] WI-096 — Standard Descriptor and Registry Migration (`WI-096-standard-descriptor-and-registry-migration.md`)

---

## Dependency Map

```
WI-094 ──► WI-095
WI-094 ──► WI-096
WI-096 ──► WI-090
WI-094 ──► WI-090 ──► WI-091
```

Rationale:
- WI-094 establishes descriptor and validation contracts used by WI-090 edit APIs and UI renderer.
- WI-095 implements admin-facing facet type management and schema-driven authoring UI on top of WI-094.
- WI-096 migrates standard facet definitions and registry strategy to descriptor-native behavior before
  full WI-090 rollout.
- WI-091 promotion execution depends on the write APIs and service paths introduced by WI-090.

---

## Initial Story Notes

- Security/context authorization remains a hard requirement for write and promotion flows.
- Facet editing must be schema-driven (no hardcoded facet field enums).
- Promotion conflict behavior and reviewer model must be agreed before implementation.

---

## Source Lineage

These WIs were deferred from:
- `docs/workitems/metadata-edit-and-explorer/STORY.md`

They are now tracked as an independent follow-up story.

