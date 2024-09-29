package io.qpointz.mill.azure.functions;

import com.google.protobuf.InvalidProtocolBufferException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import io.qpointz.mill.proto.HandshakeRequest;
import io.qpointz.mill.proto.HandshakeResponse;
import io.qpointz.mill.proto.ListSchemasRequest;
import io.qpointz.mill.proto.ListSchemasResponse;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component
public class AzureBackendFunctions {

    @Autowired
    @Qualifier("handshake")
    protected Function<HandshakeRequest, HandshakeResponse> handshakeImpl;

    @FunctionName("handshake")
    public byte[] handshake (
            @HttpTrigger(name = "req",
                    methods = { HttpMethod.POST },
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<byte[]>> request,
            ExecutionContext context) throws InvalidProtocolBufferException {

        val req = HandshakeRequest.parseFrom(request.getBody().get());
        return handshakeImpl.apply(req)
                .toByteArray();
    }

    @Autowired
    @Qualifier("listSchemas")
    protected Function<ListSchemasRequest, ListSchemasResponse> listSchemasImpl;

    @FunctionName("listSchemas")
    public byte[] listSchemas (
            @HttpTrigger(name = "req",
                    methods = { HttpMethod.POST },
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<byte[]>> request,
            ExecutionContext context) throws InvalidProtocolBufferException {

        val req = ListSchemasRequest.parseFrom(request.getBody().get());
        return listSchemasImpl.apply(req)
                .toByteArray();
    }


}
