package io.qpointz.mill.autoconfigure.data;

import io.qpointz.mill.sql.dialect.SqlDialectSpec;
import io.qpointz.mill.sql.dialect.SqlDialectSpecs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@EnableConfigurationProperties(SqlProperties.class)
@Slf4j
public class SqlAutoConfiguration {

    public static final String MILL_DATA_SQL_CONFIG_KEY = "mill.data.sql";
    public static final SqlDialectSpec MILL_DATA_DEFAULT_DIALECT = SqlDialectSpecs.CALCITE;
    private final SqlProperties millDataProperties;

    public SqlAutoConfiguration(SqlProperties millDataProperties) {
        this.millDataProperties = millDataProperties;
    }

    @Bean
    public SqlDialectSpec millDataSqlDialectSpec() {
        return SqlDialectSpecs.byId(millDataProperties.getDialect().toUpperCase());
    }
}
