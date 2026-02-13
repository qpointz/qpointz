package io.qpointz.mill.ai

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.tool.ToolCallback


interface Capability {
    val name: String
    val description: String
    val system: String?
    val tools: List<ToolCallback>
    val advisors: List<Advisor>
    val protocol: JsonNode?
}
