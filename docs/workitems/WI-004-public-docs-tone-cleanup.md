# WI-004: Public Documentation Tone and Formatting Cleanup

**Type:** docs
**Priority:** medium
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `docs/wi-004-public-docs-tone-cleanup`

---

## Goal

Rewrite `docs/public/src/` markdown files to use a neutral, user-documentation style.
Remove marketing language, promotional phrases, emojis, calls-to-action, and audience
segmentation ("Who Is Mill For?"). The result should read like technical product
documentation — factual, concise, and structured for reference.

## Current Problems

### Marketing tone throughout

The landing page (`index.md`) reads like a product brochure:

- "Your AI-Powered Data Assistant"
- "Ask questions in plain English. Get instant answers from your data."
- "Mill transforms how you interact with data"
- "What Makes Mill Special"
- "Why Choose Mill?"
- "Ready to transform how you work with data? Get started today →"

### Emoji headers

Section headers use emojis as decoration:

- `## 🚀 What Makes Mill Special`
- `## 💡 Key Capabilities`
- `## 🎯 Who Is Mill For?`
- `## 🌟 Why Choose Mill?`
- `## 🗨️ Chat View`
- `## 🗂️ Data Model View`
- `## 🆘 Getting Help`

### Audience segmentation / persona sections

`index.md` contains a "Who Is Mill For?" section with marketing personas
(Business Analysts, Data Teams, Domain Experts, Developers) — each with
promotional copy. This is not appropriate for user documentation.

### Call-to-action footers

Multiple pages end with promotional CTAs:

- `index.md`: "Ready to transform how you work with data? Get started today →"
- `quickstart.md`: "Ready to explore? Start with Moneta to see Mill in action..."
- `grinder-ui.md`: "Ready to explore? Open the Grinder UI and start asking questions..."

### Promotional product descriptions

`quickstart.md` contains a multi-paragraph marketing pitch for Moneta:

> "Our AI-powered data chat assistant enables business users, analysts, and domain
> experts to ask questions about their data using plain language — and instantly
> receive structured, reliable answers..."
>
> "Designed for flexibility, it supports both reasoning and retrieval-augmented (RAG)
> workflows..."
>
> "Whether you're exploring KPIs, validating assumptions, or preparing reports —
> this assistant turns your data into actionable answers without needing technical
> expertise."

### Minor promotional phrasing in otherwise neutral pages

A few backend/source pages have soft promotional phrases:

- `backends/index.md`: "pick the one that fits your data"
- `backends/jdbc.md`: "This is the natural choice when..."
- `backends/calcite.md`: "The Calcite backend is the most flexible option"

These are minor but should be neutralized for consistency.

---

## Files and Required Changes

### Severity: High (major rewrite)

#### `src/index.md`

Current: full marketing landing page with emojis, persona sections, CTAs,
promotional superlatives.

Target: neutral product overview. Should contain:

- One-paragraph description of what Mill is (data access platform, unified SQL,
  AI-assisted NL queries).
- Architecture diagram (reuse existing ASCII art from `backends/index.md`).
- Feature summary as a flat list or table — no promotional prose per feature.
- Links to installation, quickstart, backends, and client docs.

Remove entirely:

- "What Makes Mill Special" section
- "Who Is Mill For?" section (personas)
- "Why Choose Mill?" section
- "Key Capabilities" section (fold into concise feature list)
- All emoji prefixes in headers
- CTA footer

#### `src/quickstart.md`

Current: marketing pitch for Moneta, emoji header, CTA footer.

Target: step-by-step quickstart guide.

Changes:

- Remove emoji from `## 🚀 Moneta: Ready-to-Use Example` header.
- Replace "The fastest way to experience Mill" with neutral intro.
- Replace the three-paragraph marketing description of Moneta ("Our AI-powered
  data chat assistant...") with a concise factual description (1-2 sentences:
  what Moneta is, what data it includes, what it demonstrates).
- Remove CTA footer ("Ready to explore? Start with Moneta...").
- Keep all Docker commands, LLM provider instructions, and sample queries as-is —
  these are good technical content.

#### `src/grinder-ui.md`

Current: emojis in all section headers, subtitle "Your Data Exploration Interface",
CTA footer, some promotional phrasing.

Target: UI reference documentation.

Changes:

- Remove subtitle "Your Data Exploration Interface" from the title — just
  "Grinder UI" or "Mill Grinder UI".
- Replace intro paragraph ("makes it easy to explore your data..." / "provides
  an intuitive experience") with factual description of what the UI is.
- Remove all emoji prefixes from section headers:
  - `## 🗨️ Chat View` → `## Chat View`
  - `## 🗂️ Data Model View` → `## Data Model View`
  - `## 💡 Context View` → `## Context View`
  - `## 🎨 Interface Features` → `## Interface Features`
  - `## 💡 Tips for Best Results` → `## Tips`
  - `## 🔍 Common Use Cases` → `## Common Use Cases`
  - `## 🆘 Getting Help` → `## Troubleshooting`
- Remove CTA footer ("Ready to explore? Open the Grinder UI...").
- Keep all content about commands, @ mentions, response types, data model
  navigation, keyboard shortcuts — this is useful reference material.

### Severity: Low (minor phrasing)

These files are already well-written technical documentation. Only isolated
phrases need adjustment.

#### `src/backends/index.md`

- "pick the one that fits your data" → remove or rephrase neutrally
  (e.g. "Select the backend that matches your data source").

#### `src/backends/jdbc.md`

- "This is the natural choice when your data already lives in..." →
  "Use this backend when data is stored in..." or similar neutral phrasing.

#### `src/backends/calcite.md`

- "The Calcite backend is the most flexible option — anything Calcite can
  connect to, Mill can query." → "The Calcite backend supports any data source
  that Calcite can connect to."
- "The model file is a standard Calcite artifact, so existing Calcite
  configurations work without modification." → keep, this is factual.

### Severity: None (no changes needed)

The following files are already neutral, well-structured technical documentation:

- `src/installation.md` (stub — "Coming soon")
- `src/sources/index.md`
- `src/sources/configuration.md`
- `src/sources/types.md`
- `src/sources/formats/csv.md`
- `src/sources/formats/tsv.md`
- `src/sources/formats/fwf.md`
- `src/sources/formats/parquet.md`
- `src/sources/formats/avro.md`
- `src/sources/formats/excel.md`
- `src/connect/python/index.md`
- `src/connect/python/installation.md`
- `src/connect/python/authentication.md`
- `src/connect/python/discovery.md`
- `src/connect/python/querying.md`
- `src/connect/python/types.md`
- `src/connect/python/dataframes.md`
- `src/connect/python/async.md`
- `src/backends/flow.md`

---

## Style Guidelines

Apply these rules consistently across all modified files:

1. **No emojis** in headers or body text.
2. **No CTAs** — no "Ready to explore?", "Get started today →", etc.
3. **No superlatives** — no "the fastest", "the most flexible", "the natural
   choice", "special", "beautiful", "intelligent".
4. **No persona/audience sections** — do not address specific user roles
   ("Business Analysts", "Data Teams"). Write for a general technical reader.
5. **No promotional paragraphs** — every paragraph should convey factual
   information. If a paragraph can be removed without losing any technical
   content, remove it.
6. **Neutral link text** — "See [Installation](installation.md)" not
   "Get started today → [Installation](installation.md)".
7. **Consistent heading style** — plain text, no emoji, no colon-suffixed
   subtitles (e.g. "Mill: Your AI-Powered Data Assistant" → "Mill").
8. **Keep technical content intact** — configuration examples, YAML snippets,
   code blocks, property tables, type mappings, architecture diagrams are all
   good. Do not remove or shorten these.

---

## Verification

1. All 25 markdown files in `docs/public/src/` reviewed.
2. No emoji characters remain in any header (`##`, `###`, etc.).
3. No CTA footers remain (grep for "Ready to", "Get started", "→").
4. No persona/audience sections remain (grep for "Who Is .* For").
5. No superlative marketing phrases remain in modified files.
6. MkDocs build succeeds (`cd docs/public && mkdocs build`).
7. All internal links still resolve (no broken cross-references).
8. Technical content (config examples, code blocks, tables, diagrams) unchanged.

## Estimated Effort

Small — 3 files need substantial rewriting, 3 files need minor phrase edits,
18 files need no changes.
