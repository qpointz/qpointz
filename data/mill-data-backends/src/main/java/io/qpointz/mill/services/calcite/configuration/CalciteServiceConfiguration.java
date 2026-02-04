package io.qpointz.mill.services.calcite.configuration;

import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.SchemaProvider;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.ConnectionContextFactory;
import io.qpointz.mill.services.calcite.providers.*;
import io.qpointz.mill.services.configuration.BackendConfiguration;
import io.qpointz.mill.services.dispatchers.SubstraitDispatcher;
import io.substrait.extension.ExtensionCollector;
import io.substrait.extension.SimpleExtension;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConditionalOnProperty(prefix = "mill.backend", name="provider", havingValue = "calcite")
@EnableConfigurationProperties
@ConfigurationProperties(prefix="mill.backend.calcite")
public class CalciteServiceConfiguration {

    @Bean
    public CalciteContextFactory calciteConextFactory(BackendConfiguration backendConfiguration) {
        val props = new Properties();
        props.putAll(backendConfiguration.getConnection());
        return new ConnectionContextFactory(props);
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
    public SchemaProvider schemaProvider(CalciteContextFactory ctxFactory, ExtensionCollector extensionCollector) {
        return new CalciteSchemaProvider(ctxFactory, extensionCollector);
    }

    @Bean
    public ExecutionProvider executionProvider(CalciteContextFactory ctxFactory, PlanConverter converter) {
        return new CalciteExecutionProvider(ctxFactory, converter);
    }

    @Bean
    public static SqlProvider sqlParserProvider(CalciteContextFactory ctxFactory, SubstraitDispatcher substraitDispatcher) {
        return new CalciteSqlProvider(ctxFactory, substraitDispatcher);
    }


}
