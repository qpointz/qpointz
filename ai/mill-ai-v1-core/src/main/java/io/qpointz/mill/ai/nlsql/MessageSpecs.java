package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.MessageTemplates;
import io.qpointz.mill.ai.chat.messages.specs.TemplateMessageSpec;
import io.qpointz.mill.ai.nlsql.messages.specs.SchemaMessageSpec;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlFeatures;
import io.qpointz.mill.ai.nlsql.models.stepback.StepBackResponse;
import io.qpointz.mill.ai.nlsql.models.stepback.ClarificationQuestion;
import io.qpointz.mill.ai.nlsql.metadata.SchemaMessageMetadataPorts;
import io.qpointz.mill.sql.v2.dialect.FunctionEntry;
import io.qpointz.mill.sql.v2.dialect.OperatorEntry;
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.staticTemplate;

@Slf4j
public class MessageSpecs {

    private MessageSpecs() {
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

    public static MessageSpec reasonSchema(SchemaMessageMetadataPorts schemaPorts) {
        return SchemaMessageSpec.forMetadata(MessageType.USER, schemaPorts)
                .includeAttributes(false)
                .includeRelationExpressions(false)
                .includeRelations(true)
                .build();
    }

    public static MessageSpec intentSchema(ReasoningResponse response, SchemaMessageMetadataPorts schemaPorts) {
        return SchemaMessageSpec.forMetadata(MessageType.USER, schemaPorts)
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

    public static MessageList stepBack(String question, SchemaMessageMetadataPorts schemaPorts) {
        return new MessageList(List.of(
                stepBackSystem(),
                SchemaMessageSpec.forMetadata(MessageType.USER, schemaPorts)
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
                                                        SchemaMessageMetadataPorts schemaPorts) {
        return new MessageList(List.of(
                stepBackSystem(),
                SchemaMessageSpec.forMetadata(MessageType.USER, schemaPorts)
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
                                                      SchemaMessageMetadataPorts schemaPorts) {
        return new MessageList(List.of(
                reasonSystem(),
                reasonSchema(schemaPorts),
                outputRules(),
                reasonClarification(originalQuestion, clarificationAnswer, previousQuestions)
        ));
    }

    public static MessageList reason(String question, SchemaMessageMetadataPorts schemaPorts) {
        return new MessageList(List.of(
           reasonSystem(),
           reasonSchema(schemaPorts),
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
            "languageName", locale.getDisplayLanguage(),
            "languageCode", locale.toLanguageTag()
        );
        return new TemplateMessageSpec(MessageType.USER, template, params);
    }

    public static MessageSpec getDataUser() {
        return userStatic("templates/nlsql/intent/get-data/user.prompt");
    }

    public static MessageList getData(ReasoningResponse reason, SchemaMessageMetadataPorts schemaPorts, SqlDialectSpec dialect) {
        return new MessageList(List.of(
            intentSystem(),
            dialectConventionsSpec(dialect, SqlFeatures.DEFAULT),
            getDataUser(),
            intentSchema(reason, schemaPorts),
            outputRules(),
            intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getChartUser() {
        return userStatic("templates/nlsql/intent/get-chart/user.prompt");
    }

    public static MessageList getChart(ReasoningResponse reason, SchemaMessageMetadataPorts schemaPorts, SqlDialectSpec dialect) {
        return new MessageList(List.of(
                intentSystem(),
                dialectConventionsSpec(dialect, SqlFeatures.DEFAULT),
                getChartUser(),
                intentSchema(reason, schemaPorts),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getExplainUser() {
        return userStatic("templates/nlsql/intent/explain/user.prompt");
    }

    public static MessageSpec fullSchema(SchemaMessageMetadataPorts schemaPorts) {
        return SchemaMessageSpec.forMetadata(MessageType.USER, schemaPorts)
                .includeAttributes(true)
                .includeRelationExpressions(true)
                .includeRelations(true)
                .build();
    }

    public static MessageList explain(ReasoningResponse reason, SchemaMessageMetadataPorts schemaPorts, SqlDialectSpec dialect) {
        return new MessageList(List.of(
                intentSystem(),
                dialectConventionsSpec(dialect, reason.sqlFeatures() != null ? reason.sqlFeatures() : SqlFeatures.DEFAULT),
                getExplainUser(),
                fullSchema(schemaPorts),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getEnrichModelUser() {
        return userStatic("templates/nlsql/intent/enrich-model/user.prompt");
    }

    public static MessageList enrichModel(ReasoningResponse reason, SchemaMessageMetadataPorts schemaPorts, SqlDialectSpec dialect) {
        return new MessageList(List.of(
                intentSystem(),
                dialectConventionsSpec(dialect, reason.sqlFeatures() != null ? reason.sqlFeatures() : SqlFeatures.DEFAULT),
                getEnrichModelUser(),
                fullSchema(schemaPorts),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getRefineUser() {
        return userStatic("templates/nlsql/intent/refine/user.prompt");
    }

    public static MessageList refineQuery(ReasoningResponse reason, SchemaMessageMetadataPorts schemaPorts, SqlDialectSpec dialect) {
        return new MessageList(List.of(
                intentSystem(),
                dialectConventionsSpec(dialect, reason.sqlFeatures() != null ? reason.sqlFeatures() : SqlFeatures.DEFAULT),
                getRefineUser(),
                fullSchema(schemaPorts),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec getDoConversationUser() {
        return userStatic("templates/nlsql/intent/do-conversation/user.prompt");
    }

    public static MessageList doConversation(ReasoningResponse reason, SchemaMessageMetadataPorts schemaPorts, SqlDialectSpec dialect) {
        return new MessageList(List.of(
                intentSystem(),
                getDoConversationUser(),
                outputRules(),
                intentUserQuestion(reason)
        ));
    }

    private static MessageSpec dialectConventionsSpec(SqlDialectSpec dialect, SqlFeatures features) {
        return new TemplateMessageSpec(
                MessageType.SYSTEM,
                MessageTemplates.text(renderDialectConventions(dialect, features))
        );
    }

    private static String renderDialectConventions(SqlDialectSpec dialect, SqlFeatures features) {
        final StringBuilder sb = new StringBuilder();
        sb.append("# Rules\n")
                .append("- Follow the dialect identifier quoting/casing and literal formats.\n")
                .append("- Prefer explicit JOIN with ON unless dialect rules say otherwise.\n")
                .append("- Use listed operators/functions only.\n")
                .append("- Return only SQL unless explicitly asked otherwise.\n\n")
                .append("# Dialect\n")
                .append("- id: ").append(dialect.getId()).append("\n")
                .append("- name: ").append(dialect.getName()).append("\n\n");

        if (features.identifiers()) {
            sb.append("## Identifiers\n")
                    .append("- Identifier quote: ")
                    .append(dialect.getIdentifiers().getQuote().getStart())
                    .append(" ... ")
                    .append(dialect.getIdentifiers().getQuote().getEnd())
                    .append("\n")
                    .append("- Alias quote: ")
                    .append(dialect.getIdentifiers().getAliasQuote().getStart())
                    .append(" ... ")
                    .append(dialect.getIdentifiers().getAliasQuote().getEnd())
                    .append("\n")
                    .append("- Unquoted storage: ").append(dialect.getIdentifiers().getUnquotedStorage()).append("\n")
                    .append("- Quoted storage: ").append(dialect.getIdentifiers().getQuotedStorage()).append("\n")
                    .append("- Use fully qualified names: ").append(dialect.getIdentifiers().getUseFullyQualifiedNames()).append("\n\n");
        }

        if (features.literals()) {
            sb.append("## Literals\n")
                    .append("- Strings quote: ").append(dialect.getLiterals().getStrings().getQuote()).append("\n")
                    .append("- Strings concat: ").append(dialect.getLiterals().getStrings().getConcat()).append("\n")
                    .append("- Strings escape: ").append(dialect.getLiterals().getStrings().getEscape()).append("\n")
                    .append("- Booleans: ").append(String.join(", ", dialect.getLiterals().getBooleans())).append("\n")
                    .append("- NULL: ").append(dialect.getLiterals().getNullLiteral()).append("\n")
                    .append("- DATE syntax: ").append(dialect.getLiterals().getDatesTimes().getDate().getSyntax()).append("\n")
                    .append("- TIME syntax: ").append(dialect.getLiterals().getDatesTimes().getTime().getSyntax()).append("\n")
                    .append("- TIMESTAMP syntax: ").append(dialect.getLiterals().getDatesTimes().getTimestamp().getSyntax()).append("\n")
                    .append("- INTERVAL style: ").append(dialect.getLiterals().getDatesTimes().getInterval().getStyle()).append("\n\n");
        }

        if (features.joins()) {
            sb.append("## Joins\n")
                    .append("- Style: ").append(dialect.getJoins().getStyle()).append("\n")
                    .append("- INNER JOIN: ").append(dialect.getJoins().getInnerJoin().getKeyword()).append("\n")
                    .append("- LEFT JOIN: ").append(dialect.getJoins().getLeftJoin().getKeyword()).append("\n")
                    .append("- RIGHT JOIN: ").append(dialect.getJoins().getRightJoin().getKeyword()).append("\n")
                    .append("- FULL JOIN: ").append(dialect.getJoins().getFullJoin().getKeyword()).append("\n")
                    .append("- ON clause keyword: ").append(dialect.getJoins().getOnClause().getKeyword()).append("\n\n");
        }

        if (features.ordering()) {
            sb.append("## Ordering\n")
                    .append("- supports NULLS FIRST: ").append(dialect.getNullSorting().getSupportsNullsFirst()).append("\n")
                    .append("- supports NULLS LAST: ").append(dialect.getNullSorting().getSupportsNullsLast()).append("\n\n");
        }

        if (features.grouping()) {
            sb.append("## Grouping\n");
            final List<FunctionEntry> aggregates = categoryEntries(dialect.getFunctions(), "aggregates");
            if (aggregates.isEmpty()) {
                sb.append("- No aggregate functions listed.\n\n");
            } else {
                for (FunctionEntry aggregate : aggregates) {
                    sb.append("- ").append(aggregate.getName()).append(": ").append(aggregate.getSyntax()).append("\n");
                }
                sb.append("\n");
            }
        }

        if (features.paging()) {
            sb.append("## Paging\n")
                    .append("- offset keyword: ").append(dialect.getPaging().getOffset()).append("\n");
            if (dialect.getPaging().getStyles() != null) {
                for (var style : dialect.getPaging().getStyles()) {
                    sb.append("- style: ").append(style.getType()).append(" -> ").append(style.getSyntax()).append("\n");
                }
            }
            sb.append("\n");
        }

        appendOperators(sb, dialect, features.operators());
        appendFunctions(sb, dialect, features.functions());
        if (dialect.getNotes() != null && !dialect.getNotes().isEmpty()) {
            sb.append("## Notes\n");
            for (String note : dialect.getNotes()) {
                sb.append("- ").append(note).append("\n");
            }
        }
        return sb.toString();
    }

    private static void appendOperators(StringBuilder sb, SqlDialectSpec dialect, Set<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        sb.append("\n## Operators\n");
        for (String category : categories) {
            final List<OperatorEntry> entries = categoryEntries(dialect.getOperators(), category);
            sb.append("- ").append(category).append(":\n");
            if (entries.isEmpty()) {
                sb.append("  - none\n");
                continue;
            }
            for (OperatorEntry entry : entries) {
                sb.append("  - ").append(entry.getSymbol());
                if (entry.getSyntax() != null && !entry.getSyntax().isBlank()) {
                    sb.append(" — ").append(entry.getSyntax());
                }
                if (entry.getSupported() != null) {
                    sb.append(" (supported=").append(entry.getSupported()).append(")");
                }
                sb.append("\n");
            }
        }
    }

    private static void appendFunctions(StringBuilder sb, SqlDialectSpec dialect, Set<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        sb.append("\n## Functions\n");
        for (String category : categories) {
            final List<FunctionEntry> entries = categoryEntries(dialect.getFunctions(), category);
            sb.append("- ").append(category).append(":\n");
            if (entries.isEmpty()) {
                sb.append("  - none\n");
                continue;
            }
            for (FunctionEntry entry : entries) {
                sb.append("  - ").append(entry.getName()).append(": ").append(entry.getSyntax()).append("\n");
            }
        }
    }

    private static <T> List<T> categoryEntries(Map<String, List<T>> source, String key) {
        if (source == null || key == null) {
            return List.of();
        }
        final List<T> direct = source.get(key);
        if (direct != null) {
            return direct;
        }
        final List<T> underscoreToHyphen = source.get(key.replace('_', '-'));
        if (underscoreToHyphen != null) {
            return underscoreToHyphen;
        }
        final List<T> hyphenToUnderscore = source.get(key.replace('-', '_'));
        return hyphenToUnderscore == null ? List.of() : hyphenToUnderscore;
    }
}
