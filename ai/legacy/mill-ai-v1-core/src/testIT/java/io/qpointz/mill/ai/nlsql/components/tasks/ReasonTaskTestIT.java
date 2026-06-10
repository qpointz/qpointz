//package io.qpointz.mill.ai.nlsql.components.tasks;
//
//import io.qpointz.mill.ai.nlsql.components.ChatBuilders;
//import io.qpointz.mill.ai.nlsql.IntentTypes;
//import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
//import io.qpointz.mill.services.metadata.MetadataProvider;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@Slf4j
//@SpringBootTest(classes = {ReasonTaskTestIT.class})
//@ComponentScan(basePackages = {"io.qpointz"})
//@ActiveProfiles("test-moneta-slim-it")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//@EnableAutoConfiguration
//class ReasonTaskTestIT {
//
//    @Autowired
//    MetadataProvider metadataProvider;
//
//    private static ChatClient chat ;
//
//    @BeforeAll
//    public static void init(@Autowired ChatClient.Builder builder) throws IOException {
//        chat = ChatBuilders.reasoningChat(builder)
//                .build();
//    }
//
//    private ReasonTask taskBuilder(String query) throws IOException {
//        return new ReasonTask(chat, metadataProvider, query);
//    }
//
//    @Test
//    void simpleReasoning() throws IOException {
//        val task = taskBuilder("count clients");
//        assertDoesNotThrow(()-> task.invoke().asMap());
//    }
//
//    @Test
//    void reasonGetData() throws IOException {
//        String userQuery = "i need to get client list";
//        val task = taskBuilder(userQuery);
//        val resp = task.callEntity();
//
//        assertNotNull(resp);
//        log.info(resp.toString());
//        assertTrue(resp.intent()== IntentTypes.GET_DATA);
//        assertTrue(resp.language().compareToIgnoreCase("en")==0);
//        if (! resp.explanation().toUpperCase().contains("CLIENTS") ||
//                ! resp.explanation().toUpperCase().contains("USER")) {
//            log.warn("Explanation deviates: {}-{}", userQuery, resp.explanation() );
//        }
//    }
//
//    @Test
//    void multiTableReasoning() throws IOException {
//        val resp = taskBuilder("Get list of customers loan and loans payment")
//                .invoke().entity(ReasoningResponse.class);
//        assertTrue(resp.requiredTables().size()>1);
//    }
//
//    @Test
//    void plannedIntent() throws IOException {
//        val resp = taskBuilder(
//                "Export client list to excell")
//                .invoke().entity(ReasoningResponse.class);
//        assertTrue(resp.intent() == IntentTypes.UNSUPPORTED);
//        assertTrue(resp.isPlannedIntent());
//    }
//
//    @Test
//    void unKnwonIntent() throws IOException {
//        val resp = taskBuilder("boil snail soup")
//                .invoke().entity(ReasoningResponse.class);
//        assertNotNull(resp);
//        assertTrue(resp.intent() == IntentTypes.UNSUPPORTED);
//        assertTrue(resp.isUnknownIntent());
//    }
//
//}