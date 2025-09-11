# Mill Documentation

This directory contains all documentation for the Mill data integration platform.

## 📚 Main Documentation

### Architecture & Analysis
- **[CODEBASE_ANALYSIS.md](CODEBASE_ANALYSIS.md)** - Comprehensive architecture overview (15,000 lines)
  - Complete module descriptions
  - Technology stack
  - Security model
  - Testing strategy
  - **Start here** for understanding the platform

### Guides
- **[CONTEXT_SYNC_GUIDE.md](CONTEXT_SYNC_GUIDE.md)** - Multi-computer workflow
  - Windows desktop ↔ Linux laptop sync
  - Git-based context sharing
  - Usage: See `.temp/sync-context.ps1` and `.temp/sync-context.sh`

- **[VALUE_MAPPING_IMPLEMENTATION.md](VALUE_MAPPING_IMPLEMENTATION.md)** - Value mapping quick reference
  - API documentation
  - YAML format
  - Usage examples

- **[COMMIT_SUMMARY.md](COMMIT_SUMMARY.md)** - Summary of recent changes
  - Files modified/created
  - Commit suggestions

### Implementation Status
- **[TACTICAL_VALUE_MAPPING_COMPLETE.md](TACTICAL_VALUE_MAPPING_COMPLETE.md)** - Value mapping implementation summary

## 🎯 Design Documents

Located in `design/`:

- **[metadata-service-design.md](design/metadata-service-design.md)** - Faceted metadata service (future)
  - 6-phase implementation roadmap
  - Complete architecture
  - **Read this before implementing full metadata service**

- **[value-mapping-tactical-solution.md](design/value-mapping-tactical-solution.md)** - Original tactical design

- **[value-mapping-via-metadata-provider.md](design/value-mapping-via-metadata-provider.md)** - Implementation approach

## 📖 Other Documentation

- **[mill-config-reference.md](mill-config-reference.md)** - Configuration quick reference
- **[mill-configuration-guide.md](mill-configuration-guide.md)** - Detailed configuration guide

### Source Documentation (`src/`)
- `src/index.md` - Main documentation index
- `src/quickstart.md` - Quick start guide
- `src/installation.md` - Installation instructions
- `src/backends/` - Backend-specific documentation
- `src/security/` - Security documentation
- `src/specs/` - Specifications

## 🔄 For New Team Members

**Read in this order:**
1. [CODEBASE_ANALYSIS.md](CODEBASE_ANALYSIS.md) - Understand the platform
2. [../AGENTS.md](../AGENTS.md) - Repository guidelines
3. [design/metadata-service-design.md](design/metadata-service-design.md) - Current focus
4. [CONTEXT_SYNC_GUIDE.md](CONTEXT_SYNC_GUIDE.md) - Multi-computer workflow

## 🎯 For AI Assistants

The `.cursor/rules` file in the project root contains all necessary context and automatically references these documents.

---

**Last Updated:** November 5, 2025



