package io.qpointz.mill.autoconfigure.data.backend.flow;

import io.qpointz.mill.autoconfigure.data.SqlProperties;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.data.backend.ExecutionProvider;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.SqlProvider;
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory;
import io.qpointz.mill.data.backend.calcite.CalciteSqlDialectConventions;
import io.qpointz.mill.data.backend.calcite.providers.CalciteExecutionProvider;
import io.qpointz.mill.data.backend.calcite.providers.CalcitePlanConverter;
import io.qpointz.mill.data.backend.calcite.providers.CalciteSchemaProvider;
import io.qpointz.mill.data.backend.calcite.providers.CalciteSqlProvider;
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.data.backend.flow.FlowContextFactory;
import io.qpointz.mill.data.backend.flow.MultiFileSourceRepository;
import io.qpointz.mill.data.backend.flow.SourceDefinitionRepository;
import io.substrait.extension.ExtensionCollector;
import io.substrait.extension.SimpleExtension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.util.Properties;

import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;
import static io.qpointz.mill.data.backend.flow.FlowBackendConstants.BACKEND_NAME;

@Slf4j
@AutoConfiguration(after = BackendAutoConfiguration.class)
@EnableConfigurationProperties(FlowBackendProperties.class)
@ConditionalOnProperty(prefix = MILL_DATA_BACKEND_CONFIG_KEY, name = "type", havingValue = BACKEND_NAME)
public class FlowBackendAutoConfiguration {

    @Bean
    public SourceDefinitionRepository flowSourceDefinitionRepository(FlowBackendProperties props) {
        var paths = props.getSources().stream()
                .map(Path::of)
                .toList();
        log.info("Flow backend configured with {} source descriptor(s)", paths.size());
        return new MultiFileSourceRepository(paths);
    }

    @Bean
    public CalciteContextFactory flowCalciteContextFactory(
            SourceDefinitionRepository repository,
            CalciteSqlDialectConventions sqlDialectConventions,
            SqlProperties sqlProperties) {
        var conventionProps = sqlDialectConventions.asMap(sqlProperties.getConventions());
        var props = new Properties();
        props.putAll(conventionProps);
        return new FlowContextFactory(repository, props);
    }

    @Bean
    public PlanConverter flowPlanConverter(
            CalciteContextFactory ctxFactory,
            SimpleExtension.ExtensionCollection extensionCollection,
            CalciteSqlDialectConventions conventions) {
        return new CalcitePlanConverter(ctxFactory, conventions.sqlDialect(), extensionCollection);
    }

    @Bean
    public ExtensionCollector flowExtensionCollector() {
        return new ExtensionCollector();
    }

    @Bean
    public SchemaProvider flowSchemaProvider(
            CalciteContextFactory ctxFactory,
            ExtensionCollector extensionCollector) {
        return new CalciteSchemaProvider(ctxFactory, extensionCollector);
    }

    @Bean
    public ExecutionProvider flowExecutionProvider(
            CalciteContextFactory ctxFactory,
            PlanConverter converter) {
        return new CalciteExecutionProvider(ctxFactory, converter);
    }

    @Bean
    public static SqlProvider flowSqlProvider(
            CalciteContextFactory ctxFactory,
            SubstraitDispatcher substraitDispatcher) {
        return new CalciteSqlProvider(ctxFactory, substraitDispatcher);
    }
}
