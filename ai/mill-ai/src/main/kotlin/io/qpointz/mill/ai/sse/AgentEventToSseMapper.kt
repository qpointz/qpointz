package io.qpointz.mill.ai.sse

import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.core.artifact.FacetProposalWire
import io.qpointz.mill.ai.core.artifact.ProtocolFinalBatch
import io.qpointz.mill.ai.runtime.events.AgentEvent
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Stateful mapper from internal [AgentEvent] stream to public [ChatSseEvent] stream.
 *
 * One mapper instance covers one chat turn. Create a new instance per [sendMessage] call.
 *
 * Mapping rules (V1 text path):
 * - First [AgentEvent.MessageDelta] in a turn → emit [ChatSseEvent.ItemCreated] then [ChatSseEvent.ItemPartUpdated]
 * - Subsequent [AgentEvent.MessageDelta] → emit [ChatSseEvent.ItemPartUpdated]
 * - [AgentEvent.ProtocolFinal] with a registry [wirePartType][io.qpointz.mill.ai.core.artifact.ArtifactDescriptor.wirePartType]
 *   → emit structured [ChatSseEvent.ItemPartUpdated]
 * - [AgentEvent.AnswerCompleted] → emit [ChatSseEvent.ItemCompleted]; reset for next item
 * - All other [AgentEvent] subtypes → ignored at this layer (runtime-internal)
 */
class AgentEventToSseMapper(
    private val chatId: String,
    private val artifactRegistry: ArtifactDescriptorRegistry = ArtifactDescriptorRegistry.loadDefault(),
    private val jsonMapper: JsonMapper = JsonMapper.builder().addModule(kotlinModule()).build(),
) {

    private val sequence = AtomicInteger(0)
    private var itemId: String = UUID.randomUUID().toString()
    private var itemStarted = false
    private var hadTextDeltas = false
    private var structuredCompletionPresentation: String? = null
    private var structuredCompletionPartType: String? = null

    private var structuredPartCount = 0
    private val structuredPartTypes = mutableListOf<String>()

    fun map(event: AgentEvent): List<ChatSseEvent> = when (event) {
        is AgentEvent.MessageDelta -> {
            val result = mutableListOf<ChatSseEvent>()
            if (!itemStarted) {
                itemStarted = true
                result += itemCreated()
            }
            hadTextDeltas = true
            result += itemPartUpdated(event.text)
            result
        }

        is AgentEvent.ProtocolFinal -> mapProtocolFinal(event)

        is AgentEvent.AnswerCompleted -> {
            val result = mutableListOf<ChatSseEvent>()
            if (!itemStarted) {
                itemStarted = true
                result += itemCreated()
            }
            result += itemCompleted(
                content = when {
                    hadTextDeltas -> null
                    else -> event.text.takeIf { it.isNotEmpty() }
                },
                presentation = structuredCompletionPresentation ?: "conversation",
                partType = completionPartType(structuredCompletionPartType),
                structuredPartCount = structuredPartCount.takeIf { it > 1 },
                partTypes = structuredPartTypes.takeIf { structuredPartCount > 1 }?.toList(),
            )
            itemId = UUID.randomUUID().toString()
            itemStarted = false
            hadTextDeltas = false
            structuredCompletionPresentation = null
            structuredCompletionPartType = null
            structuredPartCount = 0
            structuredPartTypes.clear()
            result
        }

        else -> emptyList()
    }

    private fun mapProtocolFinal(event: AgentEvent.ProtocolFinal): List<ChatSseEvent> {
        val descriptor = artifactRegistry.descriptorForProtocol(event.protocolId) ?: return emptyList()
        val wirePartType = descriptor.wirePartType ?: descriptor.artifactKind
        val presentation = descriptor.presentation ?: "structured"
        val itemPayloads = ProtocolFinalBatch.expandItemPayloads(event.payload)
        if (itemPayloads.isEmpty()) return emptyList()

        val result = mutableListOf<ChatSseEvent>()
        if (!itemStarted) {
            itemStarted = true
            result += itemCreated()
        }

        // Generic fan-out: any multi-item protocol final → N structured parts (replace + append).
        itemPayloads.forEachIndexed { index, itemPayload ->
            val wirePayload = FacetProposalWire.normalizeForWire(wirePartType, itemPayload)
                ?: return@forEachIndexed
            val json = when (val payload = wirePayload) {
                is String -> payload
                else -> jsonMapper.writeValueAsString(payload)
            }
            structuredCompletionPresentation = presentation
            structuredCompletionPartType = wirePartType
            structuredPartCount++
            structuredPartTypes += wirePartType
            result += itemPartUpdated(
                content = json,
                presentation = presentation,
                partType = wirePartType,
                mode = if (index == 0) "replace" else "append",
            )
        }
        return result
    }

    private fun completionPartType(lastStructuredPartType: String?): String =
        if (structuredPartCount > 1) "multi" else lastStructuredPartType ?: "text"

    fun fail(code: String, reason: String): ChatSseEvent.ItemFailed = ChatSseEvent.ItemFailed(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
        code = code,
        reason = reason,
    )

    private fun itemCreated() = ChatSseEvent.ItemCreated(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
    )

    private fun itemPartUpdated(
        content: String,
        presentation: String = "conversation",
        partType: String = "text",
        mode: String = "append",
    ) = ChatSseEvent.ItemPartUpdated(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
        presentation = presentation,
        partType = partType,
        mode = mode,
        content = content,
    )

    private fun itemCompleted(
        content: String?,
        presentation: String = "conversation",
        partType: String = "text",
        structuredPartCount: Int? = null,
        partTypes: List<String>? = null,
    ) = ChatSseEvent.ItemCompleted(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
        presentation = presentation,
        partType = partType,
        content = content,
        structuredPartCount = structuredPartCount,
        partTypes = partTypes,
    )
}
