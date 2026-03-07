package io.qpointz.mill.data.backend.access.http.controllers;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.data.backend.access.http.components.MessageHelper;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@ConditionalOnService(value = "http", group = "data")
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

    @PostMapping("/GetDialect")
    public ResponseEntity<?> getDialect(@RequestBody(required = false) byte[] payload,
                                        @RequestHeader(value = "Accept", defaultValue= MediaType.APPLICATION_JSON_VALUE) String acceptsHeader,
                                        @RequestHeader(value = "Content-Type", defaultValue= MediaType.APPLICATION_JSON_VALUE) String contentTypeHeader) {
        return MessageHelper.apply(
                this.ensurePayload(payload),
                MessageHelper::getDialectRequest,
                dispatcher::getDialect,
                GetDialectRequest.newBuilder(),
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

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<?> handleStatusRuntimeException(StatusRuntimeException ex) {
        val grpcStatus = ex.getStatus();
        val httpStatus = toHttpStatus(grpcStatus.getCode());
        return ResponseEntity
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "code", grpcStatus.getCode().name(),
                        "message", grpcStatus.getDescription() == null ? "Request failed" : grpcStatus.getDescription()
                ));
    }

    private HttpStatus toHttpStatus(Status.Code code) {
        return switch (code) {
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNIMPLEMENTED -> HttpStatus.NOT_IMPLEMENTED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}
