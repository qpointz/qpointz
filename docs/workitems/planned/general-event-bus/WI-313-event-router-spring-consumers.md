# WI-313 — Event router Spring wiring + integration proof

| Field | Value |
|--------|--------|
| **Story** | [`general-event-bus`](STORY.md) |
| **Status** | `planned` |
| **Type** | `feature` |
| **Area** | `platform`, `core` |
| **Depends on** | [WI-312](WI-312-event-transport-plane.md) |

## Tracker

- [ ] `MillEventsAutoConfiguration` complete:
  - `@Bean eventRouter(consumers: List<EventConsumer>): EventRouter`
  - `@Bean eventPublisher(...)` delegating to selected transport
  - `@ConditionalOnMissingBean` overrides for tests
- [ ] `spring.factories` / `AutoConfiguration.imports` registration
- [ ] `testIT` suite per repo conventions (`JvmTestSuite`)
- [ ] Two **test stub** `@Bean EventConsumer` beans (`eventConsumer { on(…) { … } }`) in `testIT` only
- [ ] `testIT`: publish via `EventPublisher` → consumers invoked by matching `EventType.id`
- [ ] `testIT`: failing stub does not block other stub on same event type
- [ ] KDoc on autoconfigure classes

## Goal

Prove **dynamic Spring consumer registration**: the application context collects all `EventConsumer`
beans and wires them into `EventRouter` at startup. No static consumer registry in production.

## Spring wiring (locked)

```kotlin
@Bean
fun eventRouter(consumers: List<EventConsumer>): EventRouter =
    EventRouter(consumers.flatMap { it.subscriptions() })

@Bean
fun eventPublisher(transport: EventTransport): EventPublisher =
    DefaultEventPublisher(transport)
```

Example test stub:

```kotlin
@Bean
fun testStubConsumer() = eventConsumer {
    on(EventTypes.TEST_EVENT) { event -> … }
}
```

**Rules:**

- Any module may add consumers via `@Bean EventConsumer` in future stories; router code unchanged.
- `EventRouter` must not implement `EventConsumer`.
- **No production consumer beans** in this WI — stubs live under `src/testIT` only.

## Explicitly not delivered

- Beans in `mill-metadata-*`, `mill-ai-*`, or `mill-service` production configuration
- Domain event producers
- `apps/mill-service` classpath dependency unless required for autoconfigure `testIT` (prefer isolated slice)

## Acceptance

```bash
./gradlew :core:mill-events-autoconfigure:test
./gradlew :core:mill-events-autoconfigure:testIT
```

## Non-goals

- Design doc promotion (WI-314)
- Domain producers or real side workers
