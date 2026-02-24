package io.qpointz.mill.data.backend;

import io.qpointz.mill.data.backend.dispatchers.*;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.security.NoneSecurityProvider;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.extension.SimpleExtension;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceHandlerTest {

    private ExecutionProvider executionProvider;
    private ServiceHandler serviceHandler;

    @BeforeEach
    void setUp() {
        executionProvider = mock(ExecutionProvider.class);
        val schemaProvider = mock(SchemaProvider.class);
        val sqlProvider = mock(SqlProvider.class);
        val extensionCollection = SimpleExtension.loadDefaults();
        val securityDispatcher = new SecurityDispatcherImpl(new NoneSecurityProvider());
        val resultAllocator = new ResultAllocatorImpl();
        val substraitDispatcher = new SubstraitDispatcher(extensionCollection);
        val dataDispatcher = new DataOperationDispatcherImpl(
                schemaProvider, executionProvider, sqlProvider,
                securityDispatcher, null, substraitDispatcher, resultAllocator);
        serviceHandler = new ServiceHandler(dataDispatcher, securityDispatcher, resultAllocator, substraitDispatcher);
    }

    @Test
    void submitQuery() {
        when(executionProvider.execute(any(), any())).thenReturn(mockVectorBlockIterator());

        var resp = serviceHandler.data().submitQuery(QueryRequest.getDefaultInstance());
        assertTrue(resp.hasPagingId());
        assertTrue(resp.hasVector());
        var pagingId = resp.getPagingId();

        resp = serviceHandler.data().fetchResult(QueryResultRequest.newBuilder().setPagingId(pagingId).build());
        assertTrue(resp.hasPagingId());
        assertTrue(resp.hasVector());
        assertNotEquals(resp.getPagingId(), pagingId);
    }

    private VectorBlock mockVectorBlock() {
        return VectorBlock.newBuilder()
                .setVectorSize(3)
                .setSchema(VectorBlockSchema.newBuilder()
                        .addFields(Field.newBuilder()
                                .setFieldIdx(0)
                                .setName("ID")
                                .setType(DataType.newBuilder()
                                        .setType(LogicalDataType.newBuilder()
                                                .setTypeId(LogicalDataType.LogicalDataTypeId.INT)
                                                .build())
                                        .setNullability(DataType.Nullability.NULL)
                                        .build())
                        ))
                .addVectors(Vector.newBuilder()
                        .setNulls(Vector.NullsVector.newBuilder().addAllNulls(List.of(false, false, false)).build())
                        .setI32Vector(Vector.I32Vector.newBuilder()
                                .addAllValues(List.of(1, 2, 3))
                        ))
                .build();
    }

    private VectorBlockIterator mockVectorBlockIterator() {
        return new VectorBlockIterator() {
            private final Iterator<VectorBlock> blocks = List.<VectorBlock>of(
                    mockVectorBlock(),
                    mockVectorBlock(),
                    mockVectorBlock()
            ).iterator();

            @Override
            public boolean hasNext() {
                return blocks.hasNext();
            }

            @Override
            public VectorBlock next() {
                return blocks.next();
            }

            @Override
            public VectorBlockSchema schema() {
                return VectorBlockSchema.getDefaultInstance();
            }
        };
    }
}
