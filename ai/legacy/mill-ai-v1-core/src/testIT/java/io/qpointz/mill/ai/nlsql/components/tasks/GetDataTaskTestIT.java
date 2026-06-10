//package io.qpointz.mill.ai.nlsql.components.tasks;
//
//import io.qpointz.mill.ai.nlsql.components.ChatBuilders;
//import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
//import io.qpointz.mill.ai.nlsql.SchemaScope;
//import io.qpointz.mill.proto.QueryExecutionConfig;
//import io.qpointz.mill.proto.QueryRequest;
//import io.qpointz.mill.proto.SQLStatement;
//import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
//import io.qpointz.mill.services.metadata.MetadataProvider;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@Slf4j
//@SpringBootTest(classes = {GetDataTaskTestIT.class})
//@ComponentScan(basePackages = {"io.qpointz"})
//@ActiveProfiles("test-moneta-slim-it")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//@EnableAutoConfiguration
//class GetDataTaskTestIT {
//
//    @Autowired
//    ChatModel chatModel;
//
//    @Autowired
//    MetadataProvider metadataProvider;
//
//    @Autowired
//    DataOperationDispatcher dataOperationDispatcher;
//
//    @Test
//    void trivia() throws IOException {
//        val chatClient =ChatBuilders
//                .mainChat(chatModel)
//                .build();
//
//        val task = GetDataTask.builder()
//                .chatClient(chatClient)
//                .metadataProvider(metadataProvider)
//                .requiredTables(List.of(
//                        new ReasoningResponse.IntentTable("MONETA", "CLIENTS", false, false)
//                ))
//                .schemaScope(SchemaScope.PARTIAL)
//                .user("count clients by country")
//                .build();
//
//        val map = task.invoke().asMap();
//        assertTrue(map.containsKey("sql"));
//    }
//
//    @Test
//    void queryWithReasoning() throws IOException {
//        String userQuery = "count clients by country";
//        val reasonChat = ChatBuilders
//                .reasoningChat(chatModel)
//                .build();
//
//        val reasonTask = new ReasonTask(reasonChat, metadataProvider, userQuery);
//
//        val reason = reasonTask.invoke().entity(ReasoningResponse.class);
//
//        val mainChat = ChatBuilders
//                .mainChat(chatModel)
//                .build();
//
//        val getDataTask = new GetDataTask(mainChat,
//                reason.schemaScope(),
//                reason.requiredTables(),
//                metadataProvider,
//                userQuery,
//                UUID.randomUUID());
//
//        val result = getDataTask.invoke().entity(GetDataTask.GetDataResponse.class); //wam up
//
//        val query = dataOperationDispatcher.submitQuery(QueryRequest.newBuilder()
//                        .setConfig(QueryExecutionConfig.newBuilder()
//                                .setFetchSize(20)
//                                .build())
//                        .setStatement(SQLStatement.newBuilder()
//                                .setSql(result.sql())
//                                .build())
//                .build());
//
//        log.info("Paging Id:{}",query.getPagingId());
//        log.info("Batch size:{}", query.getVector().getVectorSize());
//    }
//
//
//}