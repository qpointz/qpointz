# WI-042 — AI v3 External Capability Asset Descriptors

Status: `superseded`  
Type: `✨ feature`  
Area: `ai`, `platform`  
Backlog refs: **A-31**

> **Superseded by** [`WI-326`](../ai-v3-mcp-server-poc/WI-326-external-capability-asset-descriptors.md)
> in story [`ai-v3-mcp-server-poc`](../ai-v3-mcp-server-poc/STORY.md). Descriptor work is delivered
> as part of the MCP server POC, not the `ai-v3` chat integration story.

## Problem Statement

Future MCP resources need descriptive metadata so external clients can understand what a resource represents.

## Goal

Define the descriptor model for externally exposed capability assets.

## In Scope

1. Define descriptor metadata for protocols, prompts, manifests, examples, and artifact schemas.
2. Ensure resources are self-describing and versionable.

## Out of Scope

- Full MCP server behavior.

## Acceptance Criteria

- Externally exposed capability resources have an explicit descriptor model.
- Resource descriptors are sufficient for clients to identify resource kind, ownership, and interpretation.

## Deliverables

- Fulfilled by **WI-326** on the `ai-v3-mcp-server-poc` story branch.
