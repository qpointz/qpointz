package io.qpointz.mill.services;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.qpointz.mill.vectors.sql.ResultSetVectorBlockIterator;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.Plan;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceHandlerTest extends ServiceBaseTest {

    @Test
    void submitQuery(@Autowired MetadataProvider metadataProvider,
                           @Autowired ExecutionProvider executionProvider,
                           @Autowired SecurityProvider securityProvide) {
        val serviceHander = new ServiceHandler(metadataProvider, executionProvider, null, securityProvide, null);
        VectorBlockIterator mockIterator = mockVectorBlockIterator();
        when(executionProvider.execute(any(),any())).thenReturn(mockIterator);
        var resp = serviceHander.submitPlanQuery(ExecPlanRequest.getDefaultInstance());
        assertTrue(resp.hasPagingId());
        assertTrue(resp.hasVector());
        var pagingId = resp.getPagingId();

        resp = serviceHander.fetchResult(FetchQueryResultRequest.newBuilder().setPagingId(pagingId).build());
        assertTrue(resp.hasPagingId());
        assertTrue(resp.hasVector());
        assertNotEquals(resp.getPagingId(), pagingId);
    }

    private VectorBlockIterator mockVectorBlockIterator() {
        return new VectorBlockIterator() {

            private Iterator<VectorBlock> blocks = List.<VectorBlock>of(
                    VectorBlock.getDefaultInstance(),
                    VectorBlock.getDefaultInstance(),
                    VectorBlock.getDefaultInstance()
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