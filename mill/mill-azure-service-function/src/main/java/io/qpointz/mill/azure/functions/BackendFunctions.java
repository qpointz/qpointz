package io.qpointz.mill.azure.functions;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.ServiceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class BackendFunctions {

    private final ServiceHandler serviceHandler;

    public BackendFunctions(@Autowired ServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Bean("handshake")
    public Function<HandshakeRequest, HandshakeResponse>  handshake() {
        return r-> serviceHandler.handshake();
    }

    @Bean("listSchemas")
    public Function<ListSchemasRequest, ListSchemasResponse> listSchemas() {
        return r -> serviceHandler.listSchemas();
    }

    @Bean("getSchema")
    public Function<GetSchemaRequest, GetSchemaResponse> getSchemas() {
        return serviceHandler::getSchemaProto;
    }

    @Bean("parseSql")
    public Function<ParseSqlRequest, ParseSqlResponse> parseSql() {
        return serviceHandler::parseSqlProto;
    }

    @Bean("submitQuery")
    public Function<QueryRequest, QueryResultResponse> submitQuery() {
        return serviceHandler::submitQuery;
    }

    @Bean("fetchQueryResult")
    public Function<QueryResultRequest, QueryResultResponse> fetchQueryResult() {
        return serviceHandler::fetchResult;
    }

}
