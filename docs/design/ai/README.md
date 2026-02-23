# AI and NL-to-SQL

Design documents for the AIMILL natural-language-to-SQL subsystem.

## Classification Criteria

A document belongs here if its **primary subject** is one of:

- LLM reasoning architecture (step-back, multi-step, capabilities, advisors, orchestrators)
- NL-to-SQL query understanding, gap detection, and clarification flows
- AI interaction model and UX specification (how the agent talks to users)
- Scenario-aware flows (comparisons, trends, multi-query workflows)
- AI regression testing and conversation snapshotting
- Refactoring of AI-specific application response types

## Does NOT Belong Here

- General UI/UX patterns with no AI-specific content → `ui/`
- Metadata or data-model browsing that happens to appear in an AI chat → `metadata/`
- Client-side SQL dialect descriptors consumed by AI → `client/`

## Naming Convention

Documents related to Step-Back reasoning use the `sb-` prefix (e.g. `sb-reasoning.md`,
`sb-ux-flow.md`). Other AI documents use descriptive kebab-case names.

## Documents

| File | Description |
|------|-------------|
| `capabilities_design.md` | Modular LLM architecture v2: capabilities, advisors, orchestrators, reasoner |
| `regression-snapshotting.md` | Full-conversation snapshot approach for regression testing |
| `sb-interaction-model.md` | Step-Back interaction pattern: LLM calls, clarification flow, data model |
| `sb-reasoning.md` | Step-Back reasoning concept: query understanding, gap detection, clarification |
| `sb-refactor-app-response.md` | Unify reasoner response types with ChatApplicationResponse |
| `sb-scenarious.md` | Step-Back extended to multi-step scenarios (comparisons, trends, workflows) |
| `sb-ux-flow.md` | AIMILL user interaction specification: flows, example messages, tone |
| `rag-value-mapping-integration.md` | RAG ingestion of value mappings: embedding text format, document structure, semantic query flow |
| `sb-ux-implementation-plan.md` | Implementation plan aligning backend/frontend with Step-Back UX flows |
