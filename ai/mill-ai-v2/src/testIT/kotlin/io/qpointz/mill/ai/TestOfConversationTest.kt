//package io.qpointz.mill.ai
//
//import io.qpointz.mill.ai.test.scenario.ConversationScenario
//import org.springframework.ai.chat.model.ChatModel
//import org.springframework.ai.chat.prompt.ChatOptions
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.annotation.DirtiesContext
//import org.springframework.test.context.ActiveProfiles
//
//@SpringBootTest(classes = [BaseIntegrationTest::class])
//@ActiveProfiles("test-it")
//@EnableAutoConfiguration
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//class TestOfConversationTest(@Autowired override val chatModel: ChatModel) : ConversationScenarioTest() {
//
//    override val scenario: List<ConversationScenario>
//        get() = ConversationScenario.fromResource("sce.yml")
//
//    override val conversation: ChatConversation
//        get() {
////            val c = ContextDescriptor
////                .fromResource("ai/capabilities/talk/talk.yml")
////                .createCapability()
//            val capabilities = emptyList<Capability>()
//            return ChatConversation(capabilities, ChatOptions.builder().build())
//        }
//}