package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

@Slf4j
public record TestITProfile(
        String host,
        int port,
        Protocol protocol,
        boolean tls,
        Authentication auth,
        String basePath,
        String username,
        String password,
        String token,
        String tlsCa,
        String tlsCert,
        String tlsKey,
        String schemaName
) {

    public enum Protocol {
        HTTP,
        GRPC
    }

    public enum Authentication {
        NO_AUTH,
        BASIC,
        BEARER,
    }


    private static Stream<Arguments> profileArgs() {
        return profiles().stream()
                .map(Arguments::of);
    }

    public String jdbcProtocol() {
        return switch (this.protocol) {
            case GRPC -> this.tls ? "grpcs" : "grpc";
            case HTTP -> this.tls ? "https" : "http";
        };
    }

    public String jdbcUrl() {
        val path = this.protocol == Protocol.HTTP ? sanitizeHttpPath(this.basePath) : "";
        return String.format("jdbc:mill:%s://%s:%s%s", jdbcProtocol(), this.host, this.port, path);
    }

    public Properties connectionProperties() {
        val properties = new Properties();
        if (this.auth == Authentication.BASIC) {
            properties.setProperty("user", this.username);
            properties.setProperty("password", this.password);
        } else if (this.auth == Authentication.BEARER) {
            properties.setProperty("bearerToken", this.token);
        }

        if (!isBlank(this.tlsCert)) {
            properties.setProperty("tlsKeyCertChain", this.tlsCert);
        }
        if (!isBlank(this.tlsKey)) {
            properties.setProperty("tlsKeyPrivateKey", this.tlsKey);
        }
        if (!isBlank(this.tlsCa)) {
            properties.setProperty("tlsTrustRootCert", this.tlsCa);
        }
        return properties;
    }

    public String maskedConnectionProperties() {
        val props = connectionProperties();
        val masked = new Properties();
        for (String name : props.stringPropertyNames()) {
            String value = props.getProperty(name);
            if ("password".equals(name) || "bearerToken".equals(name)) {
                masked.setProperty(name, isBlank(value) ? "<empty>" : "<set>");
            } else {
                masked.setProperty(name, value);
            }
        }
        return masked.toString();
    }

    public String debugSummary() {
        return String.format(
                "host=%s, port=%s, protocol=%s, tls=%s, auth=%s, basePath=%s, schema=%s, tlsCa=%s, tlsCert=%s, tlsKey=%s",
                this.host,
                this.port,
                jdbcProtocol(),
                this.tls,
                this.auth,
                this.protocol == Protocol.HTTP ? sanitizeHttpPath(this.basePath) : "<n/a>",
                this.schemaName,
                this.tlsCa != null ? "<set>" : "<empty>",
                this.tlsCert != null ? "<set>" : "<empty>",
                this.tlsKey != null ? "<set>" : "<empty>"
        );
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("Jet(%s,%s%s)  %s:%s schema=%s",
                this.protocol,
                this.auth.name(),
                this.tls ? ",TLS" : "",
                this.host,
                this.port,
                this.schemaName);
    }

    public static List<TestITProfile> profiles()  {
        val profile = profileFromEnv();
        log.info("Using integration profile from MILL_IT_* environment variables/defaults");
        log.info("Integration profile: {}", profile.debugSummary());
        return List.of(profile);
    }

    private static TestITProfile profileFromEnv() {
        val protocolRaw = envOr("MILL_IT_PROTOCOL", "grpc").toLowerCase(Locale.ROOT);
        val protocol = parseProtocol(protocolRaw);
        val tls = parseBoolean(envOr("MILL_IT_TLS", "false"));
        val host = envOr("MILL_IT_HOST", "localhost");
        val port = parseInt(envOr("MILL_IT_PORT", ""), defaultPort(protocol, tls));
        val auth = parseAuth(envOr("MILL_IT_AUTH", "none"));
        val basePath = envOr("MILL_IT_BASE_PATH", "/services/jet");

        return new TestITProfile(
                host,
                port,
                protocol,
                tls,
                auth,
                basePath,
                envOr("MILL_IT_USERNAME", "reader"),
                envOr("MILL_IT_PASSWORD", "reader"),
                envOr("MILL_IT_TOKEN", ""),
                emptyToNull(envOr("MILL_IT_TLS_CA", "")),
                emptyToNull(envOr("MILL_IT_TLS_CERT", "")),
                emptyToNull(envOr("MILL_IT_TLS_KEY", "")),
                envOr("MILL_IT_SCHEMA", "skymill")
        );
    }

    private static Protocol parseProtocol(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "grpc" -> Protocol.GRPC;
            case "http", "http-json", "http-protobuf" -> Protocol.HTTP;
            default -> throw new IllegalArgumentException("Unsupported MILL_IT_PROTOCOL: " + value);
        };
    }

    private static Authentication parseAuth(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "none", "no_auth", "no-auth" -> Authentication.NO_AUTH;
            case "basic" -> Authentication.BASIC;
            case "bearer" -> Authentication.BEARER;
            default -> throw new IllegalArgumentException("Unsupported MILL_IT_AUTH: " + value);
        };
    }

    private static boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value);
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private static int defaultPort(Protocol protocol, boolean tls) {
        if (protocol == Protocol.GRPC) {
            return tls ? 443 : 9090;
        }
        return tls ? 443 : 8501;
    }

    private static String sanitizeHttpPath(String path) {
        if (path == null || path.isBlank()) {
            return "/services/jet";
        }
        val withLeadingSlash = path.startsWith("/") ? path : "/" + path;
        return withLeadingSlash.endsWith("/") ? withLeadingSlash : withLeadingSlash + "/";
    }

    private static String envOr(String key, String defaultValue) {
        val value = System.getenv(key);
        return value == null ? defaultValue : value.trim();
    }

    private static String emptyToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
