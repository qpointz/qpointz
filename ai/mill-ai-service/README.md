# mill-ai-service

HTTP/SSE facade for AI v3 (`AiChatController`, `AiProfileController`, OpenAPI). Orchestration lives in `UnifiedChatService` and `ChatService`.

**Schema-capable profiles:** ensure the host application includes the data/SQL beans described in [`../mill-ai-autoconfigure/README.md`](../mill-ai-autoconfigure/README.md) so `CapabilityDependencyAssembler` can populate `AgentContext` for turns.

**Developer CLI:** [`../mill-ai-cli/README.md`](../mill-ai-cli/README.md) exercises this API over HTTP only.
