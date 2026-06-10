package io.qpointz.mill.ai.runtime

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

enum class MessageRole { SYSTEM, USER, ASSISTANT, TOOL_RESULT }

data class ConversationMessage(
    val role: MessageRole,
    val content: String,
    val toolCallId: String? = null,
    val toolName: String? = null,
)

data class ConversationSession(
    val conversationId: String = java.util.UUID.randomUUID().toString(),
    val profileId: String = "",
    val messages: MutableList<ConversationMessage> = mutableListOf(),
) {
    fun appendUserMessage(content: String) {
        messages.add(ConversationMessage(role = MessageRole.USER, content = content))
    }
    fun appendAssistantMessage(content: String) {
        messages.add(ConversationMessage(role = MessageRole.ASSISTANT, content = content))
    }
    fun appendToolResult(toolCallId: String, toolName: String, content: String) {
        messages.add(ConversationMessage(role = MessageRole.TOOL_RESULT, content = content, toolCallId = toolCallId, toolName = toolName))
    }
    fun clear() { messages.clear() }
}





