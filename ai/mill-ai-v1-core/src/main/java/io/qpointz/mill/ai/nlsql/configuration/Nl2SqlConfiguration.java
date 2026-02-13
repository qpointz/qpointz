package io.qpointz.mill.ai.nlsql.configuration;

import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.ai.nlsql.models.SqlDialects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;

/**
 * Spring configuration that exposes helper beans for the NL2SQL module, including the SQL dialect and
 * raw value-mapping configuration blocks.
 */
@Slf4j
@Configuration
public class Nl2SqlConfiguration {

    private static final Bindable<List<Map<String, Object>>> VALUE_MAPPING_BINDABLE =
            Bindable.of(
                    ResolvableType.forClassWithGenerics(
                            List.class,
                            ResolvableType.forClassWithGenerics(Map.class, String.class, Object.class)
                    )
            );

    /**
     * Resolves the configured SQL dialect. When no value is supplied the registry falls back to the default
     * dialect.
     *
     * @param dialectName optional dialect name supplied via configuration
     * @return dialect implementation used by NL2SQL components
     */
    @Bean
    public SqlDialect nl2SqlSqlDialect(@Value("${mill.ai.nl2sql.dialect:#{null}}") String dialectName) {
        log.info("Using SQL dialect '{}'", dialectName);
        return SqlDialects
                .byName(dialectName);
    }

    /**
     * Binds the "mill.ai.nl2sql.value-mapping" configuration block to a generic list of maps. Downstream
     * components convert these entries into strongly typed document sources.
     *
     * @param environment Spring environment used for property binding
     * @return list of raw document source descriptors
     */
    @Bean
    @Qualifier("LOJOKOJ")
    public List<Map<String, Object>> vectorStoreDocumentSources(Environment environment) {
        return Binder.get(environment)
                .bind("mill.ai.nl2sql.value-mapping", VALUE_MAPPING_BINDABLE)
                .orElse(List.of());
    }

}
