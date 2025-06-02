package io.qpointz.mill.services.jdbc.configuration;


import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.MetadataProvider;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.providers.CalciteMetadataProvider;
import io.qpointz.mill.services.calcite.providers.CalcitePlanConverter;
import io.qpointz.mill.services.calcite.providers.CalciteSqlProvider;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.services.jdbc.providers.JdbcCalciteContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcContext;
import io.qpointz.mill.services.jdbc.providers.JdbcContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcExecutionProvider;
import io.substrait.extension.ExtensionCollector;
import io.substrait.extension.SimpleExtension;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "mill.backend")
@ConditionalOnProperty(prefix = "mill.backend", name="provider", havingValue = "jdbc")
public class JdbcCalciteConfiguration {

    @Getter
    @Value("${mill.backend.provider}")
    private String  providerName;

    @Getter
    @Setter
    private Map<String, String> connection;

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.url}")
    private String url;

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.driver}")
    private String driver;

    @Getter
    @Setter
    private String dialect;

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.user:#{null}}")
    private Optional<String> user = Optional.empty();

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.password:#{null}}")
    private Optional<String> password = Optional.empty();

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.target-schema:ts}")
    private Optional<String> targetSchema= Optional.empty();

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.schema:#{null}}")
    private Optional<String> schema= Optional.empty();

    @Getter
    @Setter
    @Value("${mill.backend.jdbc.catalog:#{null}}")
    private Optional<String> catalog= Optional.empty();



    @Bean
    public JdbcContextFactory jdbcContextFactory() {
        return this.jdbcContext();
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
    public static SqlProvider sqlParserProvider(CalciteContextFactory ctxFactory, SubstraitDispatcher substraitDispatcher) {
        return new CalciteSqlProvider(ctxFactory, substraitDispatcher);
    }

    @Bean
    public CalciteContextFactory calciteContextFactory() {
        val props = new Properties();
        props.putAll(connection);
        return new JdbcCalciteContextFactory(props, this, this.targetSchema);
    }

    @Bean
    public PlanConverter planConverter(CalciteContextFactory calciteConextFactory, SimpleExtension.ExtensionCollection extensionCollection) {
        return new CalcitePlanConverter(calciteConextFactory, SqlDialect.DatabaseProduct.CALCITE.getDialect(), extensionCollection);
    }

    @Bean
    public ExtensionCollector extensionCollector() {
        return new ExtensionCollector();
    }

    public JdbcContextFactory jdbcContext() {
        return () -> new JdbcContext() {
            private JdbcCalciteConfiguration config = JdbcCalciteConfiguration.this;
            @Override
            public Connection getConnection() {
                try {
                    Class.forName(this.config.getDriver());
                    return DriverManager.getConnection(this.config.getUrl(),
                            this.config.getUser().orElse(""),
                            this.config.getPassword().orElse(""));
                } catch (SQLException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
