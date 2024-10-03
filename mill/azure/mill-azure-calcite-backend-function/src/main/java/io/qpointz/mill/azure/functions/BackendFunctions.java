package io.qpointz.mill.azure.functions;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.ServiceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class BackendFunctions {

    @Autowired
    ServiceHandler serviceHandler;

    @Bean("handshake")
    public Function<HandshakeRequest, HandshakeResponse>  handshake() {
        return serviceHandler::handshake;
    }

    @Bean("listSchemas")
    public Function<ListSchemasRequest, ListSchemasResponse> listSchemas() {
        return serviceHandler::listSchemas;
    }

    @Bean("getSchema")
    public Function<GetSchemaRequest, GetSchemaResponse> getSchemas() {
        return serviceHandler::getSchemaProto;
    }

    @Bean("parseSql")
    public Function<ParseSqlRequest, ParseSqlResponse> parseSql() {
        return serviceHandler::parseSqlProto;
    }

    @Bean("submitSqlQuery")
    public Function<ExecSqlRequest, FetchQueryResultResponse> submitSqlQuery() {
        return serviceHandler::submitSqlQuery;
    }

    @Bean("submitPlanQuery")
    public Function<ExecPlanRequest, FetchQueryResultResponse> submitPlanQuery() {
        return serviceHandler::submitPlanQuery;
    }

    @Bean("fetchQueryResult")
    public Function<FetchQueryResultRequest, FetchQueryResultResponse> fetchQueryResult() {
        return serviceHandler::fetchResult;
    }

}
