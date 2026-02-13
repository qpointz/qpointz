package io.qpointz.mill.ai

import io.qpointz.mill.ai.tools.ProtocolSchema
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.tool.ToolCallback
import org.springframework.ai.tool.ToolCallbackProvider

class ChatConversation(
    val capabilities: List<Capability>,
    val chatOptions: ChatOptions
    ) {

    fun chatClient(chatModel: ChatModel): ChatClient {
        val cc = ChatClient
            .builder(chatModel)
            .defaultOptions(this.chatOptions)
            .defaultAdvisors(this.capabilitiesAdvisors())
            .defaultToolCallbacks(this.capabilitiesTools())
            .defaultSystem (this.capabilitiesSystem())
        return cc.build()
    }

    fun capabilitiesSystem(): String {
        val sb = StringBuilder()
        sb.appendLine(Prompts.systemResource("ai/capabilities/system.md").text)
        fun capabilitiesSection(c: Capability) {
            sb.appendLine("## Capability `${c.name}`\n\nDescription:\n${c.description}\n\n${c.system?:""}")
        }
        capabilities
            .forEach { capabilitiesSection(it) }
        return sb.toString()
    }

    private fun defaultTools(): ToolCallbackProvider {
        val jsonSchemas = capabilities.associate { it.name to it.protocol }
        return ToolCallbackProvider.from(listOf(
            ProtocolSchema.callback(jsonSchemas)
        ))
    }

    private fun capabilitiesTools(): ToolCallbackProvider {
        val allTools = mutableListOf<ToolCallback>(*defaultTools().toolCallbacks)
        capabilities
            .forEach { allTools.addAll(it.tools) }
        return ToolCallbackProvider
            .from(allTools)
    }

    private fun capabilitiesAdvisors():List<Advisor> {
        return capabilities
            .flatMap { k -> k.advisors }
            .toList()
    }
}