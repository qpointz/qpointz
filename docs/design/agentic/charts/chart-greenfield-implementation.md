# Chart UI implementation approach

Normative for **WI-370** / Gap 27.

## Decision

**Greenfield implementation** under `ui/mill-ui/src/components/charts/`. Do **not** port or wrap
legacy [`mill-grinder-ui/ChartView.tsx`](../../../../ui/mill-grinder-ui/src/component/data/ChartView.tsx).

Legacy grinder chart code is **lightly inspirational only** — the planned mill-ui chart stack is
conceptually different (renderer-agnostic semantic spec → local ECharts compile, shared
`ChartRenderer`, chat-agnostic props).

## WI-370 module layout (from scratch)

```
ui/mill-ui/src/components/charts/
  types.ts
  compileChartSpecToECharts.ts
  ChartRenderer.tsx
  chartTheme.ts
  chartData.ts
```

| Module | Responsibility |
|--------|----------------|
| `types.ts` | Semantic chart + snapshot types aligned with [`chart-artifact-contract.md`](../chart-artifact-contract.md) |
| `compileChartSpecToECharts.ts` | Deterministic semantic → ECharts `option` per [`echarts-compiler-contract.md`](./echarts-compiler-contract.md) |
| `ChartRenderer.tsx` | `echarts-for-react` host; resize; Mantine theme tokens |
| `chartTheme.ts` | ECharts theme from Mantine CSS variables |
| `chartData.ts` | Row snapshot → dataset rows for encode |

Chat integration (`SqlDataCondensedPreview`, expand) **imports** `ChartRenderer` — no
`ChartView.tsx` name reuse under `mill-ui`.

## Legacy reference policy

| Allowed | Not allowed |
|---------|-------------|
| Occasional comment: "pattern inspired by mill-grinder ChartView" | Copy-paste grinder components or option builders |
| Read legacy for UX ideas (resize, empty state) | Depend on `ui/mill-grinder-ui` package or paths |
| STORY "Related" link as historical note | Port grinder-specific chart type switches or data assumptions |

Grinder assumed ad-hoc chart config; mill-ui compiles from **`generated-chart`** semantic contract
and catalog — incompatible shapes.

## Docs cleanup

- Do **not** add `ChartView.tsx` under `mill-ui` (avoid name collision with retired grinder module).
- Prefer `ChartRenderer` in architecture docs when WI-370 lands.

## Tests

Vitest compiler vectors per chart type spec; `ChartRenderer` smoke with compiled options — no
grinder parity tests.
