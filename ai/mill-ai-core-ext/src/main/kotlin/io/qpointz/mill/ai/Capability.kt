package io.qpointz.mill.ai

import org.springframework.ai.chat.messages.SystemMessage


data class ConversationSpec (
    val capabilities: List<Capability>
)

interface Capability {
    val name: String
    val description: String
    val rules: List<SystemMessage>?
}
