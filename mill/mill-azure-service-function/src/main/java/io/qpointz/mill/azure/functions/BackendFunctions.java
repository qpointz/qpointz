package io.qpointz.mill.azure.functions;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.security.configuration.SecurityConfig;
import io.qpointz.mill.services.ServiceHandler;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class BackendFunctions {

    private final DataOperationDispatcher dataDispatcher;

    public BackendFunctions(@Autowired DataOperationDispatcher dataDispatcher) {
        this.dataDispatcher = dataDispatcher;
    }

    @Bean("handshake")
    public Function<HandshakeRequest, HandshakeResponse>  handshake() {
        return dataDispatcher::handshake;
    }

    @Bean("listSchemas")
    public Function<ListSchemasRequest, ListSchemasResponse> listSchemas() {
        return dataDispatcher::listSchemas;
    }

    @Bean("getSchema")
    public Function<GetSchemaRequest, GetSchemaResponse> getSchemas() {
        return dataDispatcher::getSchema;
    }

    @Bean("parseSql")
    public Function<ParseSqlRequest, ParseSqlResponse> parseSql() {
        return dataDispatcher::parseSql;
    }

    @Bean("submitQuery")
    public Function<QueryRequest, QueryResultResponse> submitQuery() {
        return dataDispatcher::submitQuery;
    }

    @Bean("fetchQueryResult")
    public Function<QueryResultRequest, QueryResultResponse> fetchQueryResult() {
        return dataDispatcher::fetchResult;
    }

}
