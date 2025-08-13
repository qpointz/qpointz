package io.qpointz.rapids.jdbc;

import lombok.Builder;
import lombok.Getter;

import java.net.URI;
import java.util.Properties;
import java.util.regex.Pattern;

@Builder
public class RapidsConnectionConfig {

    public static final String URL_PREFIX = "jdbc:rapids:";

    @Getter
    private String host;

    @Getter
    private int port;

    @Getter
    private String protocol;

    private static RapidsConnectionConfig from(Properties props) {
        return RapidsConnectionConfig.builder()
                .host(props.getOrDefault("host", "").toString())
                .port((int)props.getOrDefault("port", 0))
                .protocol((String)props.getOrDefault("protocol", "grpc"))
                .build();
    }
    public static RapidsConnectionConfig from(String url) {
        return from(url, new Properties());
    }

    public static RapidsConnectionConfig from(String url, Properties info) {
        if (!url.startsWith(URL_PREFIX)) {
            throw new IllegalArgumentException("Not supported URL format:" + url);
        }
        var cleanUrl = url.replace(URL_PREFIX, "");
        var effectiveProps = new Properties();
        var parsedUrl = URI.create(cleanUrl) ;

        var query = parsedUrl.getQuery();
        if (query!=null) {
            final var params = query.split("&");
            for (var pv : params) {
                if (pv.contains("=")) {
                    final var kv = pv.split("=");
                    effectiveProps.put(kv[0].toLowerCase(), kv[1]);
                } else {
                    effectiveProps.put(pv.toLowerCase(), "true");
                }
            }
        }

        if (parsedUrl.getHost()!=null) {
            effectiveProps.put("host", parsedUrl.getHost());
        }

        effectiveProps.put("port", parsedUrl.getPort());

        if (parsedUrl.getScheme()!=null) {
            effectiveProps.put("protocol", parsedUrl.getScheme());
        }

        if (parsedUrl.getPath()!=null) {
            effectiveProps.put("scheme", parsedUrl.getPath());
        }

        if (info!=null) {
            for (var k : info.stringPropertyNames()) {
                effectiveProps.put(k.toLowerCase(), info.get(k));
            }
        }

        return RapidsConnectionConfig.from(effectiveProps);
    }

}
