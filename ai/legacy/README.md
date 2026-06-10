# AI v1 (legacy, reference only)

Spring AI–based NL2SQL stack retained for **historical reference** while Mill AI is consolidated
on the LangChain4j agentic runtime under `ai/mill-ai*`.

| Module | Role |
|--------|------|
| `mill-ai-v1-core` | NL2SQL core (Spring AI, value mapping, reasoners) |
| `mill-ai-v1-nlsql-chat-service` | REST chat service (`/api/nl2sql/**`, `@ConditionalOnService("ai-nl2data")`) |

These modules are **not** part of supported Mill editions (`apps/mill-service` uses `mill-ai-autoconfigure`
only). Integration `testIT` suites are **disabled** so `:ai:testIT` does not run v1 scenario tests.

CI history and reimplementation plan: [`docs/design/ai/ai-v1-integration/README.md`](../../docs/design/ai/ai-v1-integration/README.md).
