package io.qpointz.mill.service.configuration;

import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;
import io.qpointz.mill.service.descriptors.ServiceAddressScheme;
import io.qpointz.mill.service.providers.ServiceAddressPlaceholders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;
import java.util.Optional;

/**
 * Resolves {@link ServiceAddressProperties.ServiceEndpointDescriptor} entries, substituting
 * {@link ServiceAddressPlaceholders} from the active servlet request when present.
 */
@Component
public class ServiceAddressPlaceholderResolver {

    /**
     * @param raw configured endpoint; may contain {@code @request.*} placeholders
     * @return resolved descriptor, or empty when request placeholders are used without an active request
     */
    public Optional<ServiceAddressDescriptor> resolve(ServiceAddressProperties.ServiceEndpointDescriptor raw) {
        if (raw == null) {
            return Optional.empty();
        }

        boolean needsRequest = requiresRequestContext(raw);
        HttpServletRequest request = null;
        if (needsRequest) {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servletAttrs)) {
                return Optional.empty();
            }
            request = servletAttrs.getRequest();
        }

        String schemeRaw = resolveField(raw.scheme(), request);
        String hostRaw = resolveField(raw.host(), request);
        String portRaw = resolveField(raw.port(), request);

        if (schemeRaw == null || hostRaw == null || portRaw == null) {
            return Optional.empty();
        }

        ServiceAddressScheme scheme = parseScheme(schemeRaw.strip());
        Integer port = parsePort(portRaw.strip());
        if (scheme == null || port == null) {
            return Optional.empty();
        }

        String host = hostRaw.strip();
        if (host.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ServiceAddressDescriptor(scheme, host, port));
    }

    private static boolean requiresRequestContext(ServiceAddressProperties.ServiceEndpointDescriptor raw) {
        return ServiceAddressPlaceholders.isRequestPlaceholder(raw.scheme())
                || ServiceAddressPlaceholders.isRequestPlaceholder(raw.host())
                || ServiceAddressPlaceholders.isRequestPlaceholder(raw.port());
    }

    private static String resolveField(String configured, HttpServletRequest request) {
        if (configured == null) {
            return null;
        }
        String trimmed = configured.strip();
        if (!ServiceAddressPlaceholders.isRequestPlaceholder(trimmed)) {
            return trimmed;
        }
        if (request == null) {
            return null;
        }
        String field = trimmed.substring(ServiceAddressPlaceholders.REQUEST_PREFIX.length()).toLowerCase(Locale.ROOT);
        return switch (field) {
            case "scheme" -> ServletRequestAddressSupport.scheme(request);
            case "host" -> ServletRequestAddressSupport.host(request);
            case "port" -> String.valueOf(ServletRequestAddressSupport.port(request));
            default -> null;
        };
    }

    private static ServiceAddressScheme parseScheme(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "http" -> ServiceAddressScheme.HTTP;
            case "https" -> ServiceAddressScheme.HTTPS;
            case "grpc" -> ServiceAddressScheme.GRPC;
            default -> null;
        };
    }

    private static Integer parsePort(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
