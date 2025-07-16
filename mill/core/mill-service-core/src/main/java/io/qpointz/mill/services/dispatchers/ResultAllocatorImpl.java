package io.qpointz.mill.services.dispatchers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.qpointz.mill.proto.QueryResultResponse;
import io.qpointz.mill.vectors.VectorBlockIterator;
import lombok.Getter;
import lombok.val;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ResultAllocatorImpl implements ResultAllocator {

    @Getter(lazy = true)
    private final Cache<String, VectorBlockIterator> submitCache = createCache();

    private Cache<String, VectorBlockIterator> createCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    private String newKey() {
        val random = new SecureRandom();
        val bytes = new byte[32];
        random.nextBytes(bytes);
        return new String(Base64.getEncoder().encode(bytes));
    }


    @Override
    public AllocationResult allocate(VectorBlockIterator iterator) {
        val key = newKey();
        this.getSubmitCache().put(key, iterator);
        return new AllocationResult(key);
    }


    @Override
    public FetchResult nextBlock(String pagingId) {
        val cache = this.getSubmitCache();
        VectorBlockIterator iter;
        synchronized (cache) {
            iter = cache.getIfPresent(pagingId);
            if (iter == null) {
                return new FetchResult(pagingId, null, false, null);
            }
            cache.invalidate(pagingId);
        }

        if (!iter.hasNext()) {
            return new FetchResult(pagingId, null, false, null);
        }

        val nextKey = newKey();
        val vector = iter.next();

        if (vector.getVectorSize() == 0) {
            return new FetchResult(pagingId, null, true, vector);
        }

        cache.put(nextKey, iter);
        return new FetchResult(pagingId, nextKey, true, vector);
    }
}
