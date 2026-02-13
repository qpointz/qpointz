package io.qpointz.mill.ai.test.scenario

import io.qpointz.mill.ai.ChatConversation
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel

abstract class ConversationScenarioBaseTest {

    abstract val chatModel: ChatModel

    abstract val scenario : List<ConversationScenario>

    abstract val conversation: ChatConversation

    private val chatClients = mutableMapOf<ConversationScenario, ChatClient>()

    companion object {
        val log = LoggerFactory.getLogger(ConversationScenarioBaseTest::class.java)
    }

    @TestFactory
    fun scenarioTests(): List<DynamicContainer?> {
        // imagine this comes from YAML
        return scenario
            .withIndex()
            .map { c ->
                val steps = c.value.conversation
                    .withIndex()
                    .map { s -> DynamicTest.dynamicTest(
                        "${s.index+1}. ${s.value.user}",
                        { executeAsk(c.value, c.index,  s.value, s.index) })}
                    .toList()
                DynamicContainer.dynamicContainer(c.value.name, steps)
            }
            .toList()
    }

    private fun executeAsk(scenario: ConversationScenario, scenarioIdx: Int,
                           step: ConversationScenario.Step, stepIdx: Int)
    {
        val client = chatClients.computeIfAbsent(scenario)
            { this.conversation.chatClient(chatModel) }

        val prompt =
            client.prompt()

        step.system?.let { prompt.system(it) }
        step.user?.let { prompt.user(it) }

        (step.expect ?: DefaultExpectations()).assert(prompt)

//        if (step.expect?.json !=null) {
//            val resps = prompt.stream()
//                .content()
//                .content()
//                .json()
//                .collectList()
//                .block()
//            log.info("{}", resps)
//            Assertions.assertTrue(resps.isNotEmpty())
//        } else if (step.expect?.content !=null) {
//            val resps = prompt.stream()
//                .content()
//                .content()
//                .collectList()
//                .block()
//            log.info("{}", resps)
//            Assertions.assertTrue(resps.isNotEmpty())
//        } else if (step.expect == null) {
//            val resps = prompt.stream()
//                .content()
//                .content()
//                .collectList()
//                .block()
//            log.info("{}", resps)
//            Assertions.assertTrue(resps.isNotEmpty())
//        }

    }
}