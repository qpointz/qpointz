package io.qpointz.mill.data.backend.grpc.config;

import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.data.backend.grpc.GrpcExceptionInterceptor;
import io.qpointz.mill.data.backend.grpc.GrpcSecurityInterceptor;
import io.qpointz.mill.data.backend.grpc.GrpcServerLifecycle;
import io.qpointz.mill.data.backend.grpc.MillGrpcService;
import io.qpointz.mill.security.SecurityContextSecurityProvider;
import io.qpointz.mill.security.SecurityProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.SmartLifecycle;

import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Wires the grpc-java Netty server, interceptors, and lifecycle for the data gRPC transport.
 */
@Configuration
@ConditionalOnService(value = "grpc", group = "data")
@EnableConfigurationProperties(GrpcServerProperties.class)
public class MillGrpcConfiguration {

    /**
     * Exposes the same {@link SecurityProvider} contract as the v1 gRPC module for {@link io.qpointz.mill.data.backend.ServiceHandler}.
     *
     * @return security provider backed by the Spring {@link org.springframework.security.core.context.SecurityContextHolder}
     */
    @Bean
    @ConditionalOnMissingBean(SecurityProvider.class)
    public SecurityProvider securityProvider() {
        return new SecurityContextSecurityProvider();
    }

    /**
     * Native gRPC {@link Server} (not started until {@link GrpcServerLifecycle} runs).
     *
     * @param service              data-plane service implementation
     * @param properties           bind address and port
     * @param securityInterceptor  present when Mill security is enabled
     * @param exceptionInterceptor maps exceptions to gRPC status (always registered)
     * @return configured server instance
     */
    @Bean
    public Server grpcServer(
            MillGrpcService service,
            GrpcServerProperties properties,
            ObjectProvider<GrpcSecurityInterceptor> securityInterceptor,
            GrpcExceptionInterceptor exceptionInterceptor
    ) {
        List<ServerInterceptor> chain = new ArrayList<>();
        chain.add(exceptionInterceptor);
        securityInterceptor.ifAvailable(chain::add);
        ServerServiceDefinition definition = ServerInterceptors.intercept(
                service,
                chain.toArray(ServerInterceptor[]::new)
        );
        if (StringUtils.hasText(properties.getInProcessName())) {
            return InProcessServerBuilder.forName(properties.getInProcessName())
                    .addService(definition)
                    .build();
        }
        InetSocketAddress address = new InetSocketAddress(properties.getHost(), properties.getPort());
        return NettyServerBuilder.forAddress(address)
                .addService(definition)
                .build();
    }

    /**
     * Starts and stops the gRPC server with the Spring application context.
     *
     * @param server     grpc-java server bean
     * @param properties shutdown grace configuration
     * @return Spring lifecycle adapter
     */
    @Bean
    public SmartLifecycle grpcServerLifecycle(Server server, GrpcServerProperties properties) {
        return new GrpcServerLifecycle(server, properties);
    }
}
