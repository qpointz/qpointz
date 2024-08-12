package io.qpointz.mill.service.jdbc.configuration;


import io.qpointz.mill.service.calcite.CalciteContextFactory;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceProperties;
import io.qpointz.mill.service.calcite.providers.CalcitePlanConverter;
import io.qpointz.mill.service.calcite.providers.PlanConverter;
import io.qpointz.mill.service.jdbc.providers.JdbcCalciteContextFactory;
import io.substrait.extension.ExtensionCollector;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

public class JdbcCalciteConfiguration {

    @Bean
    public CalciteContextFactory calciteContextFactory(CalciteServiceProperties properties,
                                                       JdbcConnectionConfiguration jdbcConfig,
                                                       @Value("${qp.mill.backend.jdbc.schema-name}") String schemaName) {
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

}
