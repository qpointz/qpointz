package io.qpointz.mill.ai.nlsql.models;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.specs.TemplateMessageSpec;
import io.qpointz.mill.utils.YamlUtils;
import lombok.val;
import org.springframework.ai.chat.messages.MessageType;

import java.io.IOException;
import java.util.Map;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.pebbleTemplate;

public class SpecSqlDialect implements SqlDialect {

    private final Map<String, Object> sqlDialectConfig;

    public SpecSqlDialect(Map<String, Object> sqlDialectConfig) {
        this.sqlDialectConfig = sqlDialectConfig;
    }

    @Override
    public MessageSpec getConventionsSpec(SqlFeatures features) {
        val metadata = Map.of(
                "d", this.sqlDialectConfig,
                "sf" , features
        );
        return new TemplateMessageSpec(MessageType.SYSTEM,
                pebbleTemplate("templates/nlsql/dialects/sql-features.prompt", SpecSqlDialect.class),
                metadata);
    }


    public static SpecSqlDialect fromResource(String resourceLocation) {
        try (val in = SpecSqlDialect.class.getClassLoader().getResourceAsStream(resourceLocation)) {
            val config = YamlUtils.defaultYamlMapper().readValue(in, Map.class);
            return new SpecSqlDialect(config);
        } catch (IOException e) {
            throw new MillRuntimeException(e);
        }
    }

}
