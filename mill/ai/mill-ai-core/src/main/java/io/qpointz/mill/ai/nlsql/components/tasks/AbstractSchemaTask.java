//package io.qpointz.mill.ai.nlsql.components.tasks;
//
//import io.qpointz.mill.ai.chat.prompts.PromptTemplate;
//import io.qpointz.mill.ai.chat.prompts.templates.CompositePromptTemplate;
//import io.qpointz.mill.ai.chat.tasks.ChatTask;
//import io.qpointz.mill.ai.nlsql.components.prompts.SchemaPrompt;
//import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
//import io.qpointz.mill.ai.nlsql.SchemaScope;
//import io.qpointz.mill.services.metadata.MetadataProvider;
//import lombok.val;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static io.qpointz.mill.ai.chat.prompts.templates.CompositePromptTemplate.compositeItem;
//
//public abstract class AbstractSchemaTask extends ChatTask  {
//
//    public abstract SchemaScope getSchemaScope();
//
//    public abstract CompositePromptTemplate.Item getUserPromptTemplateItem() throws IOException;
//
//    public abstract List<ReasoningResponse.IntentTable> getRequiredTables();
//
//    public abstract MetadataProvider getMetadataProvider();
//
//    @Override
//    protected PromptTemplate getUserPromptTemplate() throws IOException {
//        val templateItems = new ArrayList<CompositePromptTemplate.Item>();
//        templateItems.add(this.getUserPromptTemplateItem());
//
//        if (this.getSchemaScope()!= SchemaScope.NONE) {
//            templateItems.add(schemaPromptTemplate());
//        }
//
//        return new CompositePromptTemplate(templateItems);
//    }
//
//    private CompositePromptTemplate.Item schemaPromptTemplate() {
//        val requieredTables = this.getSchemaScope() == SchemaScope.FULL
//                ? List.<ReasoningResponse.IntentTable>of() //full schema will be build
//                : this.getRequiredTables();
//
//        val schemaPromptTemplate = SchemaPrompt.builder(this.getMetadataProvider())
//                .includeAttributes(true)
//                .includeRelations(true)
//                .includeRelationExpressions(true)
//                .requiredTables(requieredTables)
//                .build();
//        return compositeItem(schemaPromptTemplate);
//    }
//
//}
