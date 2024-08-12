package io.qpointz.mill.service.jdbc.configuration;

import io.qpointz.mill.service.jdbc.providers.JdbcContext;
import io.qpointz.mill.service.jdbc.providers.JdbcContextFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="qp.mill.backend.jdbc.connection")
public class JdbcConnectionConfiguration {

    @Getter
    @Setter
    private String url;

    @Getter
    @Setter
    private String driver;

    @Getter
    @Setter
    private String dialect;

    @Getter
    @Setter
    private Optional<String> user = Optional.empty();

    @Getter
    @Setter
    private Optional<String> password = Optional.empty();

    @Getter
    @Setter
    private Optional<String> schema= Optional.empty();

    @Getter
    @Setter
    private Optional<String> catalog= Optional.empty();


    public static JdbcContextFactory jdbcContext(JdbcConnectionConfiguration configuration) {
        return () -> new JdbcContext() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName(configuration.getDriver());
                    return DriverManager.getConnection(configuration.url,
                            configuration.user.orElse(""),
                            configuration.password.orElse(""));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
