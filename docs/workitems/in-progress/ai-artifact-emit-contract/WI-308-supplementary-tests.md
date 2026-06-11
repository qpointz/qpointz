# WI-308 — Supplementary unit and service tests

Status: `planned`  
Type: `🧪 test`  
Area: `ai`  
Story: [`STORY.md`](STORY.md)

## Depends on

- **WI-305–307** (emit + scenario acceptance).

## Goal

Secondary test coverage outside the scenario harness — unit tests and HTTP SSE smoke.

## Deliverables

- [ ] [`AiChatControllerIT`](../../../../ai/mill-ai-service/src/testIT/kotlin/io/qpointz/mill/ai/service/AiChatControllerIT.kt):
  - Profile list includes `data-analysis` (four profiles, sorted by id)
  - Stub runtime emits `StructuredPart` for `sql` + `facet-proposal`; assert SSE JSON shape
- [ ] Any remaining `:ai:mill-ai:test` gaps from WI-303–305 not covered by scenarios
- [ ] mill-ui: existing [`chatArtifactParse.test.ts`](../../../../ui/mill-ui/src/utils/__tests__/chatArtifactParse.test.ts) still passes (no regression)

## Acceptance criteria

- [ ] `./gradlew :ai:mill-ai:test :ai:mill-ai-service:testIT :ai:mill-ai-autoconfigure:test` pass.
- [ ] Scenario packs (WI-307) remain the **primary** acceptance path; this WI does not duplicate full emit E2E in service IT.

## Out of scope

- HTTP `ScenarioPack` runner (follow-up story).
- Live OpenAI scenario packs.
