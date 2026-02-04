package io.qpointz.mill.ai.capabilities

import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.Prompts
import org.springframework.ai.chat.messages.SystemMessage

object Talk : Capability {

    override val name: String = "mill-talk"

    override val description: String = """Respond warmly and naturally to casual messages—matching the user’s 
                tone and language—acknowledge greetings or thanks appropriately, answer user 
                questions within your knowledge when they arise, keep it brief and friendly, 
                and gently offer help with data queries when it fits."""

    override val rules: List<SystemMessage> = listOf(
        Prompts.systemResource("capabilities/talk/talk-system.md")
    )


}