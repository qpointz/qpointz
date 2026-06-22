# WI-336 — FacetCondensedPreview (SQL shell parity)

**Story:** [`ai-chat-facet-display`](STORY.md) · **Backlog:** U-16  
**Seq:** 2 · **Depends on:** [WI-335](WI-335-extract-facet-readonly-renderer.md)

## Status: complete

## Goal

Replace the general-chat facet stub with a **condensed preview** that matches
[`SqlDataCondensedPreview`](../../../../ui/mill-ui/src/components/chat/artifactPreview/SqlDataCondensedPreview.tsx):
bordered card, **Facet + JSON** tabs, and a **reserved** action-bar column (no handlers yet).

## Scope

### New components

| File | Role |
|------|------|
| `artifactPreview/FacetCondensedPreview.tsx` | Main condensed view; implements `ArtifactPreviewContext` |
| `artifactPreview/FacetJsonReadOnlyPanel.tsx` | Bounded JSON tab (mirror `SqlReadOnlyPanel` height ~160–280px; copy button OK) |

### Layout (normative)

```tsx
<ChatArtifactCard p="xs">
  <Stack gap={4}>
    <Tabs>
      <Group justify="space-between" wrap="nowrap">
        <Tabs.List>
          <Tabs.Tab value="facet">{`Facet:${facetTypeTitle}`}</Tabs.Tab>
          <Tabs.Tab value="json">JSON</Tabs.Tab>
        </Tabs.List>
        <ChatArtifactActionBar enabledActions={[]} reserveLayout />
      </Group>
      <Tabs.Panel value="facet">… FacetReadOnlyBody …</Tabs.Panel>
      <Tabs.Panel value="json">… FacetJsonReadOnlyPanel …</Tabs.Panel>
    </Tabs>
  </Stack>
</ChatArtifactCard>
```

### Facet tab content

- Load descriptor: `facetTypeService.get(normalizeFacetTypeKeyForApi(artifact.facetTypeKey))`
- First tab label: **`Facet:<Type>`** where `<Type>` = `facetBoxBaseTitle(facetTypeKey, {}, descriptor)` (e.g. `Facet:Descriptive`)
- Subheader inside facet panel: `metadataEntityId` (monospace) + **Proposed** badge
- Body: `<FacetReadOnlyBody … />` from WI-335
- Loading: `Loader` / skeleton; API error → generic read-only fallback inside facet panel

### JSON tab content

Pretty-print wire object:

```json
{
  "facetTypeKey": "…",
  "metadataEntityId": "…",
  "payload": { … }
}
```

### Action bar reservation

Extend [`ChatArtifactActionBar.tsx`](../../../../ui/mill-ui/src/components/chat/artifactPreview/ChatArtifactActionBar.tsx):

- Add `reserveLayout?: boolean`
- When `enabledActions.length === 0` && `reserveLayout`: render fixed-width placeholder (disabled icon slots or `minWidth` matching SQL bar) so layout does not shift when actions land in a follow-up story

Document planned future action IDs in [`types.ts`](../../../../ui/mill-ui/src/components/chat/artifactPreview/types.ts) comments: `promote`, `copy-json` (not added to `ArtifactActionId` enum until implemented).

### Treatment + registry

**[`chatArtifactTreatments.ts`](../../../../ui/mill-ui/src/components/chat/artifactPreview/chatArtifactTreatments.ts)** — change **only** `general.facet-proposal`:

```ts
'facet-proposal': {
  mode: 'condensed-preview',
  views: ['condensed'],
  actions: [],
},
```

Leave `inline-model`, `inline-knowledge`, `inline-analysis` on `conversation-card`.

**[`registry.tsx`](../../../../ui/mill-ui/src/components/chat/artifactPreview/registry.tsx):**

- Register `'facet-proposal': FacetCondensedPreview` in `previewRegistry`
- Keep `FacetCardPreview` → `FacetProposalArtifactCard` for inline `conversation-card` path

## Acceptance criteria

- [x] General chat renders `FacetCondensedPreview` for `facet-proposal` artefacts (live SSE + GET replay)
- [x] General chat renders facet-style panel for `schema-capture` after wire normalisation (GET replay parity; branch follow-up)
- [x] Facet tab shows schema-aware fields when metadata facet-type API succeeds
- [x] JSON tab shows full wire payload with bounded height
- [x] Action-bar column present and aligned with SQL artefact row (`reserveLayout`)
- [x] Inline hosts still use stub `FacetProposalArtifactCard` (unchanged)
- [x] No new HTTP API surface beyond existing GET replay mapper extensions for `schema.authoring.capture`

## Out of scope

- Wiring action handlers (promote, copy, expand)
- Expand pane for facets
- Inline chat tabbed shell

## Commit

`[feat] WI-336: FacetCondensedPreview with SQL-parity shell for general chat`
