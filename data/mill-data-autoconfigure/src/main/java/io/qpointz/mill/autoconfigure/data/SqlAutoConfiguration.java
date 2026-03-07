package io.qpointz.mill.autoconfigure.data;

import io.qpointz.mill.sql.v2.dialect.DialectRegistry;
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@EnableConfigurationProperties(SqlProperties.class)
@Slf4j
public class SqlAutoConfiguration {

    public static final String MILL_DATA_SQL_CONFIG_KEY = "mill.data.sql";
    public static final String MILL_DATA_DEFAULT_DIALECT = "CALCITE";
    private final SqlProperties millDataProperties;

    public SqlAutoConfiguration(SqlProperties millDataProperties) {
        this.millDataProperties = millDataProperties;
    }

    @Bean
    public SqlDialectSpec millDataSqlDialectSpec() {
        final DialectRegistry registry = DialectRegistry.fromClasspathDefaults();
        final String dialectId = millDataProperties.getDialect().toUpperCase();
        final SqlDialectSpec spec = registry.getDialect(dialectId);
        if (spec == null) {
            throw new IllegalStateException(
                    "Unknown configured dialect for v2 runtime: " + dialectId + ". Supported: " + String.join(",", registry.ids()));
        }
        return spec;
    }
}
