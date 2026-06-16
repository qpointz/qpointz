# WI-300 — Conversation scenario design docs

Status: `done`  
Type: `📐 design`  
Area: `ai`, `docs`  
Story: [`STORY.md`](STORY.md)

## Depends on

- None (first WI).

## Goal

Author normative design for the greenfield conversation replay harness and sketch the artefact emit contract. Create the story folder (this file + siblings).

## Deliverables

- [x] [`docs/design/agentic/ai-v3-conversation-scenarios.md`](../../../design/agentic/ai-v3-conversation-scenarios.md):
  - `ScenarioPack` YAML spec (`ask` / `verify` / `script` with `toolCalls` / `answer`)
  - `ConversationRegressionRecord` JSON schema (`schemaVersion: 1`), normalization rules, baseline workflow
  - `TurnCheckRegistry` extension guide
  - Offline re-check from saved records
  - Future `ai:v3-integration` CI job (see [`ai-v1-integration/README.md`](../../../design/ai/ai-v1-integration/README.md))
  - Relationship to [`v3-validation-harness.md`](../../../design/agentic/v3-validation-harness.md)
- [x] [`docs/design/agentic/artifact-emit-contract.md`](../../../design/agentic/artifact-emit-contract.md):
  - `ArtifactDescriptor` canonical fields (see STORY.md **Design decisions**)
  - Emission strategies + payload source rules (`OnToolSuccess` = direct construct; `OnCaptureSuccess` = protocol executor)
  - Tool-result vs protocol-final persistence (no duplicate `sql.generated`)
  - Profile gating via `capabilityIds`
  - Wire table (`partType`, persist kinds, pointers, `sourceEvent`)
  - Cross-profile reuse (`data-analysis` + `schema-authoring` sharing `sql-query`)
- [x] Story folder [`docs/workitems/planned/ai-artifact-emit-contract/`](.) with `STORY.md` + WI-300–309.

### Reference YAML example (include in design doc)

```yaml
name: data-analysis-sql-emit
profileId: data-analysis
parameters:
  mode: scripted
run:
  - ask: "Show sales by region"
    script:
      # Step 1: agent loop iteration 0 — model returns toolCalls
      - toolCalls:
          - name: validate_sql
            args:
              sql: "SELECT region, SUM(amount) FROM sales GROUP BY region"
              attempt: 1
      # Step 2: agent loop iteration 1 — after tool result + coordinator ProtocolFinal
      - answer: ""
    verify:
      pass: ERROR
      check:
        - events:
            containsInOrder:
              - { type: tool.call, name: validate_sql }
              - { type: tool.result, name: validate_sql }
              - { type: protocol.final, protocolId: sql-query.generated-sql }
        - artifacts:
            - persistKind: sql.validation
              count: 1
            - persistKind: sql.generated
              count: 1
        - sse:
            - type: item.part.updated
              presentation: structured
              partType: sql
```

CAPTURE (facet) example — **three script steps** (toolCalls + protocol executor model call + optional answer not needed):

```yaml
  - ask: "Propose a descriptive facet for urn:mill/model/moneta/clients"
    script:
      - toolCalls:
          - name: propose_facet_assignment
            args:
              facetTypeKey: descriptive
              metadataEntityId: "urn:mill/model/moneta/clients"
              payload: { displayName: "Clients" }
              rationale: "Inferred from schema"
      - answer: '{"facetTypeKey":"descriptive","metadataEntityId":"urn:mill/model/moneta/clients","payload":{"displayName":"Clients"}}'
    verify:
      pass: ERROR
      check:
        - events:
            containsInOrder:
              - { type: tool.call, name: propose_facet_assignment }
              - { type: protocol.final, protocolId: metadata.faceting.capture }
```

The second `answer` step is the **protocol executor's** structured JSON response (see WI-302 script semantics).

## Acceptance criteria

- [x] Design docs committed; no implementation code required in this WI beyond story/WI markdown.
- [x] `script` semantics documented: stubs **planner + protocol executor only**; runtime produces tool results, coordinator emissions, artefacts.
- [x] Regression record fields + **normalization rules** documented (UUIDs, ids, timestamps, token stats — see WI-301).
- [x] Descriptor schema acceptance criteria listed before WI-303 implementation (canonical fields in STORY.md).
