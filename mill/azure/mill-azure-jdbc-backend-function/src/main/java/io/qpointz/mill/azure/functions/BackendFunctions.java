package io.qpointz.mill.azure.functions;

import io.qpointz.mill.proto.HandshakeRequest;
import io.qpointz.mill.proto.HandshakeResponse;
import io.qpointz.mill.proto.ListSchemasRequest;
import io.qpointz.mill.proto.ListSchemasResponse;
import io.qpointz.mill.services.ServiceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class BackendFunctions {


    @Bean("handshake")
    public Function<HandshakeRequest, HandshakeResponse>  handshake(@Autowired ServiceHandler serviceHandler) {
        return serviceHandler::handshake;
    }

    @Bean("listSchemas")
    public Function<ListSchemasRequest, ListSchemasResponse> listSchemas(@Autowired ServiceHandler serviceHandler) {
        return serviceHandler::listSchemas;
    }

}
