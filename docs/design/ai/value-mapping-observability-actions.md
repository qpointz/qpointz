# Value mapping / vector sync — observability (action points)

**Status:** open — **not** part of current [`implement-value-mappings`](../../workitems/in-progress/implement-value-mappings/STORY.md) acceptance criteria.

**Context:** [**WI-179**](../../workitems/in-progress/implement-value-mappings/WI-179-sync-vectors-hydration.md) (sync) and [**WI-180**](../../workitems/in-progress/implement-value-mappings/WI-180-value-mapping-service-orchestrator.md) (`ValueMappingService`) introduce **log-and-continue** embedding failures and **batch** reconciliation; **without metrics**, operators cannot see drift, partial failure rates, or sync throughput.

## Action points

| # | Action | Notes |
|---|--------|--------|
| **A1** | Expose **counters** for rows **synced**, **deleted from store/repo**, **re-embedded** per attribute / run (labels: `attribute_urn` or hashed id if cardinality is a concern). | Tie to Micrometer or Mill’s standard metrics facade. |
| **A2** | Expose **counters / timers** for **embed API** calls, **success vs failure** (supports **log-and-continue** diagnosis). | Align with [**WI-176**](../../workitems/in-progress/implement-value-mappings/WI-176-embedding-model-harness.md) harness operations. |
| **A3** | Optional: **vector store** operation counts (add/replace/delete) from [**WI-177**](../../workitems/in-progress/implement-value-mappings/WI-177-vector-store-harness.md). | Low priority if store is in-memory MVP. |
| **A4** | Document **dashboard / alert** suggestions when metrics exist (runbook pointer — **TBD**). | Can live in public ops docs later. |

## Ownership / scheduling

- **Track** as a **follow-up WI** or **hardening** milestone after first **`implement-value-mappings`** merge; link from story **closure** notes.
- **Duplicate pointer** (short) remains in [`../platform/mill-configuration.md`](../platform/mill-configuration.md) under **Observability gaps** so platform readers find this doc.

## Related

- [`mill-ai-configuration.md`](mill-ai-configuration.md) — `mill.ai.*` stack.
- [`../platform/CONFIGURATION_INVENTORY.md`](../platform/CONFIGURATION_INVENTORY.md) — Spring property inventory.
