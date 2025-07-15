package io.qpointz.mill.services.access.http.controllers;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.access.http.components.MessageHelper;
import io.qpointz.mill.services.annotations.ConditionalOnService;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static io.qpointz.mill.services.access.http.components.MessageHelper.*;


@Slf4j
@RestController
@ConditionalOnService("jet-http")
@RequestMapping(value = AccessServiceController.CONTEXT_PATH,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROTOBUF_VALUE },
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROTOBUF_VALUE })
public class AccessServiceController {

    public static final String CONTEXT_PATH = "/services/jet";
    private final DataOperationDispatcher dispatcher;

    public AccessServiceController(DataOperationDispatcher dispatcher) {
        log.debug("Access Service HTTP Controller initialized at {}", CONTEXT_PATH);
        this.dispatcher = dispatcher;
    }

    @PostMapping(value = "/ListSchemas",
                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROTOBUF_VALUE },
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROTOBUF_VALUE })
    public ResponseEntity<?> listSchemas(@RequestBody(required = false) byte[] payload,
                                         @RequestHeader(value = "Accept", defaultValue= MediaType.APPLICATION_JSON_VALUE) String acceptsHeader,
                                         @RequestHeader(value = "Content-Type", defaultValue= MediaType.APPLICATION_JSON_VALUE) String contentTypeHeader
                                         ) {
        return MessageHelper.apply(
                this.ensurePayload(payload),
                MessageHelper::listSchemasRequest,
                dispatcher::listSchemas,
                ListSchemasRequest.newBuilder(),
                contentTypeHeader,
                acceptsHeader);
    }

    private byte[] ensurePayload(byte[] payload) {
        if (payload!=null) {
            return payload;
        }
        return new byte[0];
    }

    @PostMapping(value = "/Handshake",
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROTOBUF_VALUE },
                consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROTOBUF_VALUE })
    public ResponseEntity<?> handshake(@RequestBody(required = false) byte[] payload,
                                       @RequestHeader(value = "Accept", defaultValue= MediaType.APPLICATION_JSON_VALUE) String acceptsHeader,
                                       @RequestHeader(value = "Content-Type", defaultValue= MediaType.APPLICATION_JSON_VALUE) String contentTypeHeader) {
        return MessageHelper.apply(
                this.ensurePayload(payload),
                MessageHelper::handshakeRequest,
                dispatcher::handshake,
                HandshakeRequest.newBuilder(),
                contentTypeHeader,
                acceptsHeader);
    }

    @PostMapping("/GetSchema")
    public ResponseEntity<?> getSchema(@RequestBody byte[] payload,
                                       @RequestHeader(value = "Accept", defaultValue= MediaType.APPLICATION_JSON_VALUE) String acceptsHeader,
                                       @RequestHeader(value = "Content-Type", defaultValue= MediaType.APPLICATION_JSON_VALUE) String contentTypeHeader) {
        return MessageHelper.apply(
                payload,
                MessageHelper::getSchemaRequest,
                dispatcher::getSchema,
                GetSchemaRequest.newBuilder(),
                contentTypeHeader,
                acceptsHeader);
    }

    @PostMapping("/ParseSql")
    public ResponseEntity<?> parseSql(@RequestBody byte[] payload,
                                      @RequestHeader(value = "Accept", defaultValue= MediaType.APPLICATION_JSON_VALUE) String acceptsHeader,
                                      @RequestHeader(value = "Content-Type", defaultValue= MediaType.APPLICATION_JSON_VALUE) String contentTypeHeader) {
        return MessageHelper.apply(
                payload,
                MessageHelper::parseSqlRequest,
                dispatcher::parseSql,
                ParseSqlRequest.newBuilder(),
                contentTypeHeader,
                acceptsHeader);
    }

    @PostMapping("/SubmitQuery")
    public ResponseEntity<?> submitQuery(@RequestBody byte[] payload,
                                         @RequestHeader(value = "Accept", defaultValue= MediaType.APPLICATION_JSON_VALUE) String acceptsHeader,
                                         @RequestHeader(value = "Content-Type", defaultValue= MediaType.APPLICATION_JSON_VALUE) String contentTypeHeader) {
        return MessageHelper.apply(
                payload,
                MessageHelper::queryRequest,
                dispatcher::submitQuery,
                QueryRequest.newBuilder(),
                contentTypeHeader,
                acceptsHeader);
    }

    @PostMapping("/FetchQueryResult")
    public ResponseEntity<?> fetchQueryResult(@RequestBody byte[] payload,
                                              @RequestHeader(value = "Accept", defaultValue= MediaType.APPLICATION_JSON_VALUE) String acceptsHeader,
                                              @RequestHeader(value = "Content-Type", defaultValue= MediaType.APPLICATION_JSON_VALUE) String contentTypeHeader) {
        return MessageHelper.apply(
                payload,
                MessageHelper::queryResultRequest,
                dispatcher::fetchResult,
                QueryResultRequest.newBuilder(),
                contentTypeHeader,
                acceptsHeader);
    }

}
