package io.qpointz.mill.autoconfigure.data.backend;

import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.services.calcite.CalciteSqlDialectConventions;
import io.qpointz.mill.sql.dialect.SqlDialectSpec;
import io.substrait.extension.SimpleExtension;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@AutoConfiguration(after = SqlAutoConfiguration.class)
@EnableConfigurationProperties(BackendProperties.class)
public class BackendAutoConfiguration {

    public static final String MILL_DATA_DEFAULT_BACKEND = "calcite";
    public static final String MILL_DATA_BACKEND_CONFIG_KEY = "mill.data.backend";

    @Bean
    public SimpleExtension.ExtensionCollection substraitExtensionCollection() throws IOException {
        return SimpleExtension.loadDefaults();
    }

    @Bean
    public CalciteSqlDialectConventions sqlDialectConventions(SqlDialectSpec dialectSpec) {
        return new CalciteSqlDialectConventions(dialectSpec);
    }

}
