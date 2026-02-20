# Design Documents

This directory contains internal design documents, architecture notes, and implementation
plans. Documents are grouped by topic area into subfolders.

## Folder Structure

| Folder | Scope | What goes here |
|--------|-------|----------------|
| `ai/` | AI and NL-to-SQL | Reasoning architecture, step-back flows, capabilities, scenarios, AI UX specifications, regression testing |
| `client/` | Client libraries | Python client (mill-py), JDBC driver, SQL dialect descriptors, client API design |
| `data/` | Data layer | Type system, complex types, vector encoding, schema definitions, wire format design |
| `metadata/` | Metadata subsystem | Metadata service design, provider refactoring, value mappings, metadata UI, implementation roadmaps |
| `platform/` | Infrastructure and cross-cutting | Configuration, migration plans (Spring, WebFlux), codebase analysis, protocols (MCP, gRPC export), Calcite dialect work |
| `publish/` | Build, release, and documentation | Maven publishing, artifact signing, documentation generation tooling (MkDocs, Dokka) |
| `refactoring/` | Codebase refactoring | Refactoring iterations, file inventories, dependency graphs, configuration key audits, progress tracking |
| `source/` | Data source framework | Source provider design, format handlers (CSV, Parquet, etc.), storage abstraction, Calcite adapter |
| `ui/` | Frontend and UX | Grinder UI design, chat input, clarification flows, visual refresh, interaction patterns |

## Classification Principles

1. **Classify by primary domain, not by artifact type.** A document about "metadata UI" goes
   in `metadata/` (its domain), not `ui/` — unless the document is purely about UI patterns
   with no domain-specific content.

2. **When a document spans two domains, place it in the domain it serves.** For example, a
   type system reference belongs in `data/` (its primary domain), even though types are
   consumed by sources, clients, and backends.

3. **`platform/` is the catch-all for cross-cutting concerns.** Configuration, migrations,
   build tooling, codebase audits, protocol specifications, and anything that doesn't belong
   to a single product domain goes here.

4. **Keep the hierarchy flat — one level of subfolders only.** Do not create nested subfolders
   (e.g. `ai/step-back/`). If a topic area grows large, prefer filename prefixes
   (e.g. `sb-reasoning.md`, `sb-ux-flow.md`) over deeper nesting.

5. **Use descriptive filenames with lowercase-kebab-case.** The filename should hint at the
   document's purpose without needing to open it. Avoid generic names like `design.md` or
   `notes.md`.

6. **New domain folders require justification.** Only create a new top-level folder if there
   are at least 3 documents that don't fit any existing category. Prefer expanding an existing
   folder's scope over fragmenting into many small folders.
