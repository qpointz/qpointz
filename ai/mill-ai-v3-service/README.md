# mill-ai-v3-service

HTTP/SSE facade for AI v3 (`AiChatController`, `AiProfileController`, OpenAPI). Orchestration lives in `UnifiedChatService` and `ChatService`.

**Schema-capable profiles:** ensure the host application includes the data/SQL beans described in [`../mill-ai-v3-autoconfigure/README.md`](../mill-ai-v3-autoconfigure/README.md) so `CapabilityDependencyAssembler` can populate `AgentContext` for turns.

**Developer CLI:** [`../mill-ai-v3-cli/README.md`](../mill-ai-v3-cli/README.md) exercises this API over HTTP only.
