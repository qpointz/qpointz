package io.qpointz.mill.service.calcite.configuration;


import io.qpointz.mill.service.calcite.providers.CalciteContext;
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
public class CalciteServiceProvidersContextConfiguration implements ProvidersConfig {

    @Bean
    public MetadataProvider schemaProvider(CalciteContext calciteContext, ExtensionCollector extensionCollector) {
        return new CalciteMetadataProvider(calciteContext, extensionCollector);
    }

    @Bean
    public ExecutionProvider executionProvider(CalciteContext calciteContext) {
        return new CalciteExecutionProvider(calciteContext);
    }

    @Bean
    public static SqlProvider sqlParserProvider(CalciteContext calciteContext) {
        return new CalciteSqlProvider(calciteContext);
    }



}
