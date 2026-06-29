# WI-364 — Migrate mill-ui Mantine 8.x → 9.x

Status: `done`  
Type: `🔧 chore`, `✨ feature`  
Area: `ui`  
Depends on: [WI-362](WI-349-metadata-authoring-tests-docs.md) (stage 4 merged — story baseline on `dev`)  
**Stage:** **5** — branch `feat/mill-ui-mantine-9` (**separate MR** from WI-363; see [`STORY.md`](STORY.md))

## Problem Statement

**mill-ui** pins **`@mantine/*` at ^8.3.18** while Mantine **9.x** is the current major line. Staying on 8.x
blocks React 19.2+ alignment, upstream security fixes, and new component APIs. Mantine 9 introduces
widespread **prop renames** and **theme defaults** that must be applied repo-wide (~100+ component files).

This WI is **UI-only** and **orthogonal** to WI-363 (capability prompt declaration). It ships as a
**separate work item and MR** within stage 5 so prompt refactors are not blocked by a large UI migration.

## Goal

Upgrade **`ui/mill-ui`** to **Mantine 9.x** (latest stable 9.x at implementation time), fix all breaking
changes, and keep **build**, **lint**, and **Vitest** green with no visual regressions on primary routes.

## Normative references

- [Mantine 8.x → 9.x migration guide](https://mantine.dev/guides/8x-to-9x/)
- [Mantine 9.0.0 changelog](https://mantine.dev/changelog/9-0-0/)

## Prerequisites (locked)

| Requirement | Today (`mill-ui`) | Target |
| ----------- | ----------------- | ------ |
| **React** | ^19.1.0 | **19.2+** (Mantine 9 peer) |
| **`@mantine/core`**, **hooks**, **notifications**, **code-highlight** | ^8.3.18 | **^9.x** (aligned versions) |
| **PostCSS** | `postcss-preset-mantine` / `postcss-simple-vars` | Verify versions compatible with Mantine 9 docs |

**Not used in mill-ui today:** `@mantine/form`, `@mantine/tiptap`, `@mantine/charts` — no action unless added during migration.

## In Scope

1. **Dependency bump** — all `@mantine/*` packages to 9.x; React / `@types/react*` to 19.2+ if required
2. **Codemod / mechanical fixes** — apply migration guide breaking changes across `ui/mill-ui/src/`:
   - `Text` / `Anchor` **`color`** → **`c`**
   - **`Collapse`** **`in`** → **`expanded`**
   - **`Grid`** **`gutter`** → **`gap`** (and related layout props per guide)
   - **`Spoiler`** **`initialState`** → **`defaultExpanded`**
   - **`TypographyStylesProvider`** → **`Typography`** (if present)
   - Remove deprecated **`positionDependencies`** on Popover/Tooltip (if present)
3. **Theme** — review `src/theme/` for Mantine 9 CSS variables (`light` variant, **`defaultRadius`** change sm→md); preserve v8 look via theme overrides if product requires pixel-parity
4. **PostCSS / Vite** — update config per Mantine 9 install docs if package versions change
5. **Tests** — fix Vitest + Testing Library mocks (`MantineProvider`, etc.) for API changes
6. **Smoke routes** — manual checklist: chat, data model, query playground, admin, auth, AppShell chrome

## Out of Scope

- WI-363 capability / prompt YAML changes
- mill-grinder-ui (legacy / retired)
- Redesign or new UI features beyond migration parity
- Story archive / MILESTONE / BACKLOG (explicit user request only)

## Acceptance Criteria

- [x] `ui/mill-ui/package.json` — all `@mantine/*` at **9.x**; React **19.2+**
- [x] `npm run build` succeeds in `ui/mill-ui`
- [x] `npm run lint` succeeds
- [x] `npm run test -- --run` — full Vitest suite green
- [x] No remaining imports from Mantine 8 deprecated APIs (grep: `TypographyStylesProvider`, `color=`, `gutter=`, `Collapse in=`)
- [x] Theme documented if `defaultRadius` or `v8CssVariablesResolver` retained for visual parity — **Mantine 9 defaults**; existing `defaultRadius: 'md'` in theme unchanged
- [x] Short migration note in `ui/mill-ui/README.md` or `docs/design/ui/` (breaking renames + theme choices) — **only if** no existing UI design doc covers dependencies

## Verify

```bash
cd ui/mill-ui
npm install
npm run lint
npm run build
npm run test -- --run
```

Optional root shortcut: `make -C ui test` / `make -C ui build` (if defined).

## Suggested commit

`[change] WI-364: migrate mill-ui to Mantine 9`
