//package io.qpointz.mill.ai.nlsql.components.tasks;
//
//import io.qpointz.mill.ai.chat.prompts.templates.CompositePromptTemplate;
//import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
//import io.qpointz.mill.ai.nlsql.SchemaScope;
//import io.qpointz.mill.services.metadata.MetadataProvider;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import org.springframework.ai.chat.client.ChatClient;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static io.qpointz.mill.ai.chat.prompts.PromptTemplates.staticTemplate;
//import static io.qpointz.mill.ai.chat.prompts.templates.CompositePromptTemplate.compositeItem;
//
//@AllArgsConstructor
//public class ExplainTask extends AbstractSchemaTask {
//
//    @Getter
//    private final ChatClient chatClient;
//
//    @Getter
//    private final SchemaScope schemaScope;
//
//    @Getter
//    private final List<ReasoningResponse.IntentTable> requiredTables;
//
//    @Getter
//    private final MetadataProvider metadataProvider;
//
//    @Getter
//    private final String user;
//
//    @Getter
//    private final UUID chatId;
//
//    @Override
//    public CompositePromptTemplate.Item getUserPromptTemplateItem() throws IOException {
//        return compositeItem(
//                staticTemplate("prompts/main-explain-user.prompt",
//                        GetDataTask.class.getClassLoader()), Map.of());
//    }
//}