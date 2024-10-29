package io.qpointz.mill.services.calcite.configuration;

import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.MetadataProvider;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.ConnectionContextFactory;
import io.qpointz.mill.services.calcite.providers.*;
import io.substrait.extension.ExtensionCollector;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "mill.backend")
@ConditionalOnProperty(prefix = "mill.backend", name="provider", havingValue = "calcite")
public class CalciteServiceConfiguration {

    @Getter
    @Value("${mill.backend.provider}")
    String  providerName;

   @Getter
   @Setter
   Map<String, String> connection;

    @Bean
    public CalciteContextFactory calciteConextFactory() {
        val props = new Properties();
        props.putAll(connection);
        return new ConnectionContextFactory(props);
    }

    @Bean
    public PlanConverter planConverter(CalciteContextFactory calciteConextFactory) {
        return new CalcitePlanConverter(calciteConextFactory, SqlDialect.DatabaseProduct.CALCITE.getDialect());
    }

    @Bean
    public ExtensionCollector extensionCollector() {
        return new ExtensionCollector();
    }

    @Bean
    public MetadataProvider schemaProvider(CalciteContextFactory ctxFactory, ExtensionCollector extensionCollector) {
        return new CalciteMetadataProvider(ctxFactory, extensionCollector);
    }

    @Bean
    public ExecutionProvider executionProvider(CalciteContextFactory ctxFactory, PlanConverter converter) {
        return new CalciteExecutionProvider(ctxFactory, converter);
    }

    @Bean
    public static SqlProvider sqlParserProvider(CalciteContextFactory ctxFactory) {
        return new CalciteSqlProvider(ctxFactory);
    }


}
