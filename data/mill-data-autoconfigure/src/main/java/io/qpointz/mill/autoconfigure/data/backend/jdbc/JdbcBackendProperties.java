package io.qpointz.mill.autoconfigure.data.backend.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

import java.util.Optional;

import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;

@Getter
@Setter
@ConfigurationProperties(prefix = MILL_DATA_BACKEND_CONFIG_KEY + ".jdbc")
public class JdbcBackendProperties {

    @Getter
    @Setter
    private String url;

    @Getter
    @Setter
    private String driver;

    @Getter
    @Setter
    private Optional<String> user = Optional.empty();

    @Getter
    @Setter
    private Optional<String> password = Optional.empty();

    @Getter
    @Setter
    @Name("target-schema")
    private Optional<String> targetSchema= Optional.empty();

    @Getter
    @Setter
    private Optional<String> schema= Optional.empty();

    @Getter
    @Setter
    private Optional<String> catalog= Optional.empty();

    @Getter
    @Setter
    private Boolean multiSchema= false;
}
