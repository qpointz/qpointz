# WI-227 — @PreAuthorize stubs on all REST operations

Status: `planned`  
Type: `refactoring`  
Area: `security`, `platform`  
Backlog refs: **P-34**

## Goal

Add **method-level authorization** to **every production REST handler** listed in [**WI-220** inventory](../../../design/security/REST-CONTROLLERS-INVENTORY.md), using **stub** `@PreAuthorize` expressions that are **valid under reactive method security** (**WI-223**).

Rationale: path-level security alone is insufficient for long-term policy; stubs make **per-operation** authority refinement a **delta** change rather than a repo-wide sweep.

## Scope

1. Choose and document a **stub convention** (pick one primary pattern and use consistently unless a handler must differ):
   - e.g. `@PreAuthorize("isAuthenticated()")` for authenticated API surfaces, or
   - `@PreAuthorize("permitAll()")` only where **globally public** by design (e.g. selected `/.well-known` or health), **plus** a short comment pointing to the security design follow-up.
   - Prefer **structured placeholders** (e.g. `hasAuthority('mill:metadata:read')`) **only if** matching authorities already exist; otherwise use **`isAuthenticated()`** and file follow-up in completion notes.
2. Apply annotations to **controller methods** (and class level where appropriate) for **all** inventory rows in scope for this story’s applications.
3. Add a short subsection to [`docs/design/security/README.md`](../../../design/security/README.md) or a dedicated **`method-security-conventions.md`** in the same folder explaining the stub convention and how it composes with **`mill.security.enable`**.

## Acceptance

- Grep/architecture check: **no** inventory operation lacks an explicit class- or method-level `@PreAuthorize` (**unless** already covered by a class-level annotation that unambiguously applies).
- With security **enabled**, anonymous calls to a representative **secured** operation still fail; with security **disabled**, behavior matches existing **`@ConditionalOnSecurity`** story (no spurious `AccessDenied` where `permitAll` was previously implicit).

## Depends on

**WI-226**, **WI-223**
