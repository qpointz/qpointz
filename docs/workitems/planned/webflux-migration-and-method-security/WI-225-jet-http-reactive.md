# WI-225 — Jet HTTP reactive dispatcher and Access service

Status: `planned`  
Type: `refactoring`  
Area: `data`, `platform`  
Backlog refs: **P-34**, **P-1**, **P-3**

## Goal

Introduce **`ReactiveDataOperationDispatcher`** (and **`ReactiveMessageHelper`** for protobuf/JSON) and migrate **`AccessServiceController`** (`/services/jet`) to reactive **`Mono`/`Flux`** signatures.

## Scope

1. **`core/mill-service-core`**: reactive dispatcher interface + wiring strategy (implementation may delegate with bounded-elastic wrapping where blocking is unavoidable initially—document trade-off in completion notes).
2. **`services/mill-jet-http-service`**: reactive controller methods; shared helper for encode/decode parity with existing `MessageHelper`-style flow.

## Acceptance

- Jet HTTP integration tests (existing or new) pass under WebFlux; document any temporary **boundedElastic** bridging.

## Depends on

**WI-224** (ordering may be relaxed if dispatcher has no metadata dependency—adjust in implementation notes if parallelized).
