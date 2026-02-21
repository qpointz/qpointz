package io.qpointz.mill.autoconfigure.data.backend;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;
import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_DEFAULT_BACKEND;

@Getter
@Setter
@ConfigurationProperties(prefix = MILL_DATA_BACKEND_CONFIG_KEY)
public class BackendProperties {

    /**
     * Data backend identifier that determines which execution backend to use.
     */
    private String type = MILL_DATA_DEFAULT_BACKEND;

}
