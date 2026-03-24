package io.qpointz.mill.data.backend.grpc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bindings for {@code mill.data.services.grpc.*} used by the native gRPC server.
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
