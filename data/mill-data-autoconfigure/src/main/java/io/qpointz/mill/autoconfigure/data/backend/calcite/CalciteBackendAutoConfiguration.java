package io.qpointz.mill.autoconfigure.data.backend.calcite;

import io.qpointz.mill.autoconfigure.data.SqlProperties;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.data.backend.ExecutionProvider;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.SqlProvider;
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory;
import io.qpointz.mill.data.backend.calcite.CalciteSqlDialectConventions;
import io.qpointz.mill.data.backend.calcite.ConnectionContextFactory;
import io.qpointz.mill.data.backend.calcite.providers.*;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.substrait.extension.ExtensionCollector;
import io.substrait.extension.SimpleExtension;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import java.util.HashMap;
import java.util.Properties;
import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;

@Slf4j
@AutoConfiguration(after = BackendAutoConfiguration.class)
@EnableConfigurationProperties(CalciteBackendProperties.class)
@ConditionalOnProperty(prefix = MILL_DATA_BACKEND_CONFIG_KEY, name = "type", havingValue = "calcite", matchIfMissing = true)
public class CalciteBackendAutoConfiguration {

    @Bean
    public CalciteContextFactory calciteBackendCalciteContextFactory( CalciteSqlDialectConventions sqlDialectConventions,
                                                        SqlProperties sqlProperties,
                                                        CalciteBackendProperties calciteBackendProperties
    ) {
        val conventionProps = sqlDialectConventions.asMap(sqlProperties.getConventions());
        val allProps = new HashMap<>(conventionProps);

        if (calciteBackendProperties.model != null) {
            allProps.put("model", calciteBackendProperties.model);
        } else {
            log.warn("No model specified for Calcite backend");
        }



        val props = new Properties();
        props.putAll(allProps);

        if (log.isDebugEnabled()) {
            props.keySet().forEach(k -> log.debug("SQL dialect convention: {}={}", k, props.get(k)));
        }
        return new ConnectionContextFactory(props);
    }



    @Bean
    public PlanConverter calciteBackendPlanConverter(CalciteContextFactory calciteConextFactory,
                                                     SimpleExtension.ExtensionCollection extensionCollection,
                                                     CalciteSqlDialectConventions calciteDialectConventions) {
        return new CalcitePlanConverter(calciteConextFactory,
                                        calciteDialectConventions.sqlDialect(),
                                        extensionCollection);
    }

    @Bean
    public ExtensionCollector calciteBackendExtensionCollector() {
        return new ExtensionCollector();
    }

    @Bean
    public SchemaProvider calciteBackendSchemaProvider(CalciteContextFactory ctxFactory, ExtensionCollector extensionCollector) {
        return new CalciteSchemaProvider(ctxFactory, extensionCollector);
    }

    @Bean
    public ExecutionProvider calciteBackendExecutionProvider(CalciteContextFactory ctxFactory, PlanConverter converter) {
        return new CalciteExecutionProvider(ctxFactory, converter);
    }

    @Bean
    public static SqlProvider calciteBackendSqlParserProvider(CalciteContextFactory ctxFactory, SubstraitDispatcher substraitDispatcher) {
        return new CalciteSqlProvider(ctxFactory, substraitDispatcher);
    }


}
