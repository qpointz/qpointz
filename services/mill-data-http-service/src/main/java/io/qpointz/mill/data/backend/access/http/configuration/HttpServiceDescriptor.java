package io.qpointz.mill.data.backend.access.http.configuration;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;
import io.qpointz.mill.service.descriptors.ServiceDescriptor;
import io.qpointz.mill.service.providers.ExternalHostsProvider;
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
            @Autowired(required = false) ExternalHostsProvider externalHosts
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
