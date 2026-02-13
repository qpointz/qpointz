package io.qpointz.mill.ai

import io.qpointz.mill.ai.streaming.Transformations.content
import io.qpointz.mill.ai.streaming.Transformations.json
import io.qpointz.mill.ai.test.scenario.ConversationScenario
import io.qpointz.mill.utils.JsonUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.platform.commons.logging.LoggerFactory
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [BaseIntegrationTest::class])
@ActiveProfiles("test-it")
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
 class BaseIntegrationTest(@Autowired val chatModel: ChatModel) {
    companion object {
        val log = LoggerFactory.getLogger(BaseIntegrationTest::class.java)
    }

    @Test
    fun simpleOne() {
        assertNotNull(chatModel)

        val c = ContextDescriptor
            .fromResource("ai/capabilities/talk/talk.yml")
            .createCapability()

        val capabilities = emptyList<Capability>()

        val conv = ChatConversation(capabilities, ChatOptions.builder().build())
        log.info { "begin story" }

        val chatClient = conv
            .chatClient(chatModel)

        val userQuestion = "give me five random owners and 10 zoos"
        log.info { "User question $userQuestion" }
        chatClient
            .prompt()
            .user (userQuestion)
            .stream()
            .content()
            .content()
            .json()
            .toStream()
            .forEach { log.info { "Parsed json: ${JsonUtils.defaultJsonMapper().writeValueAsString(it)}" } }
    }

    @Test
    fun readMulti() {


        val conversations = ConversationScenario.fromResource("sce.yml")

        log.info { JsonUtils.defaultJsonMapper().writeValueAsString(conversations) }

    }


}