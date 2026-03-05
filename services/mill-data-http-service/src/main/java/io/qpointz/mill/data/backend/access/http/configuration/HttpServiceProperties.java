package io.qpointz.mill.data.backend.access.http.configuration;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import jdk.jfr.Name;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConditionalOnService(value = "http", group = "data")
@ConfigurationProperties(prefix = "mill.data.services.http")
public class HttpServiceProperties {

    private Boolean enable;

    @Name("external-host")
    private String externalHost;


}
