package io.qpointz.delta.service;

import io.qpointz.delta.proto.Schema;
import io.qpointz.delta.proto.VectorBlock;

import java.util.Iterator;

public interface VectorBlockIterator extends Iterator<VectorBlock> {

    boolean hasSchema();

    Schema getSchema();

}
