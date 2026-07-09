# WI-401 — Analysis copilot automation radio

**Status:** complete

## Goal

Replace twin Auto Apply / Auto Run boolean switches with a single **Auto** radio:
`manual` | `apply` | `run`.

## Delivered

- `AnalysisCopilotSettings['automation.mode']` — removed legacy `autoapply.enabled` /
  `autorun.enabled`
- `analysisCopilotAutomation.ts` — `shouldAutoApplyOnArrival`, `shouldAutoRunAfterApply`
- Header settings menu — Radio.Group in `InlineChatCopilotSettingsMenu`
- `QueryPlayground` — first SQL proposal per turn only; strip actions unchanged
- Docs: `GAPS.md`, `INLINE-CHAT-FOUNDATION.md`

## Verify

```bash
cd ui/mill-ui && npx vitest run src/components/inline-chat/__tests__/analysisCopilotAutomation.test.ts src/components/inline-chat/__tests__/copilotHeaderSettingsRegistry.test.ts
```
