package io.qpointz.mill.services.jdbc.configuration;

import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.MetadataProvider;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.providers.CalciteMetadataProvider;
import io.qpointz.mill.services.calcite.providers.CalciteSqlProvider;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.configuration.ProvidersConfig;
import io.qpointz.mill.services.jdbc.providers.JdbcContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcExecutionProvider;
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
