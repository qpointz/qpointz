
package io.qpointz.delta.vectors;

import io.qpointz.delta.proto.Vector;
import lombok.val;

import java.util.Collection;
import java.util.Collections;

public interface VectorProducer<T>  {

    void reset();

    void append(T value, Boolean isNull);

    default void append(Collection<T> values, Collection<Boolean> nulls) {
        if (values.isEmpty() != nulls.isEmpty()) {
            throw new IllegalArgumentException("One of nulls or values collection is empty");
        }
        if (values.size() != nulls.size()) {
            throw new IllegalArgumentException("Nulls and values collection size mismatch");
        }

        val valIter = values.iterator();
        val nulIter = nulls.iterator();
        while (valIter.hasNext() && nulIter.hasNext()) {
            this.append(valIter.next(), nulIter.next());
        }
    }

    void build(Vector.Builder builder);

    default Vector.Builder vectorBuilder() {
        val builder = Vector.newBuilder();
        build(builder);
        return builder;
    }

}
