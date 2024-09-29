package io.qpointz.mill.services.calcite.configuration;


import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.providers.CalciteExecutionProvider;
import io.qpointz.mill.services.calcite.providers.CalciteMetadataProvider;
import io.qpointz.mill.services.calcite.providers.CalciteSqlProvider;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.configuration.ProvidersConfig;
import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.MetadataProvider;
import io.qpointz.mill.services.SqlProvider;
import io.substrait.extension.ExtensionCollector;
import org.springframework.context.annotation.Bean;

public class CalciteServiceProvidersConfiguration implements ProvidersConfig {

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
