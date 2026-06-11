package io.qpointz.mill.ai.test.runner

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.agent.tool.ToolExecutionRequest
import io.qpointz.mill.ai.test.scenario.v3.ScriptStep
import io.qpointz.mill.ai.test.scenario.v3.ScriptToolCall
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test double [StreamingChatModel] that replays [ScriptStep] entries from a [ScriptQueue].
 */
class ScriptedStreamingChatModel(
    private val queue: ScriptQueue,
    private val exhaustionContext: ScriptExhaustionContext,
    private val objectMapper: JsonMapper = JsonMapper.builder().addModule(kotlinModule()).build(),
    private val onStepConsumed: (ScriptStep) -> Unit = {},
) : StreamingChatModel {

    private val callCounter = AtomicInteger(0)

    override fun chat(chatRequest: ChatRequest, handler: StreamingChatResponseHandler) {
        val step = queue.nextOrThrow(exhaustionContext)
        onStepConsumed(step)
        step.expectTools?.let { expected ->
            val actual = step.toolCalls?.map { it.name } ?: emptyList()
            require(actual == expected) {
                "script expectTools=$expected but toolCalls names=$actual"
            }
        }
        val aiMessage = step.toAiMessage(objectMapper, callCounter.getAndIncrement())
        val response = ChatResponse.builder().aiMessage(aiMessage).build()
        handler.onCompleteResponse(response)
    }

    private fun ScriptStep.toAiMessage(mapper: JsonMapper, callBase: Int): AiMessage {
        toolCalls?.let { calls ->
            val requests = calls.mapIndexed { index, call -> call.toRequest(mapper, callBase + index) }
            return AiMessage.from(requests)
        }
        return AiMessage.from(answer ?: "")
    }

    private fun ScriptToolCall.toRequest(mapper: JsonMapper, index: Int): ToolExecutionRequest =
        ToolExecutionRequest.builder()
            .id(id ?: "call-$index")
            .name(name)
            .arguments(mapper.writeValueAsString(args))
            .build()
}
