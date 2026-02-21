package io.qpointz.mill.autoconfigure.data.backend.jdbc;

import io.qpointz.mill.autoconfigure.data.SqlProperties;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.SchemaProvider;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.CalciteSqlDialectConventions;
import io.qpointz.mill.services.calcite.providers.CalcitePlanConverter;
import io.qpointz.mill.services.calcite.providers.CalciteSchemaProvider;
import io.qpointz.mill.services.calcite.providers.CalciteSqlProvider;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.services.jdbc.JdbcCalciteConfiguration;
import io.qpointz.mill.services.jdbc.providers.JdbcCalciteContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcConnectionProvider;
import io.qpointz.mill.services.jdbc.providers.JdbcContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcExecutionProvider;
import io.qpointz.mill.services.jdbc.providers.impl.JdbcConnectionCustomizerImpl;
import io.qpointz.mill.services.jdbc.providers.impl.JdbcContextFactoryImpl;
import io.substrait.extension.ExtensionCollector;
import io.substrait.extension.SimpleExtension;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Properties;

import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;

@Slf4j
@AutoConfiguration(after = BackendAutoConfiguration.class)
@EnableConfigurationProperties(JdbcBackendProperties.class)
@ConditionalOnProperty(prefix = MILL_DATA_BACKEND_CONFIG_KEY, name = "type", havingValue = "jdbc")
public class JdbcBackendAutoConfiguration {

    @Bean
    public JdbcCalciteConfiguration jdbcCalciteConfiguration(JdbcBackendProperties backendProperties) {
        log.warn("Using legacy jdbcCalciteConfiguration");
        return JdbcCalciteConfiguration.builder()
                .url(backendProperties.getUrl())
                .driver(backendProperties.getDriver())
                .user(backendProperties.getUser())
                .password(backendProperties.getPassword())
                .targetSchema(backendProperties.getTargetSchema())
                .schema(backendProperties.getSchema())
                .catalog(backendProperties.getCatalog())
                .multiSchema(backendProperties.getMultiSchema())
                .build();
    }

    @Bean
    public JdbcContextFactory jdbcContextFactory(JdbcConnectionProvider jdbcConnectionProvider, JdbcCalciteConfiguration jdbcCalciteConfiguration) {
        return new JdbcContextFactoryImpl(jdbcCalciteConfiguration, jdbcConnectionProvider);
    }

    @Bean
    public ExecutionProvider jdbcExecutionProvider(PlanConverter converter, JdbcContextFactory jdbcContextFactory) {
        return new JdbcExecutionProvider(converter, jdbcContextFactory);
    }

    @Bean
    public SchemaProvider jdbcMetadataProvider(CalciteContextFactory ctxFactory, io.substrait.extension.ExtensionCollector extensionCollector) {
        return new CalciteSchemaProvider(ctxFactory, extensionCollector);
    }

    @Bean
    public static SqlProvider sqlParserProvider(CalciteContextFactory ctxFactory, SubstraitDispatcher substraitDispatcher) {
        return new CalciteSqlProvider(ctxFactory, substraitDispatcher);
    }


    @Bean
    public CalciteContextFactory calciteContextFactory( CalciteSqlDialectConventions sqlDialectConventions,
                                                        SqlProperties sqlProperties,
                                                        JdbcConnectionProvider jdbcConnectionProvider,
                                                        JdbcCalciteConfiguration jdbcCalciteConfiguration
    ) {
        val conventionProps = sqlDialectConventions.asMap(sqlProperties.getConventions());
        val allProps = new HashMap<>(conventionProps);
        val props = new Properties();
        props.putAll(allProps);

        if (log.isDebugEnabled()) {
            props.keySet().forEach(k -> log.debug("SQL dialect convention: {}={}", k, props.get(k)));
        }
        return new JdbcCalciteContextFactory(props, jdbcCalciteConfiguration, jdbcConnectionProvider);
    }

    @Bean
    public PlanConverter planConverter(CalciteContextFactory calciteConextFactory, SimpleExtension.ExtensionCollection extensionCollection) {
        return new CalcitePlanConverter(calciteConextFactory, SqlDialect.DatabaseProduct.CALCITE.getDialect(), extensionCollection);
    }

    @Bean
    public ExtensionCollector extensionCollector() {
        return new ExtensionCollector();
    }

    @Bean
    @ConditionalOnMissingBean(JdbcConnectionProvider.class)
    public JdbcConnectionProvider defaultJdbcConnectionProvider() {
        return new JdbcConnectionCustomizerImpl();
    }


}
