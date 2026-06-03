package io.qpointz.mill.service.providers;

/**
 * Placeholder tokens for {@code mill.application.hosts.externals} field values.
 *
 * <p>Whole-field substitution only (e.g. {@code host: "@request.host"}). Distinct from Spring
 * {@code ${property}} resolution at config load time.
 */
public final class ServiceAddressPlaceholders {

    /** Prefix for values taken from the active servlet request ({@code @request.scheme}, etc.). */
    public static final String REQUEST_PREFIX = "@request.";

    public static final String REQUEST_SCHEME = REQUEST_PREFIX + "scheme";
    public static final String REQUEST_HOST = REQUEST_PREFIX + "host";
    public static final String REQUEST_PORT = REQUEST_PREFIX + "port";

    private ServiceAddressPlaceholders() {
    }

    /**
     * @param value configured field value
     * @return {@code true} if {@code value} is a request placeholder (case-insensitive)
     */
    public static boolean isRequestPlaceholder(String value) {
        return value != null && value.strip().toLowerCase().startsWith(REQUEST_PREFIX);
    }
}
