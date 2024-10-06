package io.qpointz.mill.azure.functions.configuration;

import io.qpointz.mill.services.*;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceProperties;
import io.qpointz.mill.services.calcite.providers.CalciteMetadataProvider;
import io.qpointz.mill.services.calcite.providers.CalcitePlanConverter;
import io.qpointz.mill.services.calcite.providers.CalciteSqlProvider;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.jdbc.configuration.JdbcConnectionConfiguration;
import io.qpointz.mill.services.jdbc.providers.JdbcCalciteContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcExecutionProvider;
import io.substrait.extension.ExtensionCollector;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Import({CalciteServiceProperties.class})
public class BackendFunctionConfiguration {

//    @Bean
//    public static ServiceHandler serviceHandler(@Autowired MetadataProvider metadataProvider,
//                                                @Autowired ExecutionProvider executionProvider,
//                                                @Autowired(required = false) SqlProvider sqlProvider,
//                                                @Autowired(required = false) SecurityProvider securityProvider,
//                                                @Autowired(required = false) PlanRewriteChain planRewriteChain)
//    {
//        return new ServiceHandler(metadataProvider, executionProvider, sqlProvider, securityProvider, planRewriteChain);
//    }

    @Bean
    public CalciteContextFactory calciteContextFactory(CalciteServiceProperties properties,
                                                       JdbcConnectionConfiguration jdbcConfig,
                                                       @Value("${mill.backend.jdbc.schema-name}") String schemaName) {
        val props = new Properties();
        props.putAll(properties.getConnection());
        return new JdbcCalciteContextFactory(props, jdbcConfig, schemaName);
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
