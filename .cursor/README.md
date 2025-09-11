# .cursor Directory

This directory contains Cursor IDE-specific configuration files for the Mill project that should be version controlled and shared across the team.

## Files

### `rules`
**Primary AI assistant configuration file.**

Contains:
- Project structure and organization
- Module descriptions
- Coding conventions (Java, testing, commits)
- Build commands and workflows
- Key technologies and versions
- Current development focus
- Common patterns and best practices

**When to update:**
- New module added
- Architecture changes
- Convention changes
- Major technology upgrades

**Usage:**
Cursor AI automatically reads this file to understand the project context. This is the **main reference** for all AI interactions.

### `context.md`
**Quick context bundle for resuming work.**

Contains:
- Links to essential documentation
- Current work status
- Recent changes
- Quick reference guides
- Common tasks

**When to update:**
- Starting new feature/sprint
- Major milestone reached
- Switching focus areas
- After significant commits

**Usage:**
Use this when starting a new session or switching computers. Tells AI assistant what's currently being worked on.

### This file (`README.md`)
Documentation about the `.cursor/` directory itself.

## Why Version Control This?

✅ **DO commit** `.cursor/` to git because:
- Shares project context with team members
- Provides consistent AI assistance across computers
- Documents current work and conventions
- Helps onboard new developers
- Preserves institutional knowledge

❌ **DON'T commit** `$HOME/.cursor/` (global Cursor directory) because:
- Contains computer-specific paths
- Includes cache and temporary files
- May contain sensitive data (API keys)
- Is machine-specific configuration

## Usage for Team Members

### Setting Up on New Computer

1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd mill
   ```

2. Cursor will automatically read `.cursor/rules`

3. For additional context, tell AI:
   ```
   Read .cursor/context.md and load the current work context
   ```

### When Starting a New Session

Simply open the project in Cursor. The AI will automatically use `.cursor/rules` for context.

For resuming specific work:
```
Read .cursor/context.md for current work status
```

### Updating These Files

**Update `.cursor/rules` when:**
- Adding new modules
- Changing project structure
- Updating dependencies
- Establishing new conventions
- Documenting new patterns

**Update `.cursor/context.md` when:**
- Starting new feature work
- Completing major milestones
- Switching development focus
- After architectural decisions

**Commit message format:**
```bash
git add .cursor/
git commit -m "[docs] Update cursor rules for metadata service"
```

## Integration with Other Documentation

The `.cursor/` directory complements:

| File | Purpose | Relationship |
|------|---------|--------------|
| `.cursor/rules` | AI assistant config | References other docs |
| `CODEBASE_ANALYSIS.md` | Architecture overview | Referenced by rules |
| `docs/design/*.md` | Design documents | Referenced by context |
| `AGENTS.md` | Repository guidelines | Included in rules |
| `README.md` | Project overview | Summarized in rules |

## Best Practices

### 1. Keep Rules Updated
The `.cursor/rules` file should always reflect current project state. Update it when making significant changes.

### 2. Reference, Don't Duplicate
Use references to detailed documentation rather than duplicating content:
```markdown
✅ Good: "See docs/design/metadata-service-design.md for details"
❌ Bad: Copying entire design doc into rules
```

### 3. Focus on Context
Provide what AI needs to understand the project:
- Structure and organization
- Key conventions
- Current focus
- Where to find details

### 4. Update Context Regularly
Keep `.cursor/context.md` current with recent work:
- Update when starting new features
- Update after major commits
- Update when switching focus

### 5. Use Links
Link to other documentation files so AI can navigate:
```markdown
[CODEBASE_ANALYSIS.md](../CODEBASE_ANALYSIS.md)
```

## File Format

Both files use Markdown for:
- Easy reading by humans and AI
- Version control friendly (diffs work well)
- Rich formatting (tables, code blocks, links)
- Portable across platforms

## Maintenance

**Owner:** Development team (shared responsibility)

**Review frequency:** 
- `.cursor/rules` - Review quarterly or on major changes
- `.cursor/context.md` - Update weekly or per feature

**Keeping in sync:**
- Rules should match actual codebase
- Context should reflect current work
- Links should be valid
- Commands should be tested

## Example Workflow

### Developer A (working on metadata service)

```bash
# Make changes, update context
vim .cursor/context.md  # Update current work section

git add .cursor/context.md
git commit -m "[docs] Update cursor context for metadata service Phase 1"
git push
```

### Developer B (different computer, continuing work)

```bash
git pull

# Cursor automatically reads .cursor/rules
# For specific context:
# Tell AI: "Read .cursor/context.md for current status"
```

## Tips for AI Assistant

When working with Cursor AI on this project:

1. **On first interaction:** Rules are already loaded automatically
2. **For current work:** Ask to read `.cursor/context.md`
3. **For architecture:** Reference `CODEBASE_ANALYSIS.md`
4. **For specific feature:** Reference relevant design doc in `docs/design/`

## Version History

| Date | Change | Reason |
|------|--------|--------|
| 2025-11-05 | Initial creation | Setup cursor context for metadata service work |

---

**Note:** This directory is part of the project repository and should be committed to version control.

