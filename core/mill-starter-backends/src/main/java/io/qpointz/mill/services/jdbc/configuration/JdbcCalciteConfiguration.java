package io.qpointz.mill.services.jdbc.configuration;


import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.SchemaProvider;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.providers.CalciteSchemaProvider;
import io.qpointz.mill.services.calcite.providers.CalcitePlanConverter;
import io.qpointz.mill.services.calcite.providers.CalciteSqlProvider;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.configuration.BackendConfiguration;
import io.qpointz.mill.services.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.services.jdbc.providers.*;
import io.qpointz.mill.services.jdbc.providers.impl.JdbcConnectionCustomizerImpl;
import io.qpointz.mill.services.jdbc.providers.impl.JdbcContextFactoryImpl;
import io.substrait.extension.ExtensionCollector;
import io.substrait.extension.SimpleExtension;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Properties;

@Component
@ConditionalOnProperty(prefix = "mill.backend", name="provider", havingValue = "jdbc")
@EnableConfigurationProperties
@ConfigurationProperties(prefix="mill.backend.jdbc")
public class JdbcCalciteConfiguration {

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.url}")
    private String url;

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.driver}")
    private String driver;

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.user:#{null}}")
    private Optional<String> user = Optional.empty();

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.password:#{null}}")
    private Optional<String> password = Optional.empty();

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.target-schema:ts}")
    private Optional<String> targetSchema= Optional.empty();

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.schema:#{null}}")
    private Optional<String> schema= Optional.empty();

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.catalog:#{null}}")
    private Optional<String> catalog= Optional.empty();

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.multi-shema:#{false}}")
    private Boolean multiSchema= false;

    @Bean
    public JdbcContextFactory jdbcContextFactory(JdbcConnectionProvider jdbcConnectionProvider) {
        return new JdbcContextFactoryImpl(this, jdbcConnectionProvider);
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
    public CalciteContextFactory calciteContextFactory(BackendConfiguration backendConfiguration,
                                                       JdbcConnectionProvider jdbcConnectionProvider) {
        val props = new Properties();
        props.putAll(backendConfiguration.getConnection());
        return new JdbcCalciteContextFactory(props, this, this.targetSchema, jdbcConnectionProvider);
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
