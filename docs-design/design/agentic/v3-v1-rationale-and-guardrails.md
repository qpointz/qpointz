# Agentic Runtime v3 - v1 Rationale And Guardrails

**Status:** Draft  
**Date:** March 16, 2026  
**Scope:** Why `ai/v3` exists despite chat workflow feature parity with `ai/v1`

## 1. Purpose

This note captures the practical reasons for continuing `ai/v3` even when chat workflows
have reached feature parity with `ai/v1`.

The main question is not whether `v1` can still produce similar user-visible outcomes.
It is whether the architecture behind those outcomes remains workable for:

- prompt quality
- testing
- maintainability
- framework isolation
- future feature evolution

## 2. Why `v1` Was No Longer A Good Long-Term Base

`ai/v1` delivered important product behavior, but it accumulated several structural problems.

### 2.1 Monolithic "uber prompt" degradation

The original `v1` NL2SQL flow concentrated too much behavior into a single broad prompting
surface or into a small number of very large prompt-driven stages.

As more responsibilities were added, prompt quality degraded:

- grounding became less precise
- instruction conflicts became harder to manage
- value mapping, schema reasoning, SQL shaping, refinement, and explanation started competing for
  the same prompt budget
- model behavior became less stable as additional concerns were embedded into the same flow

This is a core reason not to revert to `v1`: the original failure mode was prompt-scale
degradation.

### 2.2 Testing was extremely difficult

`v1` made isolated testing hard because behavior was distributed across:

- prompts
- message specs
- intent selection
- post-processors
- optional tool use
- runtime wiring

This made many changes expensive to validate with confidence. The architecture did not provide
small, stable, framework-free surfaces for unit testing most business behavior.

### 2.3 Coupling to Spring was too high

`v1` was heavily entangled with Spring-oriented runtime assembly.

That caused multiple problems:

- harder local reasoning about dependencies
- weaker portability of core logic
- more expensive tests
- more friction when trying to isolate pure domain behavior from infrastructure

The coupling level was high enough that it became an architectural constraint rather than just an
integration detail.

### 2.4 Behavior was too spread out

Even when `v1` worked, important behaviors were often split across multiple layers:

- prompts told the model what to do
- processors repaired or enriched model output
- runtime wiring injected additional behavior
- tools sometimes overlapped with prompt instructions

That made the system harder to explain and harder to evolve safely.

## 3. Why `v3` Is Still The Right Direction

`ai/v3` should be kept because it directly addresses the failure drivers above.

### 3.1 Decomposition over monolithic prompting

`v3` moves from broad "do everything in one prompt" behavior toward:

- smaller capability-scoped prompts
- explicit tools
- planner-driven decomposition
- structured protocols

That is the correct answer to prompt degradation. Even if the runtime is more abstract, the model
surface is more bounded and controllable.

### 3.2 Better testability

The capability model gives smaller units that can be tested in isolation:

- pure tool handlers
- narrow dependency interfaces
- manifest/tool contract tests
- capability wiring tests without full application boot

This is a major engineering improvement over `v1`.

### 3.3 Lower framework coupling

The `v3` runtime is intentionally designed so core logic is not defined by Spring.

Dependencies are passed as explicit interfaces or dependency holders rather than discovered through
framework-heavy wiring in the core design itself.

That makes:

- core logic more portable
- tests smaller
- integration boundaries clearer

### 3.4 Better separation of concerns

`v3` lets responsibilities be placed where they belong:

- schema grounding
- value mapping
- SQL validation/execution
- answer synthesis
- authoring/capture flows

This reduces the need for prompt-era repair logic and makes each concern easier to reason about.

## 4. Feature Parity Does Not Invalidate `v3`

Chat workflow parity with `v1` is an important milestone, but it does not imply that `v1`
should become the preferred implementation base again.

Parity only means:

- the user-visible feature gap has narrowed or closed

It does not mean:

- the old architecture is now preferable
- the old prompt model stopped degrading
- testing became easy
- Spring coupling disappeared

If anything, parity strengthens the case for `v3`, because the migration cost is already paid
while the architectural benefits remain.

## 5. Practical Decision

The recommendation is:

- continue with `v3`
- do not revert to `v1`
- use `v1` as a behavioral reference, not as the implementation direction

In blunt terms:

- `v1` is a useful product reference
- `v3` is the correct engineering base

## 6. Guardrails For `v3`

Keeping `v3` does not mean accepting unlimited architectural growth.

The main risk for `v3` is not the old `v1` monolith. The main risk is over-abstracting the
replacement.

### 6.1 Freeze the current runtime shape unless a real feature breaks it

Do not introduce new runtime concepts just because they seem more generic.

New framework layers should only be added if a concrete feature exposes a real limitation in the
current model.

### 6.2 Prefer concrete capability contracts over diagnostic richness

LLM-facing contracts should stay small and task-oriented.

For example, a good capability API answers:

- which attributes are mapped
- what values resolve for a chosen attribute

It should not expose implementation-heavy diagnostics unless they are needed by the model.

### 6.3 Keep implementation flexibility behind capability boundaries

Capabilities may internally use:

- metadata
- RAG
- schema services
- dynamic sources

But those details should not leak into planner-facing contracts unless necessary.

### 6.4 Optimize for change-cost, not abstraction purity

The measure of success for `v3` is not fewer lines or fewer classes than `v1`.

The real questions are:

- is a new feature easier to add?
- is behavior easier to test?
- is reasoning about ownership clearer?
- can prompt scope remain bounded as more capabilities are added?

### 6.5 Do not recreate an uber prompt in distributed form

A risk in agentic systems is replacing one giant prompt with many vaguely overlapping capabilities.

To avoid that:

- capability roles must stay crisp
- tools must have clear ownership
- the planner should call a capability only for its bounded concern

## 7. Decision Summary

`v3` should continue because it solves the real reasons `v1` became difficult to sustain:

- prompt degradation
- poor testability
- extreme Spring coupling
- weak separation of concerns

The right discipline now is:

- keep `v3`
- keep contracts small
- keep capability ownership clear
- avoid unnecessary new abstraction layers
- judge future design choices by prompt quality, testability, and change-cost
