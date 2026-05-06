package io.qpointz.mill.export;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Bindings for {@code mill.data.services.export.*} (parity with {@code mill.data.services.http}).
 */
@Getter
@Setter
@ConditionalOnService(value = "export", group = "data")
@ConfigurationProperties(prefix = "mill.data.services.export")
public class ExportServiceProperties {

    private Boolean enable;

    /** Logical key into {@code mill.application.hosts.externals} (same pattern as HTTP data-plane). */
    private String externalHost = "";

    /**
     * When empty or contains {@code *}, all SPI formats are exposed on HTTP.
     */
    private List<String> formats = new ArrayList<>();
}
