# WI-073a - Central Persistence Module Bootstrap

Status: `planned`  
Type: `✨ feature`  
Area: `platform`  
Backlog refs: `A-64a`

## Problem Statement

Several areas in Mill need persistence but do not yet have a common adapter/module foundation:

- `ai/v3` chat memory, events, conversation record, artifacts
- metadata persistence work
- future user-profile persistence
- other durable runtime/application records

At the same time, functional modules such as `ai/v3` should remain free of Spring/JPA
contamination.

## Goal

Create a central `persistence/` project group and bootstrap the shared persistence modules:

- `persistence/mill-persistence`
- `persistence/mill-persistence-autoconfigure`

This is an enabling/platform WI for the later lane-specific persistence items.

## Scope

In scope:

- create `persistence/` project group
- create `mill-persistence` module
- create `mill-persistence-autoconfigure` module
- establish dependency direction:
  - functional modules define repository/store interfaces
  - persistence modules implement them
- baseline Flyway setup
- baseline Spring Boot autoconfiguration
- package structure conventions
- persistence-layer testing conventions
- one minimal proof-of-shape adapter implementation

Out of scope:

- full Lane 3 chat memory persistence
- full Lanes 1,2 event/conversation/artifact persistence
- full Lane 4 relation indexing

## Design Requirements

- Functional modules own their persistence/store interfaces.
- `mill-persistence` implements those interfaces without leaking JPA types outward.
- `mill-persistence-autoconfigure` contains Spring Boot bean wiring only.
- Flyway migrations are centrally managed.
- Package structure inside persistence modules remains domain-oriented.
- No JPA annotations or Spring repository abstractions leak into functional modules.

## Acceptance Criteria

- `persistence/` project group exists.
- `mill-persistence` and `mill-persistence-autoconfigure` modules exist.
- Baseline Flyway and autoconfiguration wiring exists.
- Dependency direction is documented and reflected in module structure.
- At least one minimal adapter proves the ports-and-adapters shape.
- Persistence-layer testing conventions are documented and ready for later lane work.
