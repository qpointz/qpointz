package io.qpointz.mill.ai.autoconfigure.chat

import dev.langchain4j.model.chat.StreamingChatModel
import io.qpointz.mill.ai.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.chat.ChatDiagnosticCodes
import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.memory.LlmMemoryStrategy
import io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.AgentPersistenceContext
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.RunEventStore
import io.qpointz.mill.ai.dependencies.CapabilityDependencyAssembler
import io.qpointz.mill.ai.profile.ProfileRegistry
import io.qpointz.mill.ai.profile.rehydrate
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.runtime.langchain4j.LangChain4jAgent
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

/**
 * [AiV3ChatRuntime] implementation backed by [LangChain4jAgent].
 *
 * For each [send] call:
 * 1. Resolves the [AgentProfile][io.qpointz.mill.ai.profile.AgentProfile] from [profileRegistry]
 *    using [ChatMetadata.profileId].
 * 2. Builds the [io.qpointz.mill.ai.runtime.AgentContext] from the rehydrated coarse fields and
 *    [CapabilityDependencyAssembler] output (replaces [io.qpointz.mill.ai.runtime.AgentContext.capabilityDependencies]).
 * 3. Creates a fresh [AgentPersistenceContext] per turn (each turn gets its own publisher/telemetry).
 * 4. Runs [LangChain4jAgent.run] on [Schedulers.boundedElastic] (blocking I/O off the event loop).
 * 5. Bridges the callback-based [AgentEvent] listener to a reactive [Flux] of [ChatRuntimeEvent]
 *    (answer text, optional [ChatRuntimeEvent.Diagnostic] for UX, optional tool events).
 *
 * Replace this bean with a custom [AiV3ChatRuntime] to change model provider or add
 * streaming behaviour.
 */
class LangChain4jChatRuntime(
    private val model: StreamingChatModel,
    private val profileRegistry: ProfileRegistry,
    private val capabilityDependencyAssembler: CapabilityDependencyAssembler,
    private val chatMemoryStore: ChatMemoryStore,
    private val memoryStrategy: LlmMemoryStrategy,
    private val runEventStore: RunEventStore,
    private val conversationStore: ConversationStore,
    private val artifactStore: ArtifactStore,
    private val activeArtifactPointerStore: ActiveArtifactPointerStore,
) : AiV3ChatRuntime {

    override fun send(metadata: ChatMetadata, message: String): Flux<ChatRuntimeEvent> {
        val rehydration = profileRegistry.rehydrate(metadata)
            ?: return Flux.error(
                IllegalStateException("Unknown profile '${metadata.profileId}' for chat ${metadata.chatId}")
            )

        return Flux.create<ChatRuntimeEvent> { sink ->
            try {
                // Fresh persistence context per turn: each run gets its own publisher and telemetry.
                val persistenceContext = AgentPersistenceContext(
                    runEventStore = runEventStore,
                    conversationStore = conversationStore,
                    artifactStore = artifactStore,
                    activeArtifactPointerStore = activeArtifactPointerStore,
                )
                val agent = LangChain4jAgent(
                    model = model,
                    profile = rehydration.profile,
                    chatMemoryStore = chatMemoryStore,
                    memoryStrategy = memoryStrategy,
                    persistenceContext = persistenceContext,
                )
                val session = ConversationSession(
                    conversationId = metadata.chatId,
                    profileId = rehydration.profile.id,
                )
                val capabilityDependencies = capabilityDependencyAssembler.assemble(rehydration.profile, metadata)
                val agentContext = AgentContext(
                    contextType = rehydration.agentContext.contextType,
                    focusEntityType = rehydration.agentContext.focusEntityType,
                    focusEntityId = rehydration.agentContext.focusEntityId,
                    capabilityDependencies = capabilityDependencies,
                )
                var lastText = ""
                agent.run(
                    input = message,
                    session = session,
                    context = agentContext,
                ) { event ->
                    when (event) {
                        is AgentEvent.RunStarted -> sink.next(
                            ChatRuntimeEvent.Diagnostic(
                                code = ChatDiagnosticCodes.RUN_STARTED,
                                message = "Run started",
                                detail = mapOf("profileId" to event.profileId),
                            ),
                        )
                        is AgentEvent.ThinkingDelta -> sink.next(
                            ChatRuntimeEvent.Diagnostic(
                                code = ChatDiagnosticCodes.THINKING_DELTA,
                                message = event.message,
                                detail = null,
                            ),
                        )
                        is AgentEvent.PlanCreated -> sink.next(
                            ChatRuntimeEvent.Diagnostic(
                                code = ChatDiagnosticCodes.PLAN_CREATED,
                                message = buildString {
                                    append("Plan: ")
                                    append(event.mode)
                                    event.toolName?.let { append(" (").append(it).append(')') }
                                },
                                detail = mapOf(
                                    "mode" to event.mode,
                                    "toolName" to event.toolName,
                                ),
                            ),
                        )
                        is AgentEvent.ReasoningDelta -> sink.next(
                            ChatRuntimeEvent.Diagnostic(
                                code = ChatDiagnosticCodes.REASONING_DELTA,
                                message = event.text,
                                detail = null,
                            ),
                        )
                        is AgentEvent.MessageDelta -> sink.next(ChatRuntimeEvent.Chunk(event.text))
                        is AgentEvent.ToolCall -> sink.next(
                            ChatRuntimeEvent.ToolCall(event.name, event.arguments, event.iteration),
                        )
                        is AgentEvent.ToolResult -> sink.next(
                            ChatRuntimeEvent.ToolResult(event.name, event.result),
                        )
                        is AgentEvent.AnswerCompleted -> lastText = event.text
                        else -> Unit
                    }
                }
                sink.next(ChatRuntimeEvent.Completed(lastText))
                sink.complete()
            } catch (e: Throwable) {
                sink.error(e)
            }
        }.subscribeOn(Schedulers.boundedElastic())
    }
}
