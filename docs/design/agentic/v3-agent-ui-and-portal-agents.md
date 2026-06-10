# Agent UI and Portal-Defined Agents

**Status:** Directional  
**Date:** March 26, 2026  
**Related:**
- `docs/design/portal/portal-federated-metadata-landscape.md`
- `docs/design/portal/portal-facet-types-vs-local-metadata.md`
- `docs/design/agentic/developer-manual/v3-developer-capabilities-profiles-and-dependencies.md`
- `docs/design/agentic/v3-chat-service.md`

---

## 1. Purpose

Capture a coherent design slice that links:

- a UI that lets users assemble and run agents from reusable `ai/v3` capabilities
- a federated portal that centrally curates those agent definitions across multiple Mill instances

This document extends the portal split already established for metadata: **central definitions**
vs **instance-local state and execution**.

---

## 2. Background: `ai/v3` runtime composition model

The current `ai/v3` runtime already has the right abstraction seams for user-assembled agents:

- **Capabilities** are passive packages of `prompts`, `tools`, and `protocols`.
- **Profiles** bind an agent id to an explicit set of capability ids, and optionally carry an
  event routing policy.
- **Runtimes** (LangChain4j adapters) resolve a profile into concrete capability instances for a
  run context and execute the LLM + tool loop.

The key property for an “agent builder UI” is that an agent can be represented as a
**declarative composition** rather than as a new class per agent.

---

## 3. Portal split applied to agents

The portal design notes distinguish:

- **facet types (definitions)**: portal-sourced, versioned contracts
- **metadata (state)**: instance-local values and operational source of truth

The same split can apply to agents.

| Layer | Owns | Does not own |
|------:|------|--------------|
| **Portal (central)** | Agent **definition artifacts**: stable ids, versioning, tags, approved capability compositions, routing presets, protocol expectations, governance lifecycle | LLM execution, secrets, chat transcripts, run telemetry, instance-specific schema truth |
| **Mill instance (local)** | **Application and execution**: enable/disable published agents, dependency bindings to local services, per-instance policy, runtime runs, persistence of transcript/artifacts/telemetry | Independent mutation of capability contracts outside a release boundary |

**Rule of thumb:** the portal answers *“what agents are approved and what do they mean?”*.
Each instance answers *“which of those run here, against which local services and data?”*.

---

## 4. Agent UI (builder surface)

### 4.1 Builder responsibilities

The UI for building agents should operate over a capability catalog and produce a
versioned, reviewable definition.

Core responsibilities:

- **Catalog browsing**: present available capability descriptors (id, description, supported contexts,
  tags, required dependencies).
- **Composition**: select a set of capabilities to form an agent definition.
- **Protocol visibility**: show the protocols contributed by each capability (TEXT vs structured),
  including ids and schema summaries where applicable.
- **Dependency validation**: ensure all required dependencies for the selected capabilities can be
  bound in the target execution environment.
- **Routing preset selection**: select a routing preset (or a restricted “advanced” editor) that
  controls which events become transcript, artifacts, or telemetry.
- **Preview**: present a merged “system prompt preview” and the enabled tool list before publish.

The UI should treat capabilities as “nodes” users can reason about, but the execution model remains
a profile-like declaration.

### 4.2 Execution-time bindings are not part of central definitions

The builder may allow specifying required “binding slots” (schema access, SQL execution, etc.), but
the actual binding values and credentials are instance-local.

This prevents the portal from becoming an execution bottleneck and keeps secrets local to each
instance.

---

## 5. Protocols and routing in an agent-builder world

### 5.1 Protocols (capability-owned)

Protocols are contributed by capabilities and define output contracts. In a builder UI:

- Adding a capability implicitly makes its protocols available.
- The UI should surface protocol ids/modes so users understand which structured outputs they are
  enabling (especially for authoring and artifact capture workflows).

### 5.2 Routing policy (profile-owned)

Event routing determines:

- what is streamed to the UI as chat events
- what is persisted as durable transcript
- what is persisted as artifacts and which “pointer keys” are updated
- what is recorded as telemetry

In a portal-driven system, routing should be controlled by:

- **presets** that are reviewed and versioned (default for most users), and optionally
- **restricted overrides** for advanced builders (guarded by role and validation)

The routing policy should be considered part of the *agent definition*, while the storage backends
and retention policies remain instance concerns.

---

## 6. Portal agent bundles and instance subscription

### 6.1 Definition lifecycle

Portal agent definitions should support a governance lifecycle:

- draft
- review
- published
- deprecated

Published definitions are packaged into versioned **agent bundles**.

### 6.2 Consumption model

Each Mill instance:

- pulls or imports a bundle release
- validates that all referenced capability ids are present in its runtime
- exposes the published agents for enablement/configuration locally

The instance remains free to:

- disallow specific capabilities or agents per environment (allowlist / policy)
- require admin approval before enabling certain agents

---

## 7. Suggested minimal data model (directional)

Portal-side (definition):

- `agentId` (stable id / URN)
- `version`
- `title`, `description`, `tags`
- `capabilityIds` (set)
- `routingPresetId` (or embedded policy delta)
- `supportedContexts` (optional)
- `governanceStatus`

Instance-side (application):

- `agentId@version` enabled flag
- dependency bindings (per capability id)
- runtime configuration (model selection, limits)
- local overrides (optional, policy-driven)

---

## 8. Non-goals (initial iterations)

- End-user authored **new capabilities** without a code release, signed plugin mechanism, or other
  operational controls.
- Portal-mediated execution where every run must traverse the central portal.
- Replacing instance-local storage for transcript, artifacts, or telemetry.

---

## 9. Open questions

- **Identity and namespacing**: should portal agents use the same URN conventions as facet types?
- **Compatibility**: how should instances validate portal agent definitions against runtime
  capability versions?
- **Conflict resolution**: what happens if two selected capabilities define overlapping protocol ids
  or tool names? Is namespacing required?
- **Observability**: what telemetry should be aggregated at portal level vs retained locally?

