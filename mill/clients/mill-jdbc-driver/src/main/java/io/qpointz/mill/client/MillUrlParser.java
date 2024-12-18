package io.qpointz.mill.client;

import lombok.val;

import java.net.URI;
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
        for (int i=0;i<properties.length;i++) {
            effective.putAll(properties[i]);
        }
        return effective;
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

        effectiveProps.put(PORT_PROP, Integer.toString(parsedUrl.getPort()));
        return effectiveProps;
    }

}
