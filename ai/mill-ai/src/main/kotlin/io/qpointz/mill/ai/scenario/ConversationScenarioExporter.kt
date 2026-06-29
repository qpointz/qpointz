package io.qpointz.mill.ai.scenario

import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.RunEventRecord
import io.qpointz.mill.ai.persistence.RunEventStore
import java.time.Instant

/**
 * Builds draft [ScenarioPack] documents from persisted chat data (`ai_*` tables).
 *
 * Export quality is best-effort: operator adds `verify:` manually after download.
 */
class ConversationScenarioExporter(
    private val chatRegistry: ChatRegistry,
    private val conversationStore: ConversationStore,
    private val runEventStore: RunEventStore,
    private val artifactStore: ArtifactStore,
) {

    /**
     * Exports a chat to a draft scenario pack.
     *
     * @param chatId Conversation id.
     * @return Export result, or `null` when the chat is missing.
     * @throws IllegalStateException when the chat has no exportable user turns.
     */
    fun export(chatId: String, exportedAt: Instant = Instant.now()): ScenarioPackExport? {
        val metadata = chatRegistry.load(chatId) ?: return null
        val conversation = conversationStore.load(chatId)
            ?: throw IllegalStateException("Chat $chatId has no conversation record")
        val userTurns = conversation.turns.filter { it.role == "user" }
        if (userTurns.isEmpty()) {
            throw IllegalStateException("Chat $chatId has no user turns to export")
        }

        val allEvents = runEventStore.findByChatIdOrderByCreatedAtAsc(chatId)
        val eventsByRun = allEvents.groupBy { it.runId }
        val orderedRunIds = allEvents
            .filter { it.kind == "run.started" }
            .sortedBy { it.createdAt }
            .map { it.runId }
            .distinct()

        val artifacts = artifactStore.findByConversation(chatId)
        val verifyHints = mutableListOf<String>()
        val runItems = userTurns.mapIndexed { index, userTurn ->
            val runId = orderedRunIds.getOrNull(index)
            val runEvents = runId?.let { eventsByRun[it].orEmpty().sortedBy { e -> e.createdAt } }.orEmpty()
            val turnArtifacts = when {
                runId != null -> artifacts.filter { it.runId == runId }
                userTurns.size == 1 -> artifacts
                else -> emptyList()
            }
            verifyHints += verifyHintsForArtifacts(turnArtifacts)

            var script = buildScriptFromRunEvents(runEvents)
            if (script.isEmpty()) {
                script = buildScriptFromArtifacts(turnArtifacts)
            }

            AskRunItem(
                ask = userTurn.text ?: "",
                script = script.takeIf { it.isNotEmpty() },
            )
        }

        val hasScript = runItems.any { !it.script.isNullOrEmpty() }
        val packName = metadata.chatName
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { chatId }

        return ScenarioPackExport(
            chatId = chatId,
            exportedAt = exportedAt,
            pack = ScenarioPack(
                name = packName,
                profileId = metadata.profileId,
                parameters = ScenarioParameters(mode = if (hasScript) "scripted" else "live"),
                run = runItems,
            ),
            verifyHints = verifyHints.distinct(),
        )
    }

    private fun buildScriptFromRunEvents(events: List<RunEventRecord>): List<ScriptStep> {
        if (events.isEmpty()) return emptyList()

        val steps = mutableListOf<ScriptStep>()
        val toolCallEvents = events.filter { it.kind == "tool.call" }
        if (toolCallEvents.isNotEmpty()) {
            toolCallEvents
                .groupBy { (it.content["iteration"] as? Number)?.toInt() ?: 0 }
                .toSortedMap()
                .values
                .forEach { iterationCalls ->
                    steps += ScriptStep(
                        toolCalls = iterationCalls.map { event ->
                            ScriptToolCall(
                                name = event.content["name"] as? String ?: "unknown",
                                args = @Suppress("UNCHECKED_CAST")
                                (event.content["arguments"] as? Map<String, Any?>) ?: emptyMap(),
                            )
                        },
                    )
                }
        }

        val protocolFinal = events.lastOrNull { it.kind == "protocol.final" }
        val answerCompleted = events.lastOrNull { it.kind == "answer.completed" }
        when {
            protocolFinal != null -> {
                val payload = protocolFinal.content["payload"]
                val answer = when (payload) {
                    null -> answerCompleted?.content?.get("text") as? String
                    is String -> payload
                    else -> ScenarioPackYamlWriter.jsonMapper.writeValueAsString(payload)
                }
                if (!answer.isNullOrEmpty()) {
                    steps += ScriptStep(answer = answer)
                }
            }
            answerCompleted != null -> {
                val text = answerCompleted.content["text"] as? String
                if (!text.isNullOrEmpty()) {
                    steps += ScriptStep(answer = text)
                }
            }
        }

        return steps
    }

    private fun buildScriptFromArtifacts(artifacts: List<ArtifactRecord>): List<ScriptStep> {
        val sqlArtifact = artifacts.firstOrNull { it.kind == "sql.generated" || it.kind.contains("sql") }
        if (sqlArtifact != null) {
            val sql = extractSql(sqlArtifact.payload)
            if (sql != null) {
                return listOf(
                    ScriptStep(
                        toolCalls = listOf(
                            ScriptToolCall(
                                name = "validate_sql",
                                args = mapOf(
                                    "sql" to sql,
                                    "attempt" to 1,
                                ),
                            ),
                        ),
                    ),
                )
            }
        }

        val facetArtifact = artifacts.firstOrNull {
            it.kind == "metadata.faceting.capture" || it.kind.contains("faceting")
        }
        if (facetArtifact != null) {
            return listOf(
                ScriptStep(
                    toolCalls = listOf(
                        ScriptToolCall(
                            name = "propose_facet_assignment",
                            args = facetToolArgs(facetArtifact.payload),
                        ),
                    ),
                ),
            )
        }

        return emptyList()
    }

    private fun extractSql(payload: Map<String, Any?>): String? =
        payload["sql"] as? String
            ?: payload["statement"] as? String
            ?: (payload["payload"] as? Map<*, *>)?.get("sql") as? String

    @Suppress("UNCHECKED_CAST")
    private fun facetToolArgs(payload: Map<String, Any?>): Map<String, Any?> {
        val facetTypeKey = payload["facetTypeKey"] as? String ?: "descriptive"
        val metadataEntityId = payload["metadataEntityId"] as? String
            ?: payload["entityId"] as? String
            ?: "unknown"
        val serialized = payload["serializedPayload"] as? Map<String, Any?>
            ?: payload["payload"] as? Map<String, Any?>
            ?: emptyMap<String, Any?>()
        return mapOf(
            "facetTypeKey" to facetTypeKey,
            "metadataEntityId" to metadataEntityId,
            "payload" to serialized,
            "rationale" to (payload["rationale"] as? String ?: "Exported from live chat"),
        )
    }

    private fun verifyHintsForArtifacts(artifacts: List<ArtifactRecord>): List<String> {
        if (artifacts.isEmpty()) return emptyList()
        return artifacts
            .groupBy { it.kind }
            .map { (kind, group) -> "artifacts: persistKind=$kind count=${group.size}" }
    }
}
