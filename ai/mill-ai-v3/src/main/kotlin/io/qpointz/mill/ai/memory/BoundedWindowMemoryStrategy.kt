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
 * Simple bounded-window projection strategy.
 *
 * Returns the most recent [maxMessages] messages from the stored memory,
 * preserving message order. When the store is empty or null, returns an empty list.
 *
 * Summarization strategies are out of scope for WI-073 and may be added as
 * separate [LlmMemoryStrategy] implementations later.
 */
class BoundedWindowMemoryStrategy(
    val maxMessages: Int = 40,
) : LlmMemoryStrategy {
    override fun project(input: MemoryProjectionInput): List<ConversationMessage> {
        val messages = input.memory?.messages ?: return emptyList()
        return if (messages.size <= maxMessages) messages else messages.takeLast(maxMessages)
    }
}





