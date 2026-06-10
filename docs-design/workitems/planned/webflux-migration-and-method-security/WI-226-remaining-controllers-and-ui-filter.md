# WI-226 — Remaining controllers and UI WebFilter

Status: `planned`  
Type: `refactoring`  
Area: `platform`, `ai`, `ui`  
Backlog refs: **P-34**, **P-1**, **P-2**

## Goal

Complete WebFlux migration for remaining HTTP surfaces in this story: **NlSql chat**, **application descriptor** (`.well-known`), and **UI SPA routing** implemented as a **`WebFilter`** instead of a servlet `Filter`.

## Scope

1. **`mill-ai-nlsql-chat-service`**: finish partial WebFlux migration — reactive `NlSqlChatService` API + **`NlSqlChatController`** `Mono`/`Flux` returns (including SSE/streaming paths).
2. **`core/mill-starter-service`**: **`ApplicationDescriptorController`** reactive return type.
3. **`services/mill-ui-service`**: replace **`MillUiSpaRoutingFilter`** with **`MillUiWebFilter`** (path rewrite, redirects, 405 for non-GET SPA routes) per plan.
4. Any additional REST controllers **discovered in WI-220** that belong to the **same deployed apps** as this story but were not covered by **WI-224**/**WI-225**—either migrate here or explicitly defer with rationale.

## Acceptance

- No servlet `Filter` remains for SPA routing in **`mill-ui-service`** on the reactive stack.
- Chat and descriptor endpoints functionally match prior behavior per tests.

## Depends on

**WI-225** (or parallelize descriptor/UI if independent—note in commit if order differs).
