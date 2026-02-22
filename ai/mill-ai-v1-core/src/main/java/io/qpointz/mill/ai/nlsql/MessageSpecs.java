package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.MessageTemplates;
import io.qpointz.mill.ai.chat.messages.specs.TemplateMessageSpec;
import io.qpointz.mill.ai.nlsql.messages.specs.SchemaMessageSpec;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.ai.nlsql.models.stepback.StepBackResponse;
import io.qpointz.mill.ai.nlsql.models.stepback.ClarificationQuestion;
import io.qpointz.mill.data.backend.metadata.MetadataProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.staticTemplate;

@Slf4j
public class MessageSpecs {

    private MessageSpecs() {
        // Prevent instantiation
    }

    private static MessageSpec systemStatic(String location) {
        return new TemplateMessageSpec(MessageType.SYSTEM,
                staticTemplate(location, MessageSpecs.class));
    }

    private static MessageSpec userStatic(String location) {
        return new TemplateMessageSpec(MessageType.USER,
                staticTemplate(location, MessageSpecs.class));
    }

    public static MessageSpec stepBackSystem() {
        return systemStatic("templates/nlsql/stepback/system.prompt");
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

    public static MessageSpec stepBackUserQuestion(String userQuestion) {
        val template = MessageTemplates.pebbleTemplate("templates/nlsql/stepback/user-question.prompt", MessageSpecs.class);
        val params = Map.<String, Object>of(
                "userQuestion", userQuestion
        );
        return new TemplateMessageSpec(MessageType.USER, template, params);
    }

    public static MessageSpec stepBackClarification(String originalQuestion,
                                                    String clarificationAnswer,
                                                    StepBackResponse previousResponse) {
        val template = MessageTemplates.pebbleTemplate("templates/nlsql/stepback/clarification.prompt", MessageSpecs.class);
        val params = Map.<String, Object>of(
                "originalQuestion", originalQuestion,
                "clarificationAnswer", clarificationAnswer,
                "previousQuestions", previousResponse != null ? previousResponse.questionsSafe() : List.of()
        );
        return new TemplateMessageSpec(MessageType.USER, template, params);
    }

    public static MessageSpec reasonClarification(String originalQuestion,
                                                  String clarificationAnswer,
                                                  List<ClarificationQuestion> previousQuestions) {
        val template = MessageTemplates.pebbleTemplate("templates/nlsql/reason/clarification.prompt", MessageSpecs.class);
        val params = Map.<String, Object>of(
                "originalQuestion", originalQuestion,
                "clarificationAnswer", clarificationAnswer,
                "previousQuestions", previousQuestions == null ? List.of() : previousQuestions
        );
        return new TemplateMessageSpec(MessageType.USER, template, params);
    }

    public static MessageList stepBack(String question, MetadataProvider metadataProvider) {
        return new MessageList(List.of(
                stepBackSystem(),
                SchemaMessageSpec.builder(MessageType.USER, metadataProvider)
                        .includeAttributes(true)
                        .includeRelationExpressions(true)
                        .includeRelations(true)
                        .build(),
                outputRules(),
                stepBackUserQuestion(question)
        ));
    }

    public static MessageList stepBackWithClarification(String originalQuestion,
                                                        String clarificationAnswer,
                                                        StepBackResponse previousResponse,
                                                        MetadataProvider metadataProvider) {
        return new MessageList(List.of(
                stepBackSystem(),
                SchemaMessageSpec.builder(MessageType.USER, metadataProvider)
                        .includeAttributes(true)
                        .includeRelationExpressions(true)
                        .includeRelations(true)
                        .build(),
                outputRules(),
                stepBackUserQuestion(originalQuestion),
                stepBackClarification(originalQuestion, clarificationAnswer, previousResponse)
        ));
    }

    public static MessageList reasonWithClarification(String originalQuestion,
                                                      String clarificationAnswer,
                                                      List<ClarificationQuestion> previousQuestions,
                                                      MetadataProvider metadataProvider) {
        return new MessageList(List.of(
                reasonSystem(),
                reasonSchema(metadataProvider),
                outputRules(),
                reasonClarification(originalQuestion, clarificationAnswer, previousQuestions)
        ));
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

    public static MessageList getData(ReasoningResponse reason, MetadataProvider provider, SqlDialect dialect) {
        return new MessageList(List.of(
            intentSystem(),
            //dialect.getConventionsSpec(reason.sqlFeatures() != null ? reason.sqlFeatures() : SqlDialect.SqlFeatures.DEFAULT),
            dialect.getConventionsSpec(SqlDialect.SqlFeatures.DEFAULT),
            getDataUser(),
            intentSchema(reason, provider),
            outputRules(),
            intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getChartUser() {
        return userStatic("templates/nlsql/intent/get-chart/user.prompt");
    }

    public static MessageList getChart(ReasoningResponse reason, MetadataProvider provider, SqlDialect dialect) {
        return new MessageList(List.of(
                intentSystem(),
                //dialect.getConventionsSpec(reason.sqlFeatures() != null ? reason.sqlFeatures() : SqlDialect.SqlFeatures.DEFAULT),
                dialect.getConventionsSpec(SqlDialect.SqlFeatures.DEFAULT),
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

    public static MessageList explain(ReasoningResponse reason, MetadataProvider provider, SqlDialect dialect) {
        return new MessageList(List.of(
                intentSystem(),
                dialect.getConventionsSpec(reason.sqlFeatures() != null ? reason.sqlFeatures() : SqlDialect.SqlFeatures.DEFAULT),
                getExplainUser(),
                fullSchema(provider),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getEnrichModelUser() {
        return userStatic("templates/nlsql/intent/enrich-model/user.prompt");
    }

    public static MessageList enrichModel(ReasoningResponse reason, MetadataProvider provider, SqlDialect dialect) {
        return new MessageList(List.of(
                intentSystem(),
                dialect.getConventionsSpec(reason.sqlFeatures() != null ? reason.sqlFeatures() : SqlDialect.SqlFeatures.DEFAULT),
                getEnrichModelUser(),
                fullSchema(provider),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getRefineUser() {
        return userStatic("templates/nlsql/intent/refine/user.prompt");
    }

    public static MessageList refineQuery(ReasoningResponse reason, MetadataProvider provider, SqlDialect dialect) {
        return new MessageList(List.of(
                intentSystem(),
                dialect.getConventionsSpec(reason.sqlFeatures() != null ? reason.sqlFeatures() : SqlDialect.SqlFeatures.DEFAULT),
                getRefineUser(),
                fullSchema(provider),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getDoConversationUser() {
        return userStatic("templates/nlsql/intent/do-conversation/user.prompt");
    }

    public static MessageList doConversation(ReasoningResponse reason, MetadataProvider provider, SqlDialect dialect) {
        return new MessageList(List.of(
                intentSystem(),
                getDoConversationUser(),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

}
