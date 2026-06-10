package io.qpointz.mill.ai.nlsql.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;

/**
 * Spring configuration that exposes helper beans for the NL2SQL module.
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
