package io.qpointz.mill.ai.memory

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

/**
 * Lane 3 — model-facing chat memory contracts for ai/v3.
 *
 * Ownership rules:
 * - These interfaces and models belong to `ai/mill-ai-v3` (the functional module).
 * - Durable adapters implementing [ChatMemoryStore] belong in `mill-persistence`.
 * - Spring bean wiring belongs in `mill-persistence-autoconfigure`.
 * - No Spring or JPA types appear here.
 */

data class ConversationMemory(
    val conversationId: String,
    val profileId: String,
    val messages: List<ConversationMessage>,
)

data class MemoryProjectionInput(
    val conversationId: String,
    val profileId: String,
    val memory: ConversationMemory?,
    val latestUserInput: String? = null,
)

interface ChatMemoryStore {
    fun load(conversationId: String): ConversationMemory?
    fun save(memory: ConversationMemory)
    fun clear(conversationId: String)
}

fun interface LlmMemoryStrategy {
    fun project(input: MemoryProjectionInput): List<ConversationMessage>
}





