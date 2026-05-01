package io.qpointz.mill.data.backend.grpc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bindings for {@code mill.data.services.grpc.*} for the Netty gRPC server and related discovery fields.
 *
 * <p>Properties {@link #host} and {@link #port} control the listen socket; {@link #externalHost} is optional
 * metadata for clients (resolved via {@code mill.application.hosts.externals} when a provider is present)
 * and does not change the bind address.
 */
@ConfigurationProperties(prefix = "mill.data.services.grpc")
public class GrpcServerProperties {

    /**
     * TCP port the Netty gRPC server listens on. Use {@code 0} to choose a free ephemeral port (e.g. tests).
     * Ignored when {@link #inProcessName} is set.
     */
    private int port = 9090;

    /**
     * Bind address (hostname or IP). Defaults to all interfaces.
     * Ignored when {@link #inProcessName} is set.
     */
    private String host = "0.0.0.0";

    /**
     * Hostname or logical name for discovery only when the service is fronted by a proxy, mesh, or DNS alias
     * ({@code mill.data.services.grpc.external-host} in YAML). Does not affect Netty bind address ({@link #host}),
     * listen {@link #port}, or {@link io.qpointz.mill.data.backend.grpc.GrpcServerLifecycle}; when blank, discovery
     * may omit an external host block.
     */
    private String externalHost = "";

    /**
     * When non-blank, the server uses an in-process transport with this name instead of TCP
     * ({@link #host} / {@link #port}). Used for embedded tests and clients that connect via
     * {@code jdbc:mill:mem://} URLs with the same name.
     */
    private String inProcessName = "";

    /**
     * Grace period when shutting down the server during Spring context stop.
     */
    private int shutdownGraceSeconds = 5;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getExternalHost() {
        return externalHost;
    }

    public void setExternalHost(String externalHost) {
        this.externalHost = externalHost != null ? externalHost : "";
    }

    public String getInProcessName() {
        return inProcessName;
    }

    public void setInProcessName(String inProcessName) {
        this.inProcessName = inProcessName != null ? inProcessName : "";
    }

    public int getShutdownGraceSeconds() {
        return shutdownGraceSeconds;
    }

    public void setShutdownGraceSeconds(int shutdownGraceSeconds) {
        this.shutdownGraceSeconds = shutdownGraceSeconds;
    }
}
