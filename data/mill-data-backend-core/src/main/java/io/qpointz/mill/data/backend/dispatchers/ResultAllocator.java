package io.qpointz.mill.data.backend.dispatchers;

import io.qpointz.mill.proto.VectorBlock;
import io.qpointz.mill.vectors.VectorBlockIterator;

public interface ResultAllocator {

    record AllocationResult(String pagingId) {}

    record FetchResult(String requestPagingId, String nextPagingId, boolean exists, VectorBlock block) {}


    AllocationResult allocate(VectorBlockIterator iterator);

    FetchResult nextBlock(String pagingId);

}
