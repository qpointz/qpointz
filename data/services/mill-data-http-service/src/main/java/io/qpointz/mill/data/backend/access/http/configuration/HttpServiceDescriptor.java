package io.qpointz.mill.data.backend.access.http.configuration;

import io.qpointz.mill.service.annotations.ConditionalOnService;
import io.qpointz.mill.service.configuration.ServiceAddressProperties;
import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;
import io.qpointz.mill.service.descriptors.ServiceDescriptor;
import lombok.Data;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConditionalOnService(value = "http", group = "data")
@EnableConfigurationProperties(HttpServiceProperties.class)
public class HttpServiceDescriptor implements ServiceDescriptor {

    @Getter
    private final ServiceAddressDescriptor external;

    @Getter
    private final String url;

    public HttpServiceDescriptor(
            HttpServiceProperties serviceProperties,
            @Autowired(required = false) ServiceAddressProperties externalHosts
    ) {
        val hostRef = serviceProperties.getExternalHost();
        this.external = externalHosts!=null && hostRef !=null && !hostRef.isBlank()
                ? externalHosts.getExternals().getOrDefault(hostRef, null)
                : null;

        this.url = this.external == null
                ? null
                : this.external.asUrl();

    }

    @Override
    public String getStereotype() {
        return "data-http";
    }
}
