package io.qpointz.mill.ai

import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.core.io.ClassPathResource

object Prompts {

    fun userContent(content: String): UserMessage {
        return UserMessage(content)
    }

    private fun readResourceAsString(path: String): String =
        ClassPathResource(path).inputStream
            .bufferedReader()
            .use { it.readText() }

    fun userResource(resource: String): UserMessage {
        return UserMessage(readResourceAsString(resource))
    }

    fun systemContent(content: String): SystemMessage {
        return SystemMessage(content)
    }

    fun systemResource(resource: String): SystemMessage {
        return SystemMessage(readResourceAsString(resource))
    }

}