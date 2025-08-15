package io.qpointz.mill.ai.nlsql.configuration;

import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.ai.nlsql.models.SqlDialects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Nl2SqlConfiguration {

    @Bean
    public SqlDialect nl2SqlSqlDialect(@Value("${mill.ai.nl2sql.dialect:#{null}}") String dialectName) {
        return SqlDialects
                .byName(dialectName);
    }

}
