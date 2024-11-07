package io.qpointz.mill.services;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.vectors.VectorBlockIterator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ServiceHandlerTest extends ServiceBaseTest {

    @Test
    void submitQuery(@Autowired ExecutionProvider executionProvider,
                     @Autowired ServiceHandler serviceHandler) {
        VectorBlockIterator mockIterator = mockVectorBlockIterator();
        when(executionProvider.execute(any(),any())).thenReturn(mockIterator);
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
                        .setNulls(Vector.NullsVector.newBuilder().addAllNulls(List.of(false,false,false)).build())
                        .setI32Vector(Vector.I32Vector.newBuilder()
                            .addAllValues(List.of(1,2,3))
                        ))
                .build();
    }

    private VectorBlockIterator mockVectorBlockIterator() {
        return new VectorBlockIterator() {

            private Iterator<VectorBlock> blocks = List.<VectorBlock>of(
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