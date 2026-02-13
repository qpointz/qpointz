package io.qpointz.mill.ai.capabilities

import com.fasterxml.jackson.databind.JsonNode
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.Prompts
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.tool.ToolCallback

object Talk : Capability {

    override val name: String = "mill-talk"

    override val description: String
        get() = Prompts.systemResource("ai/capabilities/talk/talk-description.md").text

    override val system: String?
        get() = Prompts.systemResource("ai/capabilities/talk/talk-system.md").text

    override val tools: List<ToolCallback>
        get() = listOf()

    override val advisors: List<Advisor>
        get() = listOf()
    override val protocol: JsonNode?
        get() = TODO("Not yet implemented")

}