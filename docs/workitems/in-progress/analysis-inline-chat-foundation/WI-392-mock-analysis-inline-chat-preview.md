# WI-392 - Mock Analysis copilot preview

## Goal

Create an early Analysis copilot UX preview so users can manually test how the interaction
looks and feels before backend profile/capability work is available.

## Status

**Complete** — mock `sendMessage` uses keyword triggers (`optimize` → prose, `rewrite`/`proposal` →
SQL strip fixture); Analysis create uses `analysis-copilot` profile id.

## Requirements

- Use the existing `ui/mill-ui` mock chat and mock Analysis services where they are sufficient.
- Ensure the Analysis copilot inline chat can be opened from the Analysis view in a local mock
  configuration.
- Focus on user interaction and visual feel, not backend-perfect mock fidelity.
- Prioritize manual product review over complete mock fidelity; the preview should feel good even
  if its data plumbing is intentionally incomplete.
- Add deterministic or locally synthesized preview responses that exercise the intended UX:
  - prose-only query advice
  - SQL proposal response
  - SQL proposal with title and description suitable for an inline artifact strip
- Emit, synthesize, or locally fixture SQL proposal state only as needed to make the UI preview
  credible.
- Prefer local fixtures or synthesized proposal state over a fragile attempt to mirror future
  streaming/profile backend behavior.
- Avoid fragile mock backend changes that try to perfectly emulate future SSE/profile behavior.
- Keep the preview path clearly mock/dev-only and easy to remove or replace when real backend
  support is wired.
- Avoid implementing final backend contracts in this WI.
- In mock mode, synthesize or accept the `analysis-copilot` profile id so the UI can be tested
  before the backend profile exists.

## Acceptance Criteria

- A developer can run `ui/mill-ui` locally and open the Analysis copilot in a mock/dev preview.
- The mock Analysis copilot shows representative assistant prose in the drawer.
- At least one mock turn displays a SQL proposal in the planned inline artifact strip presentation.
- The preview feels polished enough for manual product review even if the mock plumbing is
  intentionally incomplete.
- The preview can be used to evaluate:
  - drawer width and density
  - artifact label/title/description readability
  - action placement for Apply, Apply & Run, and Copy
  - how the chat feels next to the Analysis SQL editor
  - whether the flow feels light enough for repeated query authoring
- The preview does not introduce brittle mock backend behavior solely to mimic future SSE details.
- General Chat mock behavior is unchanged.
- Mock create-chat metadata for the Analysis copilot records `analysis-copilot` as the profile id.

## Verification

```bash
cd ui/mill-ui && npm run test -- --run
cd ui/mill-ui && npm run dev
```

## Manual Verification

With the dev server running in mock mode:

- Navigate to `/analysis`.
- Open the Analysis copilot drawer.
- Send one mock prompt that returns prose-only advice.
- Send one mock prompt that returns a SQL proposal.
- Confirm the SQL proposal appears as a compact inline artifact strip suitable for the drawer.
- Confirm the UI is good enough to evaluate before backend work:
  - drawer width and vertical spacing
  - artifact title and description readability
  - Apply, Apply & Run, and Copy action placement
  - interaction with the SQL editor beside the drawer
  - whether opening, reading, applying, and dismissing proposals feels natural
  - no text overlap or clipped controls at desktop and narrow widths
