package io.qpointz.mill.ai.nlsql.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties bound to {@code mill.ai.nl2sql}. This configuration captures the
 * high-level toggles that control value mapping ingestion and the raw document source blocks used
 * by the NL2SQL module.
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mill.ai.nl2sql")
@Configuration
public class ValueMappingConfiguration {

    /**
     * Flag that enables or disables value mapping ingestion.
     */
    @Getter
    @Setter
    private String enable;

    /**
     * Name of the SQL dialect to use when interacting with the target warehouse.
     */
    @Getter
    @Setter
    private String dialect;

    /**
     * Raw value mapping source documents. Each entry is further deserialised into a specialised
     * {@link ValueMappingDocumentSource}.
     */
    @Getter
    @Setter
    private List<Map<String,Object>> valueMapping;

    public ValueMappingConfiguration(Environment environment) {
        // Reserved for future use (override or post-processing of environment data).
    }

    /**
     * Marker interface for value mapping document sources with Jackson polymorphic bindings.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SqlDocumentSource.class, name = "sql")
            //@JsonSubTypes.Type(value = AttributeDocumentSource.class, name = "attribute")
    })
    public sealed interface ValueMappingDocumentSource permits SqlDocumentSource {
        String type();
    }

    /**
     * Marker interface for document sources that support scheduled refresh using cron expressions.
     */
    public sealed interface CronDocumentSource permits SqlDocumentSource {
        String cronExpression();
    }

    /**
     * Configuration block describing an SQL-based value mapping source. The SQL statement must
     * return {@code ID}, {@code VALUE}, and {@code TEXT} columns.
     *
     * @param type type discriminator; must be {@code sql}
     * @param cron cron expression controlling refresh cadence
     * @param target fully qualified target identifier ({@code SCHEMA.TABLE.COLUMN})
     * @param sql SQL statement that yields value mapping rows
     */
    public record SqlDocumentSource(String type,
                                    String cron,
                                    String target,
                                    String sql) implements ValueMappingDocumentSource, CronDocumentSource {

        @Override
        public String cronExpression() {
            return cron;
        }
    }

}
