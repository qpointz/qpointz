package io.qpointz.mill.azure.functions;
import com.google.common.net.HttpHeaders;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import io.qpointz.mill.proto.*;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class AzureBackendFunctions {


    private static final String CONTENT_TYPE_JSON  = "application/json";
    private static final String CONTENT_TYPE_PROTO = "application/protobuf"; //; proto=org.some.Message

    public AzureBackendFunctions(@Qualifier("listSchemas") Function<ListSchemasRequest, ListSchemasResponse> listSchemasImpl,
                                 @Qualifier("handshake") Function<HandshakeRequest, HandshakeResponse> handshakeImpl,
                                 @Qualifier("getSchema") Function<GetSchemaRequest, GetSchemaResponse> getSchemaImpl,
                                 @Qualifier("parseSql") Function<ParseSqlRequest, ParseSqlResponse> parseSqlImpl,
                                 @Qualifier("submitQuery") Function<QueryRequest, QueryResultResponse> submitQueryImpl,
                                 @Qualifier("fetchQueryResult") Function<QueryResultRequest, QueryResultResponse> fetchQueryResultImpl) {
        this.listSchemasImpl = listSchemasImpl;
        this.handshakeImpl = handshakeImpl;
        this.getSchemaImpl = getSchemaImpl;
        this.parseSqlImpl = parseSqlImpl;
        this.submitQueryImpl = submitQueryImpl;
        this.fetchQueryResultImpl = fetchQueryResultImpl;
    }

    private Map<String, String> normailizeHeaders(HttpRequestMessage<Optional<String>> req) {
        return req.getHeaders()
                .entrySet().stream()
                .collect(Collectors.toMap(k-> k.getKey().toLowerCase(), v -> v.getValue().toLowerCase()));
    }

    private <T extends Message.Builder> T buildFromRequest(HttpRequestMessage<Optional<String>> httpRequestMessage, T messageBuilder) throws InvalidProtocolBufferException {
        Optional<String> mayBeBody = httpRequestMessage.getBody();

        if (mayBeBody.isEmpty()) {
            return messageBuilder;
        }
        val content = mayBeBody.get();
        val headers = normailizeHeaders(httpRequestMessage);
        val contentType = headers.getOrDefault(HttpHeaders.CONTENT_TYPE.toLowerCase(), CONTENT_TYPE_JSON);

        if (contentType.equals(CONTENT_TYPE_JSON)) {
            JsonFormat.parser().merge(content, messageBuilder);
            return messageBuilder;
        }
        throw new IllegalArgumentException("Unsupported request content type: " + contentType);
    }

    private HttpResponseMessage buildResponse(ExecutionContext context, HttpRequestMessage<Optional<String>> req, Message responseMessage) throws InvalidProtocolBufferException {
        val logger = context.getLogger();
        for (String s : req.getHeaders().keySet()) {
            logger.info(String.format("%s=%s", s, req.getHeaders().get(s)));
        }

        val headers = normailizeHeaders(req);

        val accepts = headers.getOrDefault(HttpHeaders.ACCEPT.toLowerCase(), CONTENT_TYPE_PROTO);
        logger.info(String.format("Client accepts:%s", accepts));

        if (accepts.contains(CONTENT_TYPE_JSON)) {
            logger.info("Produce JSON response");
            return req.createResponseBuilder(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body(JsonFormat.printer().print(responseMessage))
                    .build();
        }

        logger.info("Produce PROTO response");
        val contentType = CONTENT_TYPE_PROTO + ";proto=" + responseMessage.getClass().getCanonicalName();
        return req.createResponseBuilder(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(responseMessage.toByteArray())
                .build();
    }

    private <T extends Message.Builder, R extends Message, E extends Message> HttpResponseMessage apply(ExecutionContext context,
                                                                                                        HttpRequestMessage<Optional<String>> req,
                                                                                                        Supplier<T> createBuilder,
                                                                                                        Function<T,R> asRequest,
                                                                                                        Function<R,E> func) throws InvalidProtocolBufferException {
        val request = asRequest.apply(buildFromRequest(req, createBuilder.get()));
        return buildResponse(context, req, func.apply(request));
    }

    protected final Function<ListSchemasRequest, ListSchemasResponse> listSchemasImpl;

    @FunctionName("ListSchemas")
    public HttpResponseMessage listSchemas (
            @HttpTrigger(name = "req",
                    methods = { HttpMethod.POST },
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> req,
            ExecutionContext context) throws InvalidProtocolBufferException {

        return apply(context, req, ListSchemasRequest::newBuilder, ListSchemasRequest.Builder::build, listSchemasImpl);
    }

    protected final Function<HandshakeRequest, HandshakeResponse> handshakeImpl;

    @FunctionName("Handshake")
    public HttpResponseMessage handshake (
            @HttpTrigger(name = "req",
                    methods = { HttpMethod.POST },
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> req,
            ExecutionContext context) throws InvalidProtocolBufferException {

        return apply(context, req, HandshakeRequest::newBuilder, HandshakeRequest.Builder::build, handshakeImpl);
    }


    protected final Function<GetSchemaRequest, GetSchemaResponse> getSchemaImpl;

    @FunctionName("GetSchema")
    public HttpResponseMessage getSchema (
            @HttpTrigger(name = "req",
                    methods = { HttpMethod.POST },
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> req,
            ExecutionContext context) throws InvalidProtocolBufferException {

        return apply(context, req, GetSchemaRequest::newBuilder, GetSchemaRequest.Builder::build, getSchemaImpl);
    }


    protected final Function<ParseSqlRequest, ParseSqlResponse> parseSqlImpl;

    @FunctionName("ParseSql")
    public HttpResponseMessage parseSql (
            @HttpTrigger(name = "req",
                    methods = { HttpMethod.POST },
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> req,
            ExecutionContext context) throws InvalidProtocolBufferException {

        return apply(context, req, ParseSqlRequest::newBuilder, ParseSqlRequest.Builder::build, parseSqlImpl);
    }

    protected final Function<QueryRequest, QueryResultResponse> submitQueryImpl;

    @FunctionName("SubmitQuery")
    public HttpResponseMessage submitQuery (
            @HttpTrigger(name = "req",
                    methods = { HttpMethod.POST },
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> req,
            ExecutionContext context) throws InvalidProtocolBufferException {

        return apply(context, req, QueryRequest::newBuilder, QueryRequest.Builder::build, submitQueryImpl);
    }

    protected final Function<QueryResultRequest, QueryResultResponse> fetchQueryResultImpl;

    @FunctionName("FetchQueryResult")
    public HttpResponseMessage fetchQueryResult (
            @HttpTrigger(name = "req",
                    methods = { HttpMethod.POST },
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> req,
            ExecutionContext context) throws InvalidProtocolBufferException {

        return apply(context, req, QueryResultRequest::newBuilder, QueryResultRequest.Builder::build, fetchQueryResultImpl);
    }
}

