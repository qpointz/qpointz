package io.qpointz.rapids.jdbc;

import io.qpointz.rapids.grpc.ExecSqlResponse;

import java.sql.ResultSet;
import java.util.Iterator;

public class ResultSets {

    public static ResultSet from (Iterator<ExecSqlResponse> responseIterator) {
        return new RapidsResponseIteratorResultSet(responseIterator);
    }

}
