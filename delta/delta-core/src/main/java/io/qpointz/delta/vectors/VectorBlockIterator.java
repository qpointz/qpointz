package io.qpointz.delta.vectors;

import io.qpointz.delta.proto.VectorBlock;
import io.qpointz.delta.proto.VectorBlockSchema;

import java.util.Iterator;

public interface VectorBlockIterator extends Iterator<VectorBlock> {
    VectorBlockSchema schema();
}
