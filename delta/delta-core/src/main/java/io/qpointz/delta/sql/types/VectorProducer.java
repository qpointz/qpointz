package io.qpointz.delta.sql.types;

import io.qpointz.delta.proto.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface VectorProducer {
    void read(ResultSet rs, int idx) throws SQLException;
    Vector toVector();
}
