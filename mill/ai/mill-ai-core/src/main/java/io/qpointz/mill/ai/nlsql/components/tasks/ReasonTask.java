//package io.qpointz.mill.ai.nlsql.components.tasks;
//
//import io.qpointz.mill.ai.chat.prompts.PromptTemplate;
//import io.qpointz.mill.ai.chat.prompts.PromptTemplates;
//import io.qpointz.mill.ai.chat.prompts.templates.CompositePromptTemplate;
//import io.qpointz.mill.ai.chat.tasks.ChatTask;
//import io.qpointz.mill.ai.nlsql.components.prompts.SchemaPrompt;
//import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
//import io.qpointz.mill.services.metadata.MetadataProvider;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.val;
//import org.springframework.ai.chat.client.ChatClient;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static io.qpointz.mill.ai.chat.prompts.PromptTemplateSources.resourceSource;
//
//@AllArgsConstructor
//public class ReasonTask extends ChatTask {
//
//    private static final UUID reasoningChatId = UUID.fromString("f474495a-cb27-41a1-8c53-9d0465edc36b");
//
//    @Getter
//    private final ChatClient chatClient;
//
//    @Getter
//    private final MetadataProvider metadataProvider;
//
//    @Getter
//    private final String user;
//
//    @Override
//    protected UUID getChatId() {
//        return reasoningChatId;
//    }
//
//    @Override
//    protected PromptTemplate getUserPromptTemplate() throws IOException {
//        return SchemaPrompt.builder(this.getMetadataProvider())
//                        .includeAttributes(false)
//                        .includeRelations(true)
//                        .includeRelationExpressions(false)
//                        .build();
//    }
//
//    public ReasoningResponse callEntity() throws IOException {
//        return this.invoke().entity(ReasoningResponse.class);
//    }
//}
