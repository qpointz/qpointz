package io.qpointz.mill.service.calcite.configuration;

import io.qpointz.mill.service.calcite.CalciteContextFactory;
import io.qpointz.mill.service.calcite.ConnectionContextFactory;
import io.qpointz.mill.service.calcite.providers.CalcitePlanConverter;
import io.qpointz.mill.service.calcite.providers.PlanConverter;
import io.substrait.extension.ExtensionCollector;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

public class CalciteServiceConfiguration {

    @Bean
    public CalciteContextFactory calciteConextFactory(CalciteServiceProperties properties) {
        val props = new Properties();
        props.putAll(properties.getConnection());
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

}
