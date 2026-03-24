package io.qpointz.mill.data.backend.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.qpointz.mill.data.backend.ExecutionProvider;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.SqlProvider;
import io.qpointz.mill.proto.DataConnectServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test doubles for {@link io.qpointz.mill.data.backend.ServiceHandler} dependencies and a lazily
 * created blocking client stub (after the Netty server has been started).
 */
@Configuration
public class MillGrpcTestSupportConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SqlProvider sqlProvider() {
        SqlProvider m = mock(SqlProvider.class);
        when(m.supportsSql()).thenReturn(Boolean.TRUE);
        return m;
    }

    @Bean
    public ExecutionProvider executionProvider() {
        return mock(ExecutionProvider.class);
    }

    @Bean
    public SchemaProvider schemaProvider() {
        return mock(SchemaProvider.class);
    }

    @Bean
    @Lazy
    public DataConnectServiceGrpc.DataConnectServiceBlockingStub blockingStub(Server grpcServer) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", grpcServer.getPort())
                .usePlaintext()
                .build();
        return DataConnectServiceGrpc.newBlockingStub(channel);
    }
}
