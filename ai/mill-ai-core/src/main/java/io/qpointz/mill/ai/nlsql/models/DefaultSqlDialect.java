package io.qpointz.mill.ai.nlsql.models;

import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.MessageSpecs;
import io.qpointz.mill.ai.chat.messages.specs.TemplateMessageSpec;
import org.springframework.ai.chat.messages.MessageType;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.staticTemplate;

public class DefaultSqlDialect implements SqlDialect {

    @Override
    public MessageSpec getConventionsSpec(SqlDialect.SqlFeatures features) {
        return new TemplateMessageSpec(MessageType.SYSTEM,
                staticTemplate("templates/nlsql/dialects/default/dialect.prompt",
                        MessageSpecs.class));
    }

}
