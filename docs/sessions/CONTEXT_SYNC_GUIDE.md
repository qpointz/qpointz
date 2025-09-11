# Context Sync Guide - Working Across Multiple Computers

This guide explains how to sync your work context between your **Windows desktop** and **Linux laptop**.

## 🎯 Quick Start

### Setup (One Time)

**On Windows Desktop (now):**
```powershell
# Commit everything to git
git add .cursor/ CODEBASE_ANALYSIS.md docs/design/ sync-context.ps1 sync-context.sh
git commit -m "[docs] Add project documentation and context sync system"
git push
```

**On Linux Laptop (when you get there):**
```bash
# Pull everything
git pull

# Make sync script executable
chmod +x sync-context.sh
```

---

## 📱 Daily Workflow

### 🏠 Starting Work (Any Computer)

**Windows Desktop:**
```powershell
.\sync-context.ps1
```

**Linux Laptop:**
```bash
./sync-context.sh
```

This will:
1. ✅ Pull latest changes from git
2. 📋 Show current work context
3. 🌿 Show current branch
4. 💡 Display next steps

### 🚀 When Opening Cursor

Cursor automatically reads `.cursor/rules` - you're ready to go!

**Optional:** For detailed context, tell the AI:
```
Read .cursor/context.md and continue from where I left off
```

### 💾 Finishing Work (Before Switching Computers)

#### 1. Update Context

Edit `.cursor/context.md` and add your work log at the top of "Recent Work Log":

```markdown
## Recent Work Log

### 2025-11-06 (Desktop - Windows)
**Status:** Implementing Phase 1 - Core Foundation
**Completed:**
- Created mill-metadata-core module
- Implemented MetadataEntity
- Added FacetRegistry

**In Progress:**
- Working on StructuralFacet
- File: core/mill-metadata-core/.../StructuralFacet.java (line 45)

**Next Steps:**
- Complete remaining core facets
- Test FacetRegistry plugin system

**Current Branch:** feature/metadata-service

**Notes:**
- FacetRegistry works well, need to add validation
```

#### 2. Push Context

**Windows:**
```powershell
.\sync-context.ps1 -Push
```

**Linux:**
```bash
./sync-context.sh push
```

This will:
1. Show changed files
2. Ask for commit message (or use default)
3. Commit `.cursor/context.md`
4. Push to git

**Done!** Your context is now available on the other computer.

---

## 📋 Complete Example Scenario

### Day 1 - Windows Desktop (Morning)

```powershell
# Start work
.\sync-context.ps1

# Work in Cursor...
# Implement MetadataEntity class

# Before lunch (switching to laptop)
# 1. Update .cursor/context.md
notepad .cursor\context.md

# 2. Push
.\sync-context.ps1 -Push
```

### Day 1 - Linux Laptop (Afternoon)

```bash
# At cafe with laptop
./sync-context.sh

# Opens Cursor, tells AI:
# "Read .cursor/context.md and continue from where I left off"

# Continue work on MetadataEntity...

# Evening - heading home
# 1. Update context
vim .cursor/context.md

# 2. Push
./sync-context.sh push
```

### Day 2 - Windows Desktop (Morning)

```powershell
# Back at desktop
.\sync-context.ps1

# Context shows yesterday's laptop work
# Continue seamlessly...
```

---

## 🎨 Context Update Template

Use this template when updating `.cursor/context.md`:

```markdown
### YYYY-MM-DD (Computer - OS)
**Status:** [Current phase/feature you're working on]

**Completed:**
- Finished feature X
- Fixed bug Y
- Wrote tests for Z

**In Progress:**
- Currently working on [specific task]
- File: path/to/file.java (line 123)
- Debugging [specific issue]

**Next Steps:**
- Next thing to do
- After that, do this
- Then test this

**Current Branch:** feature/branch-name

**Files Modified:**
- core/module/src/.../File1.java
- core/module/src/.../File2.java

**Notes:**
- Important context or decisions
- Things to remember
- Issues encountered
```

---

## 🔧 Troubleshooting

### Merge Conflicts in `.cursor/context.md`

This can happen if you update context on both computers without syncing.

**Solution:**
```bash
# Pull with rebase
git pull --rebase

# If conflict, edit .cursor/context.md manually
# Keep both entries (they're in chronological order anyway)
vim .cursor/context.md

# Continue rebase
git add .cursor/context.md
git rebase --continue
git push
```

### Forgot to Push Before Switching

If you forgot to push from Computer A and already started on Computer B:

**On Computer B:**
```bash
# Commit your current work with a temporary message
git add -A
git commit -m "[wip] Work from computer B - will merge"
git push
```

**On Computer A:**
```bash
# Pull and merge
git pull
# Resolve any conflicts
git push
```

### Script Won't Run (Linux)

Make it executable:
```bash
chmod +x sync-context.sh
```

### Git Pull Fails

Check for uncommitted changes:
```bash
git status
git stash           # Save current work
git pull            # Pull latest
git stash pop       # Restore your work
```

---

## 🎯 Best Practices

### ✅ Do

- **Always sync before starting work** (`./sync-context.sh` or `.\sync-context.ps1`)
- **Update context when switching computers** (not after every commit)
- **Use descriptive "In Progress" entries** (include file paths and line numbers)
- **Keep branch info current** (helps you remember where you were)
- **Add notes about decisions** (why you chose approach X over Y)

### ⚠️ Don't

- **Don't update context for tiny changes** (only when switching computers)
- **Don't forget to push** before switching
- **Don't edit old entries** (add new ones on top instead)
- **Don't commit broken code** (finish the thought first)

---

## 📊 What Gets Synced

| Item | Synced? | How |
|------|---------|-----|
| `.cursor/rules` | ✅ Yes | Automatic (git) |
| `.cursor/context.md` | ✅ Yes | Manual updates + git |
| Code changes | ✅ Yes | Normal git workflow |
| Cursor chat history | ❌ No | Local only |
| Cursor settings | ❌ No | Local only |
| Open files | ❌ No | Context log instead |
| Cursor extensions | ❌ No | Install on both |

---

## 🎪 Advanced: Automatic Context Snippets

### Add Git Hooks (Optional)

**Linux: `.git/hooks/pre-push`**
```bash
#!/bin/bash
# Remind to update context
if git diff --name-only origin/$(git rev-parse --abbrev-ref HEAD) | grep -q "\.cursor/context\.md"; then
    echo "✅ Context file updated"
else
    echo "⚠️  Don't forget to update .cursor/context.md!"
    read -p "Continue push? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi
```

Make executable:
```bash
chmod +x .git/hooks/pre-push
```

---

## 🆘 Emergency: Quick Context Transfer

If scripts don't work, manually sync:

```bash
# Computer A: Show what you were doing
cat .cursor/context.md

# Computer B: Copy-paste the context
vim .cursor/context.md
# Paste + add your own entry

# Commit
git add .cursor/context.md
git commit -m "[docs] Sync context from other computer"
git push
```

---

## 📚 Related Files

- [.cursor/rules](.cursor/rules) - Main AI configuration (auto-loaded)
- [.cursor/context.md](.cursor/context.md) - Work context log
- [.cursor/README.md](.cursor/README.md) - About .cursor directory
- [CODEBASE_ANALYSIS.md](CODEBASE_ANALYSIS.md) - Architecture overview
- [docs/design/metadata-service-design.md](docs/design/metadata-service-design.md) - Current feature design

---

## 💡 Pro Tips

1. **Commit often, push when switching** - Keep git commits atomic, but push context only when switching computers

2. **Use branches for features** - Makes it easier to track what you're working on

3. **Include line numbers** - "File: StructuralFacet.java (line 45)" helps you resume exactly where you left off

4. **Note decisions** - "Chose approach X because Y" helps future you understand

5. **Update on context switch** - Only update `.cursor/context.md` when switching computers, not after every small change

6. **Keep it brief** - Context should be scannable in 30 seconds

7. **AI reminder phrase** - Use the same phrase: "Read .cursor/context.md and continue from where I left off"

---

**Last Updated:** November 5, 2025  
**Tested On:** Windows 10/11 (PowerShell 5.1+), Linux (bash 4.4+)  
**Status:** Ready to use! 🚀

