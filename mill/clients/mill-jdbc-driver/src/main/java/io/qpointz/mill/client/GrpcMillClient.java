package io.qpointz.mill.client;

import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.qpointz.mill.proto.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.val;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.qpointz.mill.client.MillClientConfiguration.CLIENT_PROTOCOL_GRPC_VALUE;
import static io.qpointz.mill.client.MillClientConfiguration.CLIENT_PROTOCOL_IN_PROC_VALUE;

@Log
public class GrpcMillClient extends MillClient implements AutoCloseable {


    @Getter
    private final MillClientConfiguration configuration;

    public GrpcMillClient(MillClientConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public HandshakeResponse handshake(HandshakeRequest request) {
        return this.blockingStub()
                .handshake(request);
    }

    @Override
    public ListSchemasResponse listSchemas(ListSchemasRequest request) {
        return this.blockingStub()
                .listSchemas(request);
    }

    @Override
    public GetSchemaResponse getSchema(GetSchemaRequest request) {
        return this.blockingStub()
                .getSchema(request);
    }

    @Override
    public Iterator<QueryResultResponse> execQuery(QueryRequest request) {
        return this.blockingStub()
                .execQuery(request);
    }

    @Override
    public void close() throws Exception {
    }

    private MillServiceGrpc.MillServiceBlockingStub blockingStub() {
        val channel = this.createChannel();
        return MillServiceGrpc.newBlockingStub(channel);
    }

    private static final ConcurrentMap<MillClientConfiguration, ManagedChannel> channels = new ConcurrentHashMap<>();

    private ManagedChannel createChannel() {
        return channels.computeIfAbsent(this.getConfiguration(), GrpcMillClient::createNewChannel);
    }

    @SneakyThrows
    private static ManagedChannel createNewChannel(MillClientConfiguration config)  {

        if (CLIENT_PROTOCOL_IN_PROC_VALUE.equals(config.getProtocol())) {
            log.warning("InProcess channel choosen. All configuration parameters will be ignored");
            return InProcessChannelBuilder.forName(config.getHost()).build();
        }

        val channelCredentials = createChannelCredentials(config);
        val callCredentials = createCallCredentials(config);

        ChannelCredentials finalCreds;

        if (callCredentials==null || callCredentials.isEmpty()) {
            finalCreds = channelCredentials;
        } else {
            val allCreds = new ChannelCredentials[callCredentials.size()];
            var idx =0;
            for (val callCreds : callCredentials) {
                val creds = CompositeChannelCredentials.create(channelCredentials, callCreds);
                allCreds[idx++] = creds;
            }
            finalCreds = ChoiceChannelCredentials.create(allCreds);
        }

        if (CLIENT_PROTOCOL_GRPC_VALUE.equals(config.getProtocol())) {
            val channelBuilder = Grpc.newChannelBuilderForAddress(config.getHost(), config.getPort(), finalCreds);
            return channelBuilder.build();
        }

        throw new IllegalArgumentException(String.format("'%s' client channels not supported. Suported: '%s','%s' ",
                config.getProtocol(),
                CLIENT_PROTOCOL_GRPC_VALUE, CLIENT_PROTOCOL_IN_PROC_VALUE));
    }

    private static Collection<CallCredentials> createCallCredentials(MillClientConfiguration config) {
        val callCreds = new ArrayList<CallCredentials>();

        val bearerCreds = createBearerTokenCreds(config);
        if (bearerCreds!=null) {
            callCreds.add(bearerCreds);
        }

        val basicCreds = createBasicCreds(config);
        if (basicCreds!=null) {
            callCreds.add(basicCreds);
        }

        return callCreds;
    }

    private static CallCredentials createBearerTokenCreds(MillClientConfiguration config) {
        if (!config.requiresBearerAuth()) {
            return null;
        }

        if (config.getBearerToken()==null || config.getBearerToken().trim().isEmpty()) {
            throw new IllegalArgumentException("'token' should not be empty");
        }

        return MillClientCallCredentials.bearerTokenCredentials(config.getBearerToken());

    }

    private static CallCredentials createBasicCreds(MillClientConfiguration config) {
        if (!config.requiresBasicAuth()) {
            return null;
        }

        if (config.getUsername()==null || config.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("'user' should not be empty");
        }

        if (config.getPassword()==null || config.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("'password' should not be empty");
        }

        return MillClientCallCredentials.basicCredentials(config.getUsername(), config.getPassword());
    }

    private static ChannelCredentials createChannelCredentials(MillClientConfiguration config) throws IOException {
        if (!config.requiresTls()) {
            log.warning(String.format("Insercure channel used for host %s:%s", config.getHost(), config.getPort()));
            return InsecureChannelCredentials.create();
        }

        val tlsKeyCertChain = config.getTlsKeyCertChain();
        if (tlsKeyCertChain == null || tlsKeyCertChain.trim().isEmpty()) {
            throw new SSLException("SSL Certificate chain is empty");
        }

        val tlsKeyPrivateKey = config.getTlsKeyPrivateKey();
        if (tlsKeyPrivateKey == null || tlsKeyPrivateKey.trim().isEmpty()) {
            throw new SSLException("SSL Private Key  is empty");
        }

        val builder =  TlsChannelCredentials.newBuilder();
        log.info(String.format("Use TLS for host %s:%s", config.getHost(), config.getPort()));

        if (config.getTlsKeyPrivateKeyPassword() == null || config.getTlsKeyPrivateKeyPassword().trim().isEmpty()) {
            builder.keyManager(new File(tlsKeyCertChain), new File(tlsKeyPrivateKey));
        } else {
            builder.keyManager(new File(tlsKeyCertChain), new File(tlsKeyPrivateKey), config.getTlsKeyPrivateKeyPassword());
        }

        if (config.getTlsTrustRootCert() !=null && !config.getTlsTrustRootCert().trim().isEmpty()) {
            builder.trustManager(new File(config.getTlsTrustRootCert()));
        }

        return builder.build();
    }

}
