
package io.qpointz.delta.vectors;

import io.qpointz.delta.proto.Vector;
import lombok.val;

import java.util.Collection;

public interface VectorProducer<T>  {

    void reset();

    void append(T value);

    void appendAll(Collection<T> values);

    void appendNull();

    void appendNulls(int count);

    void addToVector(Vector.Builder builder);

    default Vector.Builder asVectorBuilder() {
        val builder = Vector.newBuilder();
        addToVector(builder);
        return builder;
    }

}
