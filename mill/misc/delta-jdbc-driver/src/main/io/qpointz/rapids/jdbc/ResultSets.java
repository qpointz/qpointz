package io.qpointz.rapids.jdbc;

import io.qpointz.mill.proto.ExecQueryResponse;

import java.sql.ResultSet;
import java.util.Iterator;

public class ResultSets {

    public static ResultSet from (Iterator<ExecQueryResponse> responseIterator) {
        return new RapidsResponseIteratorResultSet(responseIterator);
    }

}
