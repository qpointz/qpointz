package io.qpointz.mill;

import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration;
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.data.backend.grpc.GrpcExceptionInterceptor;
import io.qpointz.mill.data.backend.grpc.GrpcServiceDescriptor;
import io.qpointz.mill.data.backend.grpc.MillGrpcService;
import io.qpointz.mill.data.backend.grpc.config.MillGrpcConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Starts an embedded Skymill gRPC server for JDBC driver integration tests.
 *
 * The client-side ITs default to connecting to {@code localhost:9090} over gRPC. Historically this assumed a
 * separately running service. This helper makes the {@code :clients:mill-jdbc-driver:testIT} suite self-contained.
 */
@Slf4j
public final class EmbeddedSkymillGrpcServer {
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static volatile ConfigurableApplicationContext context;
    private static volatile int port = -1;

    private EmbeddedSkymillGrpcServer() {
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            SqlAutoConfiguration.class,
            BackendAutoConfiguration.class,
            FlowBackendAutoConfiguration.class,
            DefaultServiceConfiguration.class,
            MillGrpcConfiguration.class,
            MillGrpcService.class,
            GrpcExceptionInterceptor.class,
            GrpcServiceDescriptor.class,
    })
    static class SkymillGrpcServerConfig {
    }

    public static void ensureStarted() {
        if (!shouldStartEmbedded()) {
            return;
        }
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }
        log.info("Starting embedded Skymill gRPC server for JDBC driver testIT");
        context = new SpringApplicationBuilder(SkymillGrpcServerConfig.class)
                .profiles("skymill")
                .web(WebApplicationType.NONE)
                .run();
        try {
            io.grpc.Server server = context.getBean(io.grpc.Server.class);
            port = server.getPort();
            log.info("Embedded Skymill gRPC server started on 127.0.0.1:{}", port);
        } catch (Exception e) {
            log.warn("Embedded Skymill gRPC server started but port could not be resolved", e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(EmbeddedSkymillGrpcServer::stop));
    }

    private static boolean shouldStartEmbedded() {
        String flag = System.getenv("MILL_IT_EMBEDDED");
        return flag == null || flag.isBlank() || "true".equalsIgnoreCase(flag) || "y".equalsIgnoreCase(flag);
    }

    public static boolean isStarted() {
        return STARTED.get();
    }

    public static int port() {
        return port;
    }

    @Nullable
    public static String host() {
        return "127.0.0.1";
    }

    private static void stop() {
        try {
            if (context != null) {
                log.info("Stopping embedded Skymill gRPC server");
                context.close();
            }
        } catch (Exception e) {
            log.warn("Failed to stop embedded Skymill gRPC server", e);
        }
    }
}

