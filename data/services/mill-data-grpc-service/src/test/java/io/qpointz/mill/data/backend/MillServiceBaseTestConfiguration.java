package io.qpointz.mill.data.backend;

import io.qpointz.mill.proto.DataConnectServiceGrpc;
import lombok.val;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ImportAutoConfiguration({
        GrpcAdviceAutoConfiguration.class,
        GrpcClientAutoConfiguration.class,
})
public class MillServiceBaseTestConfiguration {

    @GrpcClient("test")
    public DataConnectServiceGrpc.DataConnectServiceBlockingStub blockingStub;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DataConnectServiceGrpc.DataConnectServiceBlockingStub blockingStub() {
        return blockingStub;
    }

    @Bean
    public SqlProvider sqlProvider() {
        val m = mock(SqlProvider.class);
        when(m.supportsSql()).thenReturn(Boolean.TRUE);
        return m;
    }

    @Bean
    public ExecutionProvider executionProvider() {
        return mock(ExecutionProvider.class);
    }

    @Bean
    public SchemaProvider metadataProvider() {
        return mock(SchemaProvider.class);
    }

}
