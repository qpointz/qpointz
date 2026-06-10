//package io.qpointz.mill.ai.nlsql.components;
//
//import io.qpointz.mill.ai.nlsql.IntentTypes;
//import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
//import io.qpointz.mill.services.metadata.MetadataProvider;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
//import org.springframework.ai.chat.memory.MessageWindowChatMemory;
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.io.IOException;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Slf4j
//@SpringBootTest(classes = {ChatTaskWorkflowTestIT.class})
//@ComponentScan(basePackages = {"io.qpointz"})
//@ActiveProfiles("test-moneta-slim-it")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//@EnableAutoConfiguration
//public class ChatTaskWorkflowTestIT {
//
//
//    @Autowired
//    private DataOperationDispatcher dataOperationDispatcher;
//
//    @Autowired
//    private ChatModel chatModel;
//
//    @Autowired
//    private MetadataProvider metadataProvider;
//
//    ChatTaskWorkflow workflow() throws IOException {
//        val chatMemory = MessageWindowChatMemory.builder()
//                .maxMessages(200)
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())
//                .build();
//        return new ChatTaskWorkflow(chatModel, UUID.randomUUID(),  metadataProvider, dataOperationDispatcher, chatMemory);
//    }
//
//    @Test
//    void reason() {
//        assertDoesNotThrow(()->workflow().reason("how many clients"));
//    }
//
//    @Test
//    void getData() throws IOException {
//        val howManyClients = "how many clients?";
//        val reason = workflow().reason(howManyClients);
//        assertTrue(reason.reasoning().intent()== IntentTypes.GET_DATA);
//        val result = workflow().call(howManyClients);
//        assertNotNull(result);
//        assertTrue(result.result().containsKey("result"));
//    }
//
//    @Test
//    void chartData() throws IOException {
//        val howManyClients = "count clients by country as bar chart";
//        val reason = workflow().reason(howManyClients);
//        assertTrue(reason.reasoning().intent()== IntentTypes.GET_CHART);
//        val result = workflow().call(howManyClients);
//        assertNotNull(result);
//        assertTrue(result.result().containsKey("result"));
//        assertTrue(result.result().containsKey("chart"));
//    }
//
//    @Test
//    void explain() throws IOException {
//        val howManyClients = "describe data model";
//        val reason = workflow().reason(howManyClients);
//        assertTrue(reason.reasoning().intent()== IntentTypes.EXPLAIN);
//        val result = workflow().call(howManyClients);
//        assertNotNull(result);
//    }
//
//}
