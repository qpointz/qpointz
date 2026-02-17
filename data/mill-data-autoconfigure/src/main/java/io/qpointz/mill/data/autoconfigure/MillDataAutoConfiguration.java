package io.qpointz.mill.data.autoconfigure;

import io.qpointz.mill.sql.dialect.SqlDialectSpec;
import io.qpointz.mill.sql.dialect.SqlDialectSpecs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MillDataProperties.class)
public class MillDataAutoConfiguration {

    private final MillDataProperties millDataProperties;

    public MillDataAutoConfiguration(MillDataProperties millDataProperties) {
        this.millDataProperties = millDataProperties;
    }

    @Bean
    public SqlDialectSpec millDataSqlDialectSpec() {
        return SqlDialectSpecs.byId(millDataProperties.getSqlDialect().toUpperCase());
    }

}
