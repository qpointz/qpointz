package io.qpointz.mill.export;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;
import io.qpointz.mill.service.providers.ExternalHostsProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Resolves the public base URL for absolute export links (catalog), mirroring HTTP data-plane discovery.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnService(value = "export", group = "data")
public class ExportBaseUrlResolver {

    private final ExportServiceProperties properties;

    @Autowired(required = false)
    private ExternalHostsProvider externalHostsProvider;

    /**
     * @param request current servlet request (fallback when externals are not configured)
     * @return origin without trailing slash (e.g. {@code https://host:8443})
     */
    public String origin(HttpServletRequest request) {
        String hostRef = properties.getExternalHost();
        if (externalHostsProvider != null && hostRef != null && !hostRef.isBlank()) {
            ServiceAddressDescriptor ext = externalHostsProvider.getExternals().get(hostRef);
            if (ext != null) {
                return ext.asUrl();
            }
        }
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String portPart = "";
        if (("http".equalsIgnoreCase(scheme) && port != 80)
                || ("https".equalsIgnoreCase(scheme) && port != 443)) {
            portPart = ":" + port;
        }
        return scheme + "://" + host + portPart;
    }
}
