# Federated Metadata Landscape Portal

## Purpose

Capture initial thoughts for a separate application that aggregates metadata from
multiple Mill instances and provides a central place for discovery,
collaboration, and knowledge sharing.

This portal is not intended to become the operational bottleneck for local
instances. Local Mill instances remain the owners of execution, local schema
truth, and instance-specific metadata publication. The portal acts as a
coordination layer above them.

## Working Vision

The portal provides:

- a company-wide view of the data landscape across multiple Mill instances
- a central registry of known facets and shared facet semantics
- a collaboration surface where consumers and maintainers can ask questions,
  provide feedback, and refine metadata together
- metadata scoring and stewardship signals that show where understanding is
  strong, weak, trusted, stale, or incomplete
- a place to accumulate organizational knowledge without forcing every consumer
  to interact with each Mill instance separately

This aligns with a partial data mesh operating model:

- data remains domain-owned
- local instances publish metadata
- the portal provides federated visibility, shared standards, and collaboration

## High-Level Model

The portal should distinguish between three different concerns:

1. Source metadata

Metadata exposed by individual Mill instances for their local assets.

2. Shared semantic model

The central registry of recognized facet types, their schemas, semantics,
validation rules, and default rendering conventions.

**Refinement:** facet **type definitions** are **portal-sourced (central)**; **metadata values**
remain **local to each Mill instance**. See
`docs/design/portal/portal-facet-types-vs-local-metadata.md`.

3. Collaborative knowledge

Questions, feedback, proposals, comments, review outcomes, and other
contributions created by portal users.

These concerns should stay separate even when they are rendered together in the
same effective view.

## Why the Current Faceted Metadata Model Looks Like a Good Fit

The current faceted metadata model appears to be a strong fit for the semantic
representation layer of the portal.

Reasons:

- Facets are modular. Different Mill instances can expose different subsets of
  metadata without requiring a single rigid schema.
- Facets are entity-centered. This matches the portal need to attach knowledge
  to schemas, tables, columns, concepts, and relationships.
- Facets are extensible. New domain-specific or portal-specific facet types can
  be introduced without redesigning the whole model.
- The existing model already anticipates scope-aware metadata, enrichments, and
  future facet families such as lineage, semantic, and data quality.

At the same time, facets should not be stretched to model every collaboration
workflow. A useful boundary is:

- facets represent effective semantic state
- portal collaboration objects represent social/editorial process

Examples of portal collaboration objects:

- question
- feedback item
- proposal
- review
- resolution
- promotion decision

Approved outcomes may then materialize into facet overlays.

## Architecture Direction

At a high level, the portal could evolve into the following layers:

1. Source integration layer

- connect to multiple Mill instances
- discover metadata capabilities and available facet types
- read source metadata and schema context
- track source identity, freshness, and provenance

2. Federation layer

- normalize source assets into a central registry
- maintain canonical identity for assets across instances
- support local asset identity and optional logical cross-instance grouping
- compute effective merged views without necessarily physically copying all
  source metadata

3. Collaboration layer

- user feedback
- questions and answers
- proposals and review
- maintainers and consumers collaborating around the same asset view
- promotion of accepted knowledge into broader scopes

4. Intelligence layer

- metadata completeness scoring
- trust and review status
- freshness and provenance strength
- identification of weakly documented or ambiguous assets
- signals for AI-readiness and stewardship needs

5. Projection layer

- UI-friendly landscape views
- APIs for search, exploration, and downstream consumers
- optional write-back or synchronization for approved metadata

## Non-Goals for the First Iteration

- replacing local Mill instances as source-of-truth systems
- centralizing all operational control in the portal
- requiring synchronous approval for every local metadata change
- solving all governance workflows before basic landscape visibility exists

## Key Design Questions

The portal direction introduces several questions that are more important than
the exact UI shape:

### Federated identity

How should assets from different Mill instances be identified?

Open questions:

- is the portal anchored primarily on physical assets or logical assets?
- how are equivalent assets across instances linked?
- how stable are identifiers when source systems rename schemas or tables?

### Overlay and precedence

How should source metadata and portal-authored knowledge combine?

Open questions:

- what wins when portal knowledge conflicts with source-published metadata?
- should portal knowledge always be additive, or can it override selected
  fields?
- what is the effective precedence order across source, team, reviewer, and
  global scopes?

### Facet governance

What does a central registry of known facets mean in practice?

Open questions:

- which facets are required, optional, experimental, or deprecated?
- who can introduce a new facet type?
- how are validation and UI rendering rules defined and versioned?

### Collaboration workflow

How should users collaborate without overloading the metadata model itself?

Open questions:

- which interactions become facets, and which stay as workflow objects?
- what is the minimum viable review and promotion workflow?
- how should maintainers be notified when consumers ask questions or report
  missing metadata?

### Scoring

What should the portal score, and how actionable should those scores be?

Candidate scores:

- completeness
- trust
- freshness
- review coverage
- collaboration activity
- AI-readiness

## Relationship to Existing Design Tracks

This portal direction appears adjacent to, but larger than, the existing
metadata subsystem design.

Relevant foundations already exist or are planned in:

- `docs/design/portal/portal-facet-types-vs-local-metadata.md` — central facet **types** vs local **metadata**
- `docs/design/metadata/metadata-service-design.md`
- `docs/design/metadata/collaborative-metadata-requirements.md`
- `docs/design/metadata/metadata-implementation-roadmap.md`
- `docs/design/data/schema-facet-service.md`
- `docs/design/persistence/persistence-overview.md`

The portal should reuse these foundations where possible rather than creating a
parallel metadata model.

## Initial Product Framing

Working description:

> A federated portal that provides a company-wide view of the data landscape,
> aggregates metadata from multiple Mill instances, and gives consumers and
> maintainers a shared place to build trusted organizational knowledge.

Possible value pillars:

- landscape visibility
- collaborative knowledge sharing
- metadata quality and trust scoring
- federated semantic governance

## Next Steps

- define the portal domain model and separate semantic state from collaboration
  workflow state
- define federated asset identity and precedence rules
- define the central facet registry model
- decide whether the portal stores a materialized metadata index, a virtualized
  view, or a hybrid
- identify the smallest MVP that proves value without requiring a full platform
  rewrite
