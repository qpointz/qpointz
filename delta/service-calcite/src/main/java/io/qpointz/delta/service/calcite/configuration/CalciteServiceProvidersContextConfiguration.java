package io.qpointz.delta.service.calcite.configuration;


import io.qpointz.delta.service.calcite.providers.CalciteContext;
import io.qpointz.delta.service.calcite.providers.CalciteExecutionProvider;
import io.qpointz.delta.service.calcite.providers.CalciteMetadataProvider;
import io.qpointz.delta.service.calcite.providers.CalciteSqlProvider;
import io.qpointz.delta.service.ExecutionProvider;
import io.qpointz.delta.service.MetadataProvider;
import io.qpointz.delta.service.SqlProvider;
import io.qpointz.delta.service.configuration.ProvidersConfig;
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
