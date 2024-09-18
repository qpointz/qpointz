package io.qpointz.mill.service.jdbc.configuration;

import io.qpointz.mill.service.ExecutionProvider;
import io.qpointz.mill.service.MetadataProvider;
import io.qpointz.mill.service.SqlProvider;
import io.qpointz.mill.service.calcite.CalciteContextFactory;
import io.qpointz.mill.service.calcite.providers.CalciteMetadataProvider;
import io.qpointz.mill.service.calcite.providers.CalciteSqlProvider;
import io.qpointz.mill.service.calcite.providers.PlanConverter;
import io.qpointz.mill.service.configuration.ProvidersConfig;
import io.qpointz.mill.service.jdbc.providers.JdbcContextFactory;
import io.qpointz.mill.service.jdbc.providers.JdbcExecutionProvider;
import org.springframework.context.annotation.Bean;

public class JdbcServiceProvidersConfiguration implements ProvidersConfig {

    @Bean
    public JdbcContextFactory jdbcContextFactory(JdbcConnectionConfiguration configuration) {
        return JdbcConnectionConfiguration.jdbcContext(configuration);
    }

    @Bean
    public ExecutionProvider jdbcExecutionProvider(PlanConverter converter, JdbcContextFactory jdbcContextFactory) {
        return new JdbcExecutionProvider(converter, jdbcContextFactory);
    }

    @Bean
    public MetadataProvider jdbcMetadataProvider(CalciteContextFactory ctxFactory, io.substrait.extension.ExtensionCollector extensionCollector) {
        return new CalciteMetadataProvider(ctxFactory, extensionCollector);
    }

    @Bean
    public static SqlProvider sqlParserProvider(CalciteContextFactory ctxFactory) {
        return new CalciteSqlProvider(ctxFactory);
    }

}
