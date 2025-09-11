# Mill Project - AI Context Bundle

This file provides quick context for AI assistants working on the Mill data integration platform.

## Essential Reading

When starting a new session or working on a different computer, always read:

1. **[.cursor/rules](.cursor/rules)** - Complete project guidelines and conventions
2. **[docs/CODEBASE_ANALYSIS.md](../docs/CODEBASE_ANALYSIS.md)** - Comprehensive architecture analysis
3. **[docs/design/metadata-service-design.md](../docs/design/metadata-service-design.md)** - Current focus: Metadata service design

## Quick Start for AI Assistants

### First-Time Context Load
```
1. Read .cursor/rules for project structure and conventions
2. Review CODEBASE_ANALYSIS.md for architecture understanding
3. Check docs/design/ for active design documents
4. Review AGENTS.md for repository guidelines
```

### Resuming Work
```
1. Check this file for current work status
2. Review recent commits: git log --oneline -10
3. Check active branches: git branch -a
4. Load relevant design documents
```

## Current Work Focus

### Active: Metadata Service Implementation

**Status:** Design phase complete, ready for Phase 1 implementation

**Primary Documents:**
- Design: [docs/design/metadata-service-design.md](../docs/design/metadata-service-design.md)
- Codebase analysis: [CODEBASE_ANALYSIS.md](../CODEBASE_ANALYSIS.md)

**What's Being Built:**
A centralized metadata service with faceted architecture to manage:
- Schema/table/column metadata
- Value mappings for NL2SQL
- Enrichments from chat sessions
- Data quality rules
- Business concepts and relationships

**Key Decisions:**
1. **Separate module:** `mill-metadata-core` (core) + `mill-metadata-ai` (AI-specific)
2. **Faceted design:** Extensible metadata aspects
3. **Multiple persistence:** File (YAML) or Database (JPA)
4. **NL2SQL integration:** Capture enrichments from chat, approval workflow

**Next Steps:**
- Phase 1 (Week 1): Core foundation
  - Create `core/mill-metadata-core` module
  - Implement `MetadataEntity` with facet support
  - Implement core facets (Structural, Descriptive, Relation, Concept)
  - File-based YAML repository
  - Basic REST API

## Project Structure Quick Reference

```
mill/
├── core/                          # No AI dependencies
│   ├── mill-core/                 # Vectors, SQL, types
│   ├── mill-security-core/        # Auth & authz
│   ├── mill-service-core/         # Service orchestration
│   ├── mill-starter-backends/     # Calcite & JDBC backends
│   ├── mill-starter-service/      # Common startup
│   ├── mill-test-common/          # Test utilities
│   └── mill-metadata-core/        # 🆕 NEW - Metadata service
│
├── services/                      # Runtime services
│   ├── mill-jet-grpc-service/     # gRPC API
│   ├── mill-jet-http-service/     # REST API
│   └── mill-grinder-service/      # Web UI (React)
│
├── ai/                            # AI/NL2SQL
│   ├── mill-ai-core/              # NL2SQL engine
│   ├── mill-ai-nlsql-chat-service/ # Chat API
│   └── mill-metadata-ai/          # 🆕 NEW - AI metadata facets
│
└── clients/                       # Client libs
    ├── mill-jdbc-driver/
    ├── mill-py/
    └── mill-spark/
```

## Technology Stack Summary

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17+ |
| Framework | Spring Boot | 3.5.4 |
| Query Engine | Apache Calcite | 1.40.0 |
| RPC | gRPC | 1.74.0 |
| AI/LLM | Spring AI | 1.0.3 |
| Build | Gradle | 8.x |

## Common Tasks

### Building
```bash
cd core && ./gradlew build        # Build core modules
cd services && ./gradlew build    # Build services
cd ai && ./gradlew build          # Build AI modules
```

### Testing
```bash
./gradlew test                    # Unit tests
./gradlew testIT                  # Integration tests
./gradlew jacocoTestReport        # Coverage report
```

### Running Services
```bash
cd services
./gradlew :mill-jet-grpc-service:bootRun    # gRPC service on :9099
./gradlew :mill-jet-http-service:bootRun    # HTTP service on :8080
```

## Key Patterns to Follow

1. **Module structure:** Use composite builds (`includeBuild("../core")`)
2. **Lombok:** Use `@Slf4j`, `@Getter`, `@Setter`, `val` everywhere
3. **Testing:** `<Subject>Test.java` with `shouldX_whenY()` methods
4. **Configuration:** Spring Boot YAML, version catalog for dependencies
5. **Commits:** `[feat]`, `[fix]`, `[change]`, `[docs]`, `[wip]` prefixes

## Recent Work Log

### 2025-11-05 (Desktop - Windows)
**Status:** Design phase complete, ready for implementation  
**Completed:**
- ✅ Created comprehensive codebase analysis (CODEBASE_ANALYSIS.md)
- ✅ Designed metadata service with faceted architecture
- ✅ Created AI assistant rules (.cursor/rules)
- ✅ Set up .cursor/ directory for context sharing

**Next Steps:**
- 📋 Start Phase 1 implementation (mill-metadata-core module)
- Create MetadataEntity base class
- Implement FacetRegistry plugin system
- Create core facets (Structural, Descriptive, Relation, Concept)

**Current Branch:** main  
**Files to Start With:**
- core/mill-metadata-core/src/main/java/io/qpointz/mill/metadata/domain/MetadataEntity.java
- core/mill-metadata-core/src/main/java/io/qpointz/mill/metadata/domain/FacetRegistry.java

**Reference:** docs/design/metadata-service-design.md

---

### Previous Context

**Before 2025-11-05:**
- NL2SQL with intent-based routing (get-data, explain, enrich-model)
- Value mapping system for term resolution
- Security with policy-based authorization
- gRPC and HTTP services operational

---

## How to Update This Log

When switching computers, add a new entry at the top:

```markdown
### YYYY-MM-DD (Computer - OS)
**Status:** [current phase/feature]
**Completed:**
- Item 1
- Item 2

**In Progress:**
- What you're working on
- File: path/to/file.java (line 123)

**Next Steps:**
- What to do next

**Current Branch:** branch-name
**Files Modified:**
- path/to/file1
- path/to/file2

**Notes:**
- Any important context or decisions
```

## Files to Reference Often

| File | Purpose |
|------|---------|
| [docs/CODEBASE_ANALYSIS.md](../docs/CODEBASE_ANALYSIS.md) | Complete architecture guide |
| [AGENTS.md](../AGENTS.md) | Repository guidelines |
| [docs/design/metadata-service-design.md](../docs/design/metadata-service-design.md) | Metadata service spec |
| [libs.versions.toml](../libs.versions.toml) | Dependency versions |
| [proto/](../proto/) | Protocol Buffer definitions |

## Configuration Files by Module

### Core
- `core/mill-service-core/src/main/resources/application-test.yml`
- `core/mill-starter-backends/config/test/model.yaml`

### Services
- `services/mill-jet-grpc-service/src/test/resources/application-test.yml`

### AI
- `ai/mill-ai-core/src/test/resources/application-test-moneta-slim.yml`
- `ai/mill-ai-core/src/main/resources/templates/nlsql/` (prompt templates)

## Test Data

Sample datasets in `test/datasets/`:
- **moneta/** - Banking data (primary test dataset)
- **airlines/** - Flight data
- **cmart/** - E-commerce data

## When Working on Metadata Service

**Must understand:**
1. Faceted architecture concept (see design doc)
2. Core facets vs AI facets separation
3. Plugin system via FacetRegistry
4. YAML serialization format
5. Composite repository pattern (physical + annotations)

**Implementation order:**
1. Domain model (MetadataEntity, MetadataFacet)
2. Core facets (Structural, Descriptive, Relation, Concept)
3. FacetRegistry
4. File-based repository
5. REST API

**Key classes to create:**
```
mill-metadata-core/
├── domain/
│   ├── MetadataEntity.java
│   ├── MetadataFacet.java
│   ├── FacetRegistry.java
│   └── core/
│       ├── StructuralFacet.java
│       ├── DescriptiveFacet.java
│       ├── RelationFacet.java
│       └── ConceptFacet.java
```

## Security Considerations

When working with metadata:
- ✅ Respect existing authorization policies
- ✅ Consider row/column level security
- ✅ Don't expose sensitive metadata in logs
- ✅ Validate user input in enrichments
- ✅ Require approval for metadata changes from NL2SQL

## Testing Strategy for Metadata Service

1. **Unit tests:** Each facet independently
2. **Integration tests:** File serialization, REST API
3. **E2E tests:** NL2SQL → enrichment → approval → metadata update
4. **Performance tests:** Large metadata sets, search

## Questions to Ask Before Implementing

1. Does this follow the faceted architecture?
2. Is this core metadata or AI-specific?
3. Should this be a new facet or part of existing facet?
4. How does this serialize to YAML?
5. Does this need REST API endpoints?
6. How does NL2SQL use this?

## Useful Git Commands

```bash
# See recent changes
git log --oneline --graph -20

# Find who changed a file
git log --follow -- path/to/file

# Search commits
git log --grep="metadata"

# See file at specific commit
git show commit:path/to/file
```

---

**Context Updated:** November 5, 2025  
**Current Phase:** Metadata Service - Design Complete, Implementation Phase 1 Starting  
**Next Milestone:** mill-metadata-core module with core facets and file repository

