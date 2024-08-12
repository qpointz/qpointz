package io.qpointz.mill.vectors;

import io.qpointz.mill.proto.VectorBlock;
import io.qpointz.mill.proto.VectorBlockSchema;

import java.util.Iterator;

public interface VectorBlockIterator extends Iterator<VectorBlock> {
    VectorBlockSchema schema();
}
