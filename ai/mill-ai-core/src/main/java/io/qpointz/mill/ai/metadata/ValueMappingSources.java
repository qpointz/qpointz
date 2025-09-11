package io.qpointz.mill.ai.metadata;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.qpointz.mill.ai.nlsql.configuration.ValueMappingConfiguration;

public final class ValueMappingSources {

    private ValueMappingSources() {
        //prevent instance creation
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ValueMappingConfiguration.SqlDocumentSource.class, name = "sql"),
            //@JsonSubTypes.Type(value = SqlDocumentSource.class, name = "sql"),
            //@JsonSubTypes.Type(value = AttributeDocumentSource.class, name = "attribute")
    })
    public sealed interface ValueMappingSource {
        String type();
    }

    public sealed interface CronValueMappingSource {
        String cronExpression();
    }

    public record SqlDocumentSource(String type,
                                    String cron,
                                    String target,
                                    String sql)  implements ValueMappingSource, CronValueMappingSource {

        @Override
        public String cronExpression() {
            return cron;
        }
    }

}
