# WI-159 — `SqlValidator` contract + autoconfigure wiring

Status: `planned`  
Type: `✨ feature` / `🧪 test`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

**`validate_sql`** ultimately depends on **`SqlQueryToolHandlers.SqlValidationService`** (functional
interface used inside **`mill-ai-v3`**). There is no **stable, application-level contract** for
“validate this SQL” that downstream modules (data layer, Calcite, JDBC, etc.) can implement without
pulling in capability tool plumbing. **`MockSqlValidationService`** is only a stub for tests and
demos.

Spring Boot apps need a **single injection point**: a **`SqlValidator`** abstraction wired through
**`ai/mill-ai-v3-autoconfigure/`** so the **`sql-query`** capability receives a validator derived from
that bean when the chat runtime builds **`SqlQueryCapabilityDependency`** (per **WI-156** dependency
shape).

## Goal

1. **Define a public `SqlValidator` interface** (name fixed) in an appropriate **`mill-ai-v3`**
   package (framework-neutral: **no Spring** imports on the interface). Document with **KDoc**:
   purpose, threading, and what guarantees the implementation should provide (or explicitly not
   provide).
2. **Map contract → capability tool boundary:** provide a small **adapter** (e.g. `SqlValidator` →
   **`SqlValidationService`**) so existing **`validateSql`** / **`SqlQueryCapability`** code paths
   stay unchanged aside from where the collaborator is constructed.
3. **Wire in `ai/mill-ai-v3-autoconfigure/`**: Spring configuration that:
   - Declares beans or factory wiring so an application-supplied **`SqlValidator`** is used when
     building the runtime **`AgentContext` / `CapabilityDependencies`** that include
     **`SqlQueryCapabilityDependency`** (exact wiring point follows **WI-156** and current
     **`LangChain4jAgent` / profile rehydration** code — implementers connect the bean to the same
     path used for schema-capable profiles).
   - Uses **`@ConditionalOnMissingBean`** / **`@ConditionalOnBean`** as appropriate so tests and
     embedders can override or omit validation.
4. **Tests** for the adapter and autoconfigure slice (unit or `@SpringBootTest` minimal), **not** for
   a production-grade validator implementation.

## Out of scope (explicit)

- **Implementing the “real” validator** (parse against Calcite, metadata checks, etc.) — **done by the
  product owner / another module** that provides a **`SqlValidator`** `@Bean`.
- **WI-158** (playground CLI) may keep **`MockSqlValidationService`**; injecting **`SqlValidator`**
  there is **optional** — not required for a thin interactive demo.

## Dependencies

- **WI-156** — final **`SqlQueryCapabilityDependency`** (validator-only on the capability path) must
  be known so autoconfigure targets the correct constructor and capability wiring.

## Acceptance Criteria

- **`SqlValidator`** exists as a **documented public interface** in **`mill-ai-v3`** (or a dedicated
  small API package if the build layout requires it — still no Spring on the interface).
- **`mill-ai-v3-autoconfigure`** registers wiring so a **`SqlValidator`** bean can be supplied by the
  application and is **used** for **`sql-query`** validation in the default chat/agent path (or the
  wiring is explicitly conditional and documented when no bean is present).
- **`MockSqlValidationService`** remains usable for tests; it is **not** required to implement
  **`SqlValidator`** unless you choose to unify them — prefer **clear separation**: contract vs test
  double.
- No duplicate competing “validator” interfaces without migration notes in KDoc.

## Deliverables

- This work item definition.
- Interface + adapter + autoconfigure + tests on the story branch per `docs/workitems/RULES.md`.

## Reference

- **WI-156** — generate-only capability (`WI-156-ai-v3-sql-query-generate-semantics.md`).
- **WI-158** — v3 CLI playground (`WI-158-mill-ai-v3-cli-generated-sql-output.md`).
- Today: `SqlQueryToolHandlers.SqlValidationService`, `MockSqlValidationService`.
- Wiring target module: `ai/mill-ai-v3-autoconfigure/`.
