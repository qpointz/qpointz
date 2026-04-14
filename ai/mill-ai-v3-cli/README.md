# mill-ai-v3-cli

**HTTP-only** developer test bench: it calls `mill-ai-v3-service` over REST and SSE. There is **no** in-process LangChain4j agent and **no** embedded schema catalog — run the service locally (default **`http://localhost:8080`**) with the same profile and data beans you use in production.

## Requirements

- A running `mill-ai-v3-service` (or full Mill app exposing the same routes).
- For LLM turns: service configured with `OPENAI_API_KEY` / `mill.ai.model.*` as usual.

## Usage

```bash
# Default: hello-world profile, chat against localhost:8080
./gradlew :ai:mill-ai-v3-cli:run

# Schema-capable profile (server must wire schema/SQL beans — see mill-ai-v3-autoconfigure README)
./gradlew :ai:mill-ai-v3-cli:run --args="--profile-id schema-authoring"

# List profiles (WI-168)
./gradlew :ai:mill-ai-v3-cli:run --args="--list-profiles"

# Another host/port
./gradlew :ai:mill-ai-v3-cli:run --args="--base-url http://127.0.0.1:9090 --profile-id hello-world"
```

Environment:

- `MILL_AI_PROFILE` — default profile id if `--profile-id` is omitted.

## Auth hooks

Outbound requests can be decorated via [`HttpRequestCustomizer`](src/main/kotlin/io/qpointz/mill/ai/cli/HttpRequestCustomizer.kt). The default is a **no-op**. Optional `--user-id` / `--password` are reserved for a future non-no-op implementation aligned with the service `UserIdResolver`.

## OpenAPI

The CLI does not embed an OpenAPI document. Use the service’s springdoc UI (e.g. `/swagger-ui.html`) if available.

## See also

- `docs/design/agentic/v3-mill-ai-v3-cli-http-client.md`
- WI-169 (story work item)
