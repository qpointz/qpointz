//package io.qpointz.mill.ai.nlsql.components;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.qpointz.mill.MillRuntimeDataException;
//import io.qpointz.mill.ai.nlsql.components.tasks.ChartDataTask;
//import io.qpointz.mill.ai.nlsql.components.tasks.ExplainTask;
//import io.qpointz.mill.ai.nlsql.components.tasks.GetDataTask;
//import io.qpointz.mill.ai.nlsql.components.tasks.ReasonTask;
//import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
//import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
//import io.qpointz.mill.services.metadata.MetadataProvider;
//import lombok.val;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.chat.model.ChatModel;
//
//import java.io.IOException;
//import java.time.ZonedDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//public class ChatTaskWorkflow {
//
//    private final ChatClient mainChatClient;
//    private final MetadataProvider metadataProvider;
//    private final ChatClient reasoningChatClient;
//    private final QueryExecutor queryExecutor;
//    private final UUID chatId;
//
//    public record Response(ReasoningResponse reasoning, Map<String,Object> result, ZonedDateTime timestamp) {
//        public static Response create(ReasoningResponse reasoning, Map<String,Object> result) {
//            return new Response(reasoning, result, ZonedDateTime.now());
//        }
//    }
//
//    public ChatTaskWorkflow(ChatModel chatModel,
//                            UUID chatId,
//                            MetadataProvider metadataProvider,
//                            DataOperationDispatcher dataOperationDispatcher,
//                            ChatMemory chatMemory) throws IOException {
//        this.mainChatClient = ChatBuilders
//                .mainChat(chatModel, chatMemory)
//                .build();
//        this.reasoningChatClient = ChatBuilders
//                .reasoningChat(chatModel, chatMemory)
//                .build();
//        this.metadataProvider = metadataProvider;
//
//        this.chatId = chatId;
//
//        this.queryExecutor = new QueryExecutor(dataOperationDispatcher);
//    }
//
//    ReasonTask reasonTask(String user) {
//        return new ReasonTask(reasoningChatClient, this.metadataProvider, user);
//    }
//
//    Response reason(String user) throws IOException {
//        val map = reasonTask(user).invoke().asMap();
//        ObjectMapper om = new ObjectMapper();
//        val reason = om.convertValue(map, ReasoningResponse.class);
//        return Response.create(reason,map);
//    }
//
//    public Response call(String user) throws IOException {
//        val reasonResponse = reason(user).reasoning();
//        if (reasonResponse.isPlannedIntent()) {
//            return planned(reasonResponse);
//        }
//
//        if (reasonResponse.isUnknownIntent()) {
//            return unknown(reasonResponse);
//        }
//
//        return switch (reasonResponse.intent()) {
//            case GET_DATA ->  getData(user, reasonResponse);
//            case GET_CHART -> chartData(user, reasonResponse);
//            case EXPLAIN -> explain(user, reasonResponse);
//            default -> throw new RuntimeException("Unhandled Intent"+reasonResponse.intent());
//        };
//    }
//
//    Response explain(String user, ReasoningResponse reasonResponse) throws IOException {
//        val task = new ExplainTask(this.mainChatClient, reasonResponse.schemaScope(), reasonResponse.requiredTables(), this.metadataProvider, user, this.chatId);
//        val taskResult = task.invoke().asMap();
//        return Response.create(reasonResponse, taskResult);
//    }
//
//    Response chartData(String user, ReasoningResponse reasonResponse) throws IOException {
//        val task = new ChartDataTask(this.mainChatClient, reasonResponse.schemaScope(), reasonResponse.requiredTables(), this.metadataProvider, user, this.chatId);
//        val taskResult = task.invoke().asMap();
//        return Response.create(reasonResponse,
//                applyData(reasonResponse, taskResult));
//    }
//
//    Response getData(String user, ReasoningResponse reasonResponse) throws IOException {
//        val task = new GetDataTask(this.mainChatClient, reasonResponse.schemaScope(), reasonResponse.requiredTables(), this.metadataProvider, user, this.chatId);
//        val taskResult = task.invoke().asMap();
//        return Response.create(reasonResponse,
//                applyData(reasonResponse, taskResult));
//    }
//
//    private Map<String,Object> applyData(ReasoningResponse reason, Map<String, Object> taskResult) {
//        if (!taskResult.containsKey("sql")) {
//            throw new MillRuntimeDataException("'sql' key missing:"+reason.toString());
//        }
//
//        val sql = taskResult.get("sql").toString();
//        val newResult = new HashMap<String,Object>();
//        newResult.putAll(taskResult);
//        newResult.put("result", this.queryExecutor.submit(sql));
//        return newResult;
//    }
//
//    Response unknown(ReasoningResponse reasonResponse) {
//        throw new RuntimeException("Intent not supported"+reasonResponse.intent().toString());
//    }
//
//    Response planned(ReasoningResponse reasonResponse) {
//        throw new RuntimeException("Intent not supported"+reasonResponse.intent().toString());
//    }
//
//}
