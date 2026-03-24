package io.qpointz.mill.data.backend.grpc

import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.data.backend.ServiceHandler
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.proto.DataConnectServiceGrpc
import io.qpointz.mill.proto.GetDialectRequest
import io.qpointz.mill.proto.GetDialectResponse
import io.qpointz.mill.proto.GetSchemaRequest
import io.qpointz.mill.proto.GetSchemaResponse
import io.qpointz.mill.proto.HandshakeRequest
import io.qpointz.mill.proto.HandshakeResponse
import io.qpointz.mill.proto.ListSchemasRequest
import io.qpointz.mill.proto.ListSchemasResponse
import io.qpointz.mill.proto.ParseSqlRequest
import io.qpointz.mill.proto.ParseSqlResponse
import io.qpointz.mill.proto.QueryRequest
import io.qpointz.mill.proto.QueryResultResponse
import io.qpointz.mill.vectors.VectorBlockIterator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.function.Function
import java.util.function.Supplier

/**
 * gRPC data-plane service (Substrait / SQL) backed by the shared [ServiceHandler] stack.
 */
@Component
@ConditionalOnService(value = "grpc", group = "data")
class MillGrpcService(
    serviceHandler: ServiceHandler,
) : DataConnectServiceGrpc.DataConnectServiceImplBase() {

    private val log = LoggerFactory.getLogger(javaClass)

    private val dataOpDispatcher: DataOperationDispatcher = serviceHandler.data()

    private fun <R, S> replyOne(
        request: R,
        observer: StreamObserver<S>,
        producer: Function<R, S>,
    ) {
        observer.onNext(producer.apply(request))
        observer.onCompleted()
    }

    private fun traceRequest(name: String, toString: Supplier<String>) {
        if (log.isTraceEnabled) {
            log.trace("{} request:{}", name, toString.get())
        }
    }

    override fun handshake(request: HandshakeRequest, observer: StreamObserver<HandshakeResponse>) {
        traceRequest("Handshake") { request.toString() }
        replyOne(request, observer) { dataOpDispatcher.handshake(request) }
    }

    override fun listSchemas(request: ListSchemasRequest, responseObserver: StreamObserver<ListSchemasResponse>) {
        traceRequest("listSchemas") { request.toString() }
        replyOne(request, responseObserver) { dataOpDispatcher.listSchemas(request) }
    }

    override fun getSchema(request: GetSchemaRequest, responseObserver: StreamObserver<GetSchemaResponse>) {
        traceRequest("getSchema") { request.toString() }
        replyOne(request, responseObserver) { dataOpDispatcher.getSchema(request) }
    }

    override fun getDialect(request: GetDialectRequest, responseObserver: StreamObserver<GetDialectResponse>) {
        traceRequest("getDialect") { request.toString() }
        replyOne(request, responseObserver) { dataOpDispatcher.getDialect(request) }
    }

    override fun parseSql(request: ParseSqlRequest, responseObserver: StreamObserver<ParseSqlResponse>) {
        traceRequest("parseSql") { request.toString() }
        replyOne(request, responseObserver) { dataOpDispatcher.parseSql(request) }
    }

    override fun execQuery(request: QueryRequest, responseObserver: StreamObserver<QueryResultResponse>) {
        traceRequest("execQuery") { request.toString() }
        val iterator = dataOpDispatcher.execute(request)
        streamResult(iterator, responseObserver)
    }

    private fun streamResult(
        iterator: VectorBlockIterator,
        responseObserver: StreamObserver<QueryResultResponse>,
    ) {
        val callObserver = responseObserver as ServerCallStreamObserver<QueryResultResponse>
        while (iterator.hasNext()) {
            val vectorBlock = iterator.next()
            val resp = QueryResultResponse.newBuilder()
                .setVector(vectorBlock)
                .build()
            callObserver.onNext(resp)
        }
        callObserver.onCompleted()
    }
}
