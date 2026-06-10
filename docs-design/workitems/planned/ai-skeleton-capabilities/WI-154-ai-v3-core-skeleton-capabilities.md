# WI-154 — Core Skeleton Capabilities and Manifests

Status: `planned`  
Type: `💡 improvement` / `🧪 test`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

`ConversationCapability` and `DemoCapability` are intentionally minimal but should remain
**coherent exemplars**: their YAML manifests, prompt/protocol assets, and `ServiceLoader`
registration should match the patterns used by production capabilities and be safe to extend.
They anchor **hello-world** and **minimal chat** profiles used while bootstrapping v3 toward **v1
parity** (conversation prompts, protocol shape).

## Goal

Polish the **core skeleton** capabilities so they demonstrate best practices (manifest structure,
prompt assets, empty vs non-empty tool lists, protocol definitions) and have tests that prevent
accidental breakage — keeping extension points aligned with the parity matrix from **WI-151**.

## In Scope

1. Align `ConversationCapability` / `DemoCapability` (and related resources under
   `capabilities/`) with conventions established in production capabilities (e.g. **WI-156**
   manifest patterns once merged).
2. Add focused tests (e.g. manifest load, descriptor IDs, provider discovery) for these providers.
3. Update short inline or design-doc notes so new capabilities can copy this pattern.

## Out of Scope

- New business features in demo flows (unless required for consistency).

## Acceptance Criteria

- Skeleton capabilities load reliably in CLI and test contexts; manifest or ID drift is caught by tests.
- Documentation pointer (KDoc or design note cross-link) explains when to extend skeleton vs add a new provider.

## Deliverables

- This work item definition.
- Code, resources, and tests on the story branch per `docs/workitems/RULES.md`.
