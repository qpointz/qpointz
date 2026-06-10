# Agentic Runtime v3 - v1 Prompt Budget Comparison

**Status:** Draft  
**Date:** March 16, 2026  
**Scope:** Compare `ai/v1` and `ai/v3` prompt/context size characteristics and estimate practical token budgets

## 1. Purpose

This note compares `ai/v1` and `ai/v3` from a prompt-budget perspective.

The goal is not to produce exact tokenizer counts. The goal is to understand the architectural
shape of context cost:

- where `v1` spends tokens
- where `v3` spends tokens
- when `v3` is cheaper or more expensive than `v1`
- what profile design choices matter most for keeping `v3` efficient

## 2. Method

Measured prompt/config files with `wc -c -w` and used a rough heuristic:

- rough tokens ~= characters / 4

This is approximate, but accurate enough for design-level budgeting.

## 3. `v1` Static Prompt Surface

For a typical NL2SQL `get-data` turn, the main static files are:

- `intent/system.prompt`
- `intent/user-question.prompt`
- `schema.prompt`
- `intent/get-data/user.prompt`

Measured sizes:

| File | Characters | Approx tokens |
|------|------------|---------------|
| `intent/system.prompt` | 1,589 | ~397 |
| `intent/user-question.prompt` | 706 | ~177 |
| `schema.prompt` | 817 | ~204 |
| `intent/get-data/user.prompt` | 8,591 | ~2,148 |

Total static prompt surface for `get-data`:

- 11,703 characters
- ~2,926 tokens

Other `v1` intent prompts:

| File | Characters | Approx tokens |
|------|------------|---------------|
| `intent/get-chart/user.prompt` | 10,200 | ~2,550 |
| `intent/refine/user.prompt` | 7,553 | ~1,888 |

## 4. `v1` Runtime Token Shape

Although the static base in `v1` is not extreme by itself, the real cost comes from embedding
schema/context directly into the prompt.

That means `v1` grows with:

- number of schemas/tables included
- number of columns included
- relation descriptions
- previous query / refinement context
- value-mapping instructions and examples

So `v1` tends to have:

- lower static instruction cost
- higher prompt-fragility
- higher schema-size sensitivity

Estimated practical `v1` turn sizes:

| Scenario | Estimated total tokens |
|----------|------------------------|
| small schema / simple NL2SQL | ~4k to ~6k |
| medium schema / typical turn | ~6k to ~9k |
| refine / richer schema context | ~8k to ~12k+ |

## 5. `v3` Static Capability / Tool Surface

Measured manifest sizes:

| File | Characters | Approx tokens |
|------|------------|---------------|
| `conversation.yaml` | 1,371 | ~343 |
| `schema.yaml` | 5,939 | ~1,485 |
| `sql-dialect.yaml` | 10,779 | ~2,695 |
| `sql-query.yaml` | 7,985 | ~1,996 |
| `value-mapping.yaml` | 5,553 | ~1,388 |
| `schema-authoring.yaml` | 13,369 | ~3,342 |

These costs matter because `v3` exposes not only prompt text but also tool descriptions and
input/output schemas to the model.

## 6. `v3` Profile Cost Comparison

### 6.1 Narrow SQL-style profile

If a query workflow uses only:

- `conversation`
- `schema`
- `sql-dialect`
- `sql-query`
- `value-mapping`

Then the measured manifest surface is:

- 31,627 characters
- ~7,907 tokens

### 6.2 Current `schema-authoring` profile

The current `schema-authoring` profile includes:

- `conversation`
- `schema`
- `schema-authoring`
- `sql-dialect`
- `sql-query`
- `value-mapping`

That measured surface is:

- 44,996 characters
- ~11,249 tokens

This is significantly more expensive than a narrower query-oriented profile.

## 7. `v3` Runtime Token Shape

`v3` has a different cost structure than `v1`.

It tends to have:

- higher static framework/capability/tool overhead
- lower dependence on dumping full schema into the prompt
- more incremental grounding through tool calls

That means `v3` is usually:

- more stable as schema complexity grows
- but not automatically cheaper in raw tokens

Estimated practical `v3` turn sizes:

| Scenario | Estimated total tokens |
|----------|------------------------|
| narrow analysis/query profile | ~8k to ~10.5k |
| broad profile with extra unused capabilities | ~11k to ~13k+ |

These totals include:

- capability prompt/tool surface
- planner context
- selected tool results accumulated during the run

## 7A. Single-Turn Cost Versus Multi-Turn Cost

The estimates above are best understood as:

- cost per isolated run or turn

That is important, but it is not the whole story.

### 7A.1 Why `v1` can look cheaper in a single turn

For a single-shot request, `v1` may appear cheaper because:

- the static instruction base is smaller
- there is no explicit capability/tool schema overhead

### 7A.2 Why `v3` should age better over a long conversation

Over a longer conversation, `v3` should usually win or at least degrade more slowly, because:

- capability/tool surface is relatively fixed
- schema grounding can be fetched incrementally rather than always embedded into prompt text
- follow-up turns can reuse previous structured artifacts
- refinement can operate on prior query artifacts instead of reconstructing everything from chat

This means the likely trade is:

| Conversation shape | Likely winner |
|--------------------|---------------|
| one-off simple request | often `v1` |
| multi-step grounded exploration | usually `v3` |
| repeated query refinement / follow-up | strongly favors `v3`, if artifact reuse is implemented correctly |

### 7A.3 The key requirement: artifact reuse

`v3` only realizes this multi-turn advantage if it actually reuses prior structured outputs such as:

- generated SQL
- SQL validation artifacts
- SQL result references
- value-mapping artifacts

Without that, `v3` can still pay high per-turn overhead while failing to amortize context cost
across the conversation.

## 8. Main Comparison

### 8.1 `v1`

Strength:

- smaller static prompt base

Weakness:

- large schema/context often gets inlined into prompt text
- prompt quality degrades as more concerns and more grounding are added

### 8.2 `v3`

Strength:

- schema grounding is incremental and tool-based
- avoids putting all reasoning and grounding into one giant prompt

Weakness:

- capability manifests and tool schemas are expensive
- token cost grows quickly if the active profile includes irrelevant capabilities

## 9. Key Conclusion

`v3` is not automatically cheaper than `v1` in raw token budget.

The real improvement of `v3` is:

- bounded prompting
- reduced prompt fragility
- better grounding decomposition

So the correct architectural claim is:

- `v3` is usually better behaved than `v1`
- `v3` is not always smaller than `v1`

## 10. Current Risk Observed In Practice

Query/refinement workflows have been observed running under the `schema-authoring` profile.

That is expensive and noisy because it includes:

- authoring intent prompts
- capture tool descriptions
- metadata-authoring protocols

These are irrelevant for straightforward query/refinement turns and increase both:

- token cost
- planning confusion

Another observed risk is failure to reuse prior query artifacts during follow-up turns.

If a refinement request is treated as a fresh run rather than a modification of the previous
query/result artifact, then `v3` loses one of its main long-conversation advantages.

## 11. Recommendations

### 11.1 Introduce or prefer a narrow analysis/NL2SQL profile

For query and query-refinement workflows, use a profile limited to:

- `conversation`
- `schema`
- `sql-dialect`
- `sql-query`
- `value-mapping`

Avoid `schema-authoring` unless the workflow is truly about metadata capture.

### 11.2 Keep capability contracts compact

The more detail exposed in tool descriptions and schemas, the more `v3` pays in static context.

Prefer:

- focused tools
- minimal LLM-facing fields
- hiding implementation diagnostics behind runtime boundaries

### 11.3 Watch `sql-dialect` and authoring manifests closely

In the current measurements, `sql-dialect.yaml` and `schema-authoring.yaml` are major
contributors to static token cost.

These should be treated as high-impact optimization targets if context budget becomes tight.

### 11.4 Measure per-profile prompt assembly explicitly

The current estimates are based on source file size.

The next useful step is to measure the actual assembled prompt/tool context per profile at runtime,
because that will show:

- what prompts are actually injected
- what tool schemas are actually exposed
- how much prior run state is retained between planning steps

## 12. Summary

`v1` spent fewer static tokens up front, but paid for it with a giant prompt-driven runtime that
became harder to scale.

`v3` spends more tokens on capability and tool structure, but gives a more stable decomposition.

The practical optimization rule for `v3` is:

- keep profiles narrow
- keep tool contracts minimal
- avoid loading authoring capabilities into analysis/query workflows
