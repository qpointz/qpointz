package io.qpointz.mill.data.backend.grpc

import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import io.qpointz.mill.data.backend.ServiceHandler
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
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
import io.qpointz.mill.proto.SQLStatement
import io.qpointz.mill.proto.VectorBlock
import io.qpointz.mill.vectors.VectorBlockIterator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

/**
 * Unit tests for each gRPC operation on [MillGrpcService] using mocked [DataOperationDispatcher] and stream observers.
 */
@ExtendWith(MockitoExtension::class)
class MillGrpcServiceTest {

    @Mock
    lateinit var serviceHandler: ServiceHandler

    @Mock
    lateinit var dispatcher: DataOperationDispatcher

    private lateinit var service: MillGrpcService

    @BeforeEach
    fun setup() {
        whenever(serviceHandler.data()).thenReturn(dispatcher)
        service = MillGrpcService(serviceHandler)
    }

    @Test
    fun shouldHandshake_whenHandshakeInvoked() {
        val request = HandshakeRequest.getDefaultInstance()
        val response = HandshakeResponse.newBuilder().build()
        whenever(dispatcher.handshake(request)).thenReturn(response)
        val observer = mock<StreamObserver<HandshakeResponse>>()

        service.handshake(request, observer)

        verify(dispatcher).handshake(request)
        verify(observer).onNext(response)
        verify(observer).onCompleted()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun shouldListSchemas_whenListSchemasInvoked() {
        val request = ListSchemasRequest.getDefaultInstance()
        val response = ListSchemasResponse.newBuilder().build()
        whenever(dispatcher.listSchemas(request)).thenReturn(response)
        val observer = mock<StreamObserver<ListSchemasResponse>>()

        service.listSchemas(request, observer)

        verify(dispatcher).listSchemas(request)
        verify(observer).onNext(response)
        verify(observer).onCompleted()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun shouldGetSchema_whenGetSchemaInvoked() {
        val request = GetSchemaRequest.newBuilder().setSchemaName("s").build()
        val response = GetSchemaResponse.newBuilder().build()
        whenever(dispatcher.getSchema(request)).thenReturn(response)
        val observer = mock<StreamObserver<GetSchemaResponse>>()

        service.getSchema(request, observer)

        verify(dispatcher).getSchema(request)
        verify(observer).onNext(response)
        verify(observer).onCompleted()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun shouldGetDialect_whenGetDialectInvoked() {
        val request = GetDialectRequest.newBuilder().setDialectId("CALCITE").build()
        val response = GetDialectResponse.newBuilder().build()
        whenever(dispatcher.getDialect(request)).thenReturn(response)
        val observer = mock<StreamObserver<GetDialectResponse>>()

        service.getDialect(request, observer)

        verify(dispatcher).getDialect(request)
        verify(observer).onNext(response)
        verify(observer).onCompleted()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun shouldParseSql_whenParseSqlInvoked() {
        val stmt = SQLStatement.newBuilder().setSql("SELECT 1").build()
        val request = ParseSqlRequest.newBuilder().setStatement(stmt).build()
        val response = ParseSqlResponse.newBuilder().build()
        whenever(dispatcher.parseSql(request)).thenReturn(response)
        val observer = mock<StreamObserver<ParseSqlResponse>>()

        service.parseSql(request, observer)

        verify(dispatcher).parseSql(request)
        verify(observer).onNext(response)
        verify(observer).onCompleted()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun shouldStreamQueryResults_whenExecQueryInvoked() {
        val request = QueryRequest.newBuilder().setStatement(stmt("SELECT 1")).build()
        val block1 = VectorBlock.newBuilder().setVectorSize(1).build()
        val block2 = VectorBlock.newBuilder().setVectorSize(2).build()
        val iterator = mock<VectorBlockIterator>()
        whenever(iterator.hasNext()).thenReturn(true, true, false)
        whenever(iterator.next()).thenReturn(block1, block2)
        whenever(dispatcher.execute(request)).thenReturn(iterator)

        val observer = mock<ServerCallStreamObserver<QueryResultResponse>>()

        service.execQuery(request, observer)

        verify(dispatcher).execute(request)
        verify(observer).onNext(
            argThat { r: QueryResultResponse ->
                r.hasVector() && r.vector == block1
            },
        )
        verify(observer).onNext(
            argThat { r: QueryResultResponse ->
                r.hasVector() && r.vector == block2
            },
        )
        verify(observer).onCompleted()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun shouldCompleteStream_whenExecQueryReturnsEmptyIterator() {
        val request = QueryRequest.newBuilder().setStatement(stmt("SELECT 1")).build()
        val iterator = mock<VectorBlockIterator>()
        whenever(iterator.hasNext()).thenReturn(false)
        whenever(dispatcher.execute(request)).thenReturn(iterator)
        val observer = mock<ServerCallStreamObserver<QueryResultResponse>>()

        service.execQuery(request, observer)

        verify(dispatcher).execute(request)
        verify(observer).onCompleted()
        verifyNoMoreInteractions(observer)
    }

    private fun stmt(sql: String): SQLStatement = SQLStatement.newBuilder().setSql(sql).build()
}
