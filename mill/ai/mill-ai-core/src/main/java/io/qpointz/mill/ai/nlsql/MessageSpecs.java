package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.MessageTemplates;
import io.qpointz.mill.ai.chat.messages.specs.TemplateMessageSpec;
import io.qpointz.mill.ai.nlsql.messages.specs.SchemaMessageSpec;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.services.metadata.MetadataProvider;
import lombok.val;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.staticTemplate;

public class MessageSpecs {

    private static MessageSpec systemStatic(String location) {
        return new TemplateMessageSpec(MessageType.SYSTEM,
                staticTemplate(location, MessageSpecs.class));
    }

    private static MessageSpec userStatic(String location) {
        return new TemplateMessageSpec(MessageType.USER,
                staticTemplate(location, MessageSpecs.class));
    }

    public static MessageSpec reasonSystem() {
        return systemStatic("templates/nlsql/reason/system.prompt");
    }

    public static MessageSpec outputRules() {
        return systemStatic("templates/nlsql/output.prompt");
    }

    public static MessageSpec reasonSchema(MetadataProvider metadataProvider) {
        return SchemaMessageSpec.builder(MessageType.USER, metadataProvider)
                .includeAttributes(false)
                .includeRelationExpressions(false)
                .includeRelations(true)
                .build();
    }

    public static MessageSpec intentSchema(ReasoningResponse response, MetadataProvider metadataProvider) {
        return SchemaMessageSpec.builder(MessageType.USER, metadataProvider)
                .requiredTables(response.requiredTables())
                .includeAttributes(true)
                .includeRelationExpressions(true)
                .includeRelations(true)
                .build();
    }

    public static MessageSpec plainUserQuestion(String userQuestion) {
        return new TemplateMessageSpec(MessageType.USER, staticTemplate(userQuestion));
    }

    public static MessageList reason(String question, MetadataProvider metadataProvider) {
        return new MessageList(List.of(
           reasonSystem(),
           reasonSchema(metadataProvider),
           outputRules(),
           plainUserQuestion(question)
        ));
    }

    public static MessageSpec intentSystem() {
        return systemStatic("templates/nlsql/intent/system.prompt");
    }

    public static MessageSpec intentUserQuestion(ReasoningResponse reason) {
        val template = MessageTemplates.pebbleTemplate("templates/nlsql/intent/user-question.prompt", MessageSpecs.class);
        val locale = new Locale(reason.language());
        val params = Map.<String, Object>of(
            "userQuestion", reason.query(),
            "languageName" ,locale.getDisplayLanguage(),
            "languageCode", locale.toLanguageTag()
        );
        return  new TemplateMessageSpec(MessageType.USER, template, params);
    }

    public static MessageSpec getDataUser() {
        return userStatic("templates/nlsql/intent/get-data/user.prompt");
    }

    public static MessageList getData(ReasoningResponse reason, MetadataProvider provider) {
        return new MessageList(List.of(
            intentSystem(),
            getDataUser(),
            intentSchema(reason, provider),
            outputRules(),
            intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getChartUser() {
        return userStatic("templates/nlsql/intent/get-chart/user.prompt");
    }

    public static MessageList getChart(ReasoningResponse reason, MetadataProvider provider) {
        return new MessageList(List.of(
                intentSystem(),
                getChartUser(),
                intentSchema(reason, provider),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getExplainUser() {
        return userStatic("templates/nlsql/intent/explain/user.prompt");
    }

    public static MessageSpec fullSchema(MetadataProvider metadataProvider) {
        return SchemaMessageSpec.builder(MessageType.USER, metadataProvider)
                .includeAttributes(true)
                .includeRelationExpressions(true)
                .includeRelations(true)
                .build();
    }

    public static MessageList explain(ReasoningResponse reason, MetadataProvider provider) {
        return new MessageList(List.of(
                intentSystem(),
                getExplainUser(),
                fullSchema(provider),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getEnrichModelUser() {
        return userStatic("templates/nlsql/intent/enrich-model/user.prompt");
    }

    public static MessageList enrichModel(ReasoningResponse reason, MetadataProvider provider) {
        return new MessageList(List.of(
                intentSystem(),
                getEnrichModelUser(),
                fullSchema(provider),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getRefineUser() {
        return userStatic("templates/nlsql/intent/refine/user.prompt");
    }

    public static MessageList refineQuery(ReasoningResponse reason, MetadataProvider provider) {
        return new MessageList(List.of(
                intentSystem(),
                getRefineUser(),
                fullSchema(provider),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

}
