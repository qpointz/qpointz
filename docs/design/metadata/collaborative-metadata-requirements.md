# Collaborative Metadata Requirements

**Status:** Draft  
**Date:** 2026-03-09  
**Scope:** Metadata collaboration, editing, relational persistence, scopes, contexts, and promotion workflows

## Purpose

Mill metadata should support **collaborative metadata and knowledge engineering**, where users
build private and contextual understanding of data, and admins or knowledge owners curate
selected insights into shared `default` or `domain` knowledge.

This document defines the product and domain requirements for that model. It is intentionally
focused on concepts, workflows, and required behavior rather than work-item planning.

## Goals

1. Allow users to build and refine metadata as part of direct editing, AI chat, and agentic
   workflows.
2. Store metadata in a relational database as the primary system of record.
3. Introduce metadata scopes as isolated metadata sets that can be combined into effective
   contexts.
4. Support collaborative promotion of user knowledge into shared `default` or `domain`
   knowledge.
5. Preserve provenance, auditability, and controlled conflict resolution throughout the process.

## Non-Goals

- Defining implementation work items.
- Locking a final database schema.
- Defining advanced semantic/vector/lineage features in detail.
- Defining fine-grained merge semantics inside facet payloads beyond first-release needs.

## Core Concepts

### Metadata Scope

A **scope** is an isolated set of metadata definitions and modifications.

Each scope has:
- an identifier
- a type
- ownership/visibility metadata
- lifecycle state
- audit metadata

Scopes are persisted independently and combined only through explicit context resolution.

### Metadata Context

A **context** is an ordered composition of one or more scopes used to resolve the effective
metadata visible to a user or activity.

A context is not a scope. It is a resolver input over scopes.

Contexts should support:
- explicit ordering
- naming/saving for reuse
- provenance of which scope contributed each effective facet
- user-level composition of multiple user-owned contexts into a higher-level working context

### Promotion

**Promotion** is the governed workflow that takes metadata from a user-originated context and
publishes selected knowledge into a shared target scope.

Promotion is:
- nominated by a user
- reviewed and applied by an admin or knowledge owner
- selective, not all-or-nothing
- auditable

## Scope Types

### `default`

The `default` scope is the singleton, instance-wide baseline metadata scope.

Requirements:
- exactly one `default` scope per Mill instance
- globally visible
- used when no other context is specified
- serves as the fallback baseline in composed contexts

### `user`

The `user` scope is the private metadata workspace for a single user.

Requirements:
- visible only to the owning user and authorized reviewers/admins
- stores personal edits, AI-derived knowledge, and workflow-derived metadata
- supports create, update, and disable operations
- can be organized into saved/named contexts by the user

### `domain`

A `domain` scope is shared metadata for a specific business or problem domain such as `sales`,
`accounting`, or `support`.

Requirements:
- shared visibility for users authorized for that domain
- used to describe data from a domain-specific point of view
- may be included alongside other domain scopes in a context
- is a valid promotion target

### `custom`

A `custom` scope is an activity-bound metadata scope created for a specific function or activity,
such as AI chat or an agentic workflow.

Requirements:
- bound to an activity identifier
- has highest priority within that activity context
- can be saved/renamed by the user
- may later be nominated or promoted through the normal workflow
- should not automatically leak into non-activity contexts
- Mill must not assume that all custom context types are known in advance
- Mill must provide APIs that allow new custom context types to be introduced without changing the
  core scope model

### Custom Context Extensibility

Mill must treat custom contexts as an extensibility mechanism.

Requirements:
- the platform must support creation of new custom contexts by API, not only by built-in workflow
  types
- custom contexts must be able to carry a context kind/type identifier supplied by the caller
- the metadata model must support unknown or future custom context kinds without schema redesign
- resolution rules for `custom` scopes should remain stable even when new custom context kinds are
  introduced
- authorization and lifecycle policy may vary by custom context kind, but the base create/read/
  update/delete and context-composition APIs should remain generic

## Context Resolution Rules

### Default Context

If no explicit context is supplied, the effective context is:

- `[default]`

### Activity Context Precedence

Within an activity such as chat, the effective precedence is:

- `custom` > `user` > `domain(s)` > `default`

### Non-Activity Context Precedence

Outside an activity, the effective precedence is:

- `user` > `domain(s)` > `default`

### Multiple Domain Scopes

A context may include multiple domain scopes.

Requirements:
- the system must validate whether selected domain scopes can coexist in one context
- conflict-resolution strategy for overlapping domain contributions is not yet finalized
- first release should prefer aggressive conflict detection over implicit merging
- overlapping domain contributions to the same `(entity_id, facet_type)` should be treated as a
  validation concern unless explicitly resolved by policy

## Metadata Modification Model

Metadata modifications are applied per:
- `entity_id`
- `facet_type`
- `scope`

Supported operations for the initial model:

### `create`

Creates a facet not present in lower-priority scopes.

### `update`

Replaces or changes facet content for an existing facet in the effective model.

### `disable`

Masks a facet for a specific `(entity_id, facet_type)` so the effective context behaves as if
that facet does not exist.

Example:
- disable `descriptive` on `schemaA.tableB.attributeC`

Requirements for `disable`:
- applies at whole-facet granularity
- does not physically delete lower-scope metadata
- must be visible in provenance/explain output as a masking decision

## User Authoring Requirements

Users must be able to build metadata knowledge through:
- direct manual editing
- AI chat outputs
- agentic workflow outputs

Required behavior:
- create and modify metadata in user-owned scopes
- save and rename user-created contexts
- inspect effective metadata through composed contexts
- inspect raw metadata in an individual scope
- understand provenance of effective metadata
- explicitly choose the target scope when writing metadata

Write operations must never be inferred solely from the effective context. Every write must
identify the destination scope explicitly.

## Relational Persistence Requirements

Relational storage is the primary persistence model for collaborative metadata.

Required behavior:
- durable create/read/update/delete for scopes, contexts, and metadata modifications
- explicit storage of ownership, visibility, revision, and audit metadata
- efficient retrieval by scope, context, entity, and facet type
- support for promotion workflow state and audit trail
- support for bootstrap/migration from file-backed metadata where needed

Persistence must support at least:
- scope definitions
- context definitions
- custom context kind/type metadata
- facet modifications by scope
- promotion nominations
- promotion review outcomes
- provenance and audit history

## API Requirements

Mill should provide generic APIs for collaborative metadata rather than only hard-coded workflow
entry points.

Required behavior:
- create/read/update/delete scopes
- create/read/update/delete saved contexts
- create custom contexts through a generic API
- register or supply a custom context kind/type when creating a custom context
- combine multiple user-owned contexts through a generic composition API
- resolve effective metadata for a supplied context
- inspect provenance for effective metadata
- submit a context for promotion review

The API design should make it easy for new product features, AI flows, or agentic workflows to
introduce new custom contexts without requiring changes to the core metadata concepts.

## Saved Context Requirements

Users need reusable named contexts, not only ad hoc composition.

Required behavior:
- create a named context
- rename a named context
- save ordered scope composition
- combine multiple user-owned contexts into a new user-owned context
- mark a context as activity-bound or reusable
- nominate a saved context for promotion

User-level context composition should be a first-class capability.

Requirements:
- a user can select multiple contexts they own and combine them into a new working context
- combined contexts must preserve ordering and provenance of contributing scopes and contexts
- the system must validate composition conflicts when combining user-owned contexts
- the resulting combined context must itself be saveable, renameable, and promotable

AI assistance may be used during user-level context composition.

Requirements:
- Mill may use RAG and summarization to help users understand overlap, conflicts, and candidate
  combined results across contexts
- AI-generated summaries or synthesized facets must preserve links to contributing source
  contexts/facets
- AI assistance must not hide the raw underlying metadata differences from the user
- any AI-generated combined result must still be reviewable, editable, and auditable

### Cross-Domain Synthesis

Mill should support synthesis of multiple domain-specific definitions into a higher-level combined
definition.

Example:
- a `sales` domain scope may describe a table from a pipeline and territory perspective
- an `accounting` domain scope may describe the same table from an invoicing and reconciliation
  perspective
- Mill should be able to generate a synthesized definition that covers both viewpoints

Requirements:
- synthesis must be treated as creation of a new facet, not only as overlay resolution
- synthesis may be AI-assisted using RAG and summarization
- synthesized results must preserve provenance to all contributing source facets and contexts
- users and reviewers must be able to inspect the synthesized result alongside original
  domain-specific definitions
- synthesized facets must remain editable before save or promotion
- synthesized facets may be stored in user, domain, or default scope depending on workflow and
  approval

Open design detail:
- whether names apply to contexts only, or to user-created scopes as well, must remain explicit
  in the implementation design

## Promotion Workflow Requirements

Promotion is the mechanism by which user knowledge becomes shared organizational knowledge.

### Promotion Sources

Promotion sources may include:
- direct user edits
- AI-chat-derived metadata
- agentic workflow outputs
- saved user contexts composed from those sources

### Promotion Targets

Admins or knowledge owners may combine nominated user context knowledge into:
- `default`
- `domain`

This means promotion supports:
- `user -> default`
- `user -> domain`

### Promotion Roles

Users:
- create and evolve private knowledge
- nominate contexts for promotion

Admins / knowledge owners:
- review nominated contexts
- select target scope (`default` or `domain`)
- choose which facets to promote
- resolve conflicts
- apply approved promotions

### Promotion Operations

Promotion must support selective handling of metadata, not only whole-context publication.

Required outcomes per selected facet:
- accept and publish to target scope
- reject and leave unpromoted
- keep existing target facet unchanged
- combine conflicting inputs into a new resulting facet

The last case is important: conflicting facets may be summarized or synthesized into a new
shared facet rather than choosing one source unchanged.

RAG and summarization may be used to support promotion review.

Requirements:
- Mill may use retrieval and summarization to present candidate merged interpretations of
  conflicting metadata during review
- Mill may use retrieval and summarization to generate a higher-level combined definition from
  multiple domain-specific definitions, even when those definitions are complementary rather than
  strictly conflicting
- AI-generated merge proposals must preserve provenance to the source user contexts/facets
- AI-generated merge proposals must require explicit admin or knowledge-owner approval before
  publication into `default` or `domain`
- the final approved shared facet must remain auditable as either accepted source content or a
  synthesized reviewed result

### Promotion Audit Requirements

Each promotion decision must record:
- source context
- source scope/facet
- target scope
- reviewer/approver identity
- timestamps
- decision/result
- resulting facet payload or reference
- provenance back to the originating user knowledge

### Promotion Review Model

Promotion must support:
- partial approval of a context
- selective facet-level promotion
- explicit handling of conflicts
- repeatable review over time

Shared scopes (`default`, `domain`) are curated knowledge products, not mechanical unions of
user scopes.

## Provenance and Explainability Requirements

For any effective facet, the system should be able to answer:
- which scope contributed this facet?
- was this facet created, updated, or disabled in that scope?
- was this facet promoted from another context?
- if this facet is absent, was it masked by `disable`?

This provenance is required for:
- user trust
- admin review
- debugging context composition
- promotion decisions

## Authorization Requirements

Authorization rules must distinguish:
- user-private editing rights
- domain-scope management rights
- promotion nomination rights
- promotion approval rights

Minimum expectations:
- users can edit their own user/custom knowledge
- users cannot directly publish to `default` or `domain`
- admins/knowledge owners can review and apply promotions
- only authorized actors can manage shared domain scopes

## Open Decisions

The following remain intentionally open and require follow-up design:

1. Exact conflict policy for multiple `domain` scopes in one context.
2. Whether saved names apply only to contexts or also to user-created scopes.
3. Lifecycle/retention policy for activity-bound `custom` scopes.
4. Whether custom context kinds need optional per-kind validation or registration.
5. Whether promotion can later support `domain -> default` flows.
6. Which composition and promotion steps should use RAG/summarization by default versus only on
   explicit request.

## Summary

Mill metadata should evolve from a read-mostly annotation system into a collaborative metadata and
knowledge-engineering platform with:
- private user knowledge workspaces
- activity-bound custom overlays
- shared domain knowledge
- a singleton default knowledge base
- explicit context composition
- relational persistence
- governed promotion into shared knowledge

The essential model is:
- users build knowledge privately
- contexts organize that knowledge
- admins and knowledge owners curate selected knowledge into `default` or `domain`
- all resulting shared metadata remains auditable and traceable to its origins
