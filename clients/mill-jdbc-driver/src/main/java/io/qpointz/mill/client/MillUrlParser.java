package io.qpointz.mill.client;

import lombok.val;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static io.qpointz.mill.client.MillClientConfiguration.*;
import static io.qpointz.mill.client.MillClientConfiguration.PORT_PROP;
import static io.qpointz.mill.client.MillUrlParser.KnownPropertyType.*;

public class MillUrlParser {

    protected enum KnownPropertyType {
        STRING,
        INT,
        FILE,
        PATH
    }

    public record KnownProperty(String key, KnownPropertyType type, Boolean mandatory, String description) {
        public static KnownProperty of(String key, KnownPropertyType type, Boolean mandatory, String description) {
            return new KnownProperty(key, type, mandatory, description);
        }
    }

    public static final String URL_PREFIX = "jdbc:mill:";

    public static final Set<KnownProperty> KNOWN_PROPERTIES = Set.of(
            KnownProperty.of(CLIENT_PROTOCOL_PROP, STRING, true, String.format("Client protocol valid values '%s', '%s','%s','%s'.",
                    CLIENT_PROTOCOL_GRPC_VALUE, CLIENT_PROTOCOL_HTTP_VALUE, CLIENT_PROTOCOL_HTTPS_VALUE, CLIENT_PROTOCOL_IN_PROC_VALUE)),
            KnownProperty.of(HOST_PROP, STRING, true, "Mill backend host name"),
            KnownProperty.of(PORT_PROP, INT, false, "Mill backend port"),
            KnownProperty.of(USERNAME_PROP, STRING, false, "Username to use for BASIC authentication"),
            KnownProperty.of(PASSWORD_PROP, STRING, false, "Password to use for BASIC authentication"),
            KnownProperty.of(BEARER_TOKEN_PROP, STRING, false, "Bearer token authentication token"),
            KnownProperty.of(TLS_KEY_CERT_CHAIN_PROP, STRING, false, "TLS path to certificate chain"),
            KnownProperty.of(TLS_KEY_PRIVATE_KEY_PROP, STRING, false, "TLS path to private key"),
            KnownProperty.of(TLS_KEY_PRIVATE_KEY_PASSWORD_PROP, STRING, false, "TLS private key password"),
            KnownProperty.of(TLS_TRUST_ROOT_CERT_PROP, STRING, false, "TLS custom root CA certificate path"),
            KnownProperty.of(FETCH_SIZE_PROP, INT, true, String.format("Record batch fetch size. Default %s", DEFAULT_FETCH_SIZE)),
            KnownProperty.of(API_PATH_PROP,STRING, false, String.format("API relative path. Default %s", DEFAULT_API_PATH))
    );

    public MillUrlParser(String url, Properties... props) {
        apply(url, props);
    }

    public static boolean acceptsURL(String url) {
        return url.startsWith(MillUrlParser.URL_PREFIX);
    }

    public static Properties apply(String url, Properties... overwrites) {
        val props = new ArrayList<Properties>();
        props.add(parseUrl(url));
        props.addAll(List.of(overwrites));
        return apply(props.toArray(new Properties[0]));
    }

    public static Properties apply(Properties... properties) {
        val effective = new Properties();
        for (var source : properties) {
            mergeInto(effective, source);
        }
        return effective;
    }

    /**
     * Merges connection properties without letting blank DBeaver/JDBC {@code user}, {@code password},
     * or {@code bearerToken} values wipe credentials parsed from {@code user:pass@host} in the URL.
     */
    private static void mergeInto(Properties target, Properties source) {
        for (var key : source.stringPropertyNames()) {
            var value = source.getProperty(key);
            if (isBlankCredentialValue(key, value) && target.containsKey(key) && !isBlank(target.getProperty(key))) {
                continue;
            }
            if (value == null) {
                target.remove(key);
            } else {
                target.setProperty(key, value);
            }
        }
    }

    private static boolean isBlankCredentialValue(String key, String value) {
        return isBlank(value)
                && (USERNAME_PROP.equals(key) || PASSWORD_PROP.equals(key) || BEARER_TOKEN_PROP.equals(key));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static Properties parseUrl(String url) {
        if (!acceptsURL(url)) {
            throw new IllegalArgumentException("Not supported URL format:" + url);
        }

        var cleanUrl = url.replace(URL_PREFIX, "");
        val effectiveProps = new Properties();
        var parsedUrl = URI.create(cleanUrl);

        var parsedScheme = parsedUrl.getScheme();
        if (parsedScheme == null || parsedScheme.trim().isEmpty()) {
            parsedScheme = MillClientConfiguration.CLIENT_PROTOCOL_GRPC_VALUE;
        }

        if (parsedScheme.equals(CLIENT_PROTOCOL_GRPC_VALUE) || parsedScheme.equals(CLIENT_PROTOCOL_IN_PROC_VALUE) ||
            parsedScheme.equals(CLIENT_PROTOCOL_HTTP_VALUE) || parsedScheme.equals(CLIENT_PROTOCOL_HTTPS_VALUE)) {
            effectiveProps.setProperty(CLIENT_PROTOCOL_PROP, parsedScheme);
        } else {
            throw new IllegalArgumentException("Not supported scheme:" + parsedScheme);
        }

        val path = parsedUrl.getPath();
        if (!(path==null || path.isEmpty())) {
            effectiveProps.setProperty(API_PATH_PROP, path);
        }

        var query = parsedUrl.getQuery();
        if (query != null) {
            final var params = query.split("&");
            for (var pv : params) {
                if (pv.contains("=")) {
                    final var kv = pv.split("=");
                    if (kv.length==2) {
                        effectiveProps.put(kv[0], kv[1]);
                        continue;
                    }
                    if (kv.length==1) {
                        effectiveProps.put(kv[0], "");
                        continue;
                    }

                    throw new IllegalArgumentException(String.format("Url part '%s' malformed", pv));
                } else {
                    effectiveProps.put(pv.toLowerCase(), "true");
                }
            }
        }

        if (parsedUrl.getHost() != null) {
            effectiveProps.put(HOST_PROP, parsedUrl.getHost());
        }

        putUserInfoCredentials(parsedUrl, effectiveProps);

        var port = parsedUrl.getPort();
        if (port > 0) {
            effectiveProps.put(PORT_PROP, Integer.toString(port));
        }
        return effectiveProps;
    }

    /**
     * Maps {@code user:password@host} from the URI authority into {@code user} / {@code password} properties
     * (only when not already set by query parameters).
     */
    private static void putUserInfoCredentials(URI parsedUrl, Properties effectiveProps) {
        var userInfo = parsedUrl.getUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return;
        }
        var colon = userInfo.indexOf(':');
        if (colon >= 0) {
            putIfAbsent(effectiveProps, USERNAME_PROP, decodeUrlComponent(userInfo.substring(0, colon)));
            putIfAbsent(effectiveProps, PASSWORD_PROP, decodeUrlComponent(userInfo.substring(colon + 1)));
        } else {
            putIfAbsent(effectiveProps, USERNAME_PROP, decodeUrlComponent(userInfo));
        }
    }

    private static void putIfAbsent(Properties props, String key, String value) {
        if (!props.containsKey(key)) {
            props.setProperty(key, value);
        }
    }

    private static String decodeUrlComponent(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

}
