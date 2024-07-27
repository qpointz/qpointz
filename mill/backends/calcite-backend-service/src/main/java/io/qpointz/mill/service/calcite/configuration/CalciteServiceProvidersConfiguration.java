package io.qpointz.mill.service.calcite.configuration;


import io.qpointz.mill.service.calcite.CalciteContextFactory;
import io.qpointz.mill.service.calcite.providers.CalciteExecutionProvider;
import io.qpointz.mill.service.calcite.providers.CalciteMetadataProvider;
import io.qpointz.mill.service.calcite.providers.CalciteSqlProvider;
import io.qpointz.mill.service.ExecutionProvider;
import io.qpointz.mill.service.MetadataProvider;
import io.qpointz.mill.service.SqlProvider;
import io.qpointz.mill.service.configuration.ProvidersConfig;
import io.substrait.extension.ExtensionCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CalciteServiceProvidersConfiguration implements ProvidersConfig {

    @Bean
    public MetadataProvider schemaProvider(CalciteContextFactory ctxFactory, ExtensionCollector extensionCollector) {
        return new CalciteMetadataProvider(ctxFactory, extensionCollector);
    }

    @Bean
    public ExecutionProvider executionProvider(CalciteContextFactory ctxFactory) {
        return new CalciteExecutionProvider(ctxFactory);
    }

    @Bean
    public static SqlProvider sqlParserProvider(CalciteContextFactory ctxFactory) {
        return new CalciteSqlProvider(ctxFactory);
    }



}
