# Codebase Refactoring

Design documents for the Mill platform refactoring effort: extracting Spring wiring into
auto-configure modules, reorganizing into functional lanes, and tracking progress.

## Classification Criteria

A document belongs here if its **primary subject** is one of:

- Refactoring goals, strategy, and iteration plans
- File inventories and Spring contamination classification (PURE vs SPRING)
- Module dependency graphs created for refactoring analysis
- Configuration key audits done as part of refactoring
- Progress tracking and branch/iteration status
- Post-refactoring test analysis and cleanup

## Does NOT Belong Here

- General configuration schema documentation → `platform/`
- Codebase analysis not tied to a specific refactoring effort → `platform/`
- Spring or framework migration plans (Boot upgrades, WebFlux) → `platform/`

## Naming Convention

Documents in this folder use a numbered prefix (`00-`, `01-`, ...) to indicate their role
in the refactoring series. New documents should continue the numbering sequence.

## Documents

| File | Description |
|------|-------------|
| `00-overview.md` | Refactoring overview: goals, functional lanes (Core, Data, Metadata, AI, Clients, UI) |
| `01-iterations.md` | Detailed iteration plan: current module graph and step-by-step strategy |
| `02-file-inventory.md` | Spring contamination file inventory: per-file PURE vs SPRING classification |
| `03-tracking.md` | Progress tracker: iteration status and branches |
| `04-dependency-graph.md` | Module dependency graph with PURE/SPRING classification and Mermaid diagram |
| `05-configuration-keys.md` | Configuration key inventory: all mill.* keys, conditional annotations, metadata JSON |
| `06-test-module-inventory.md` | Post-refactoring test analysis: tests in wrong modules or broken after refactoring |
