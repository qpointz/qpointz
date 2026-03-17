package io.qpointz.mill.ai

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
