package io.qpointz.mill;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.sql.RecordReaderResultSetBase;
import io.qpointz.mill.sql.VectorBlockRecordIterator;
import lombok.*;

import java.sql.*;
import java.sql.Statement;
import java.util.Iterator;


public abstract class MillStatementBase implements Statement {

    public MillStatementBase(MillConnection connection) {
        this.connection = connection;
    }

    private final MillConnection connection;

    @Override
    public Connection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        val client = this.connection.getClient();
        val queryConfig = QueryExecutionConfig.newBuilder()
                .setBatchSize(this.getFetchSize())
                .build();
        val request = ExecSqlRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder().setSql(sql).build())
                .setConfig(queryConfig)
                .build();
        val resp = client.newBlockingStub().execSql(request);

        val iter = new Iterator<VectorBlock>() {
            private Iterator<ExecQueryResponse> r = resp;
            @Override
            public boolean hasNext() {
                return r.hasNext();
            }

            @Override
            public VectorBlock next() {
                val block = r.next().getVector();
                return block;
            }
        };

        val rr = new VectorBlockRecordIterator(iter) {
            @Override
            public void close() {
            }
        };

        return new RecordReaderResultSetBase(rr) {
            @Override
            public SQLWarning getWarnings() throws SQLException {
                return null;
            }

            @Override
            public void clearWarnings() throws SQLException {

            }

            @Override
            public String getCursorName() throws SQLException {
                return "";
            }

            @Override
            public boolean isBeforeFirst() throws SQLException {
                return false;
            }

            @Override
            public boolean isAfterLast() throws SQLException {
                return false;
            }

            @Override
            public boolean isFirst() throws SQLException {
                return false;
            }

            @Override
            public boolean isLast() throws SQLException {
                return false;
            }

            @Override
            public void beforeFirst() throws SQLException {

            }

            @Override
            public void afterLast() throws SQLException {

            }

            @Override
            public boolean first() throws SQLException {
                return false;
            }

            @Override
            public boolean last() throws SQLException {
                return false;
            }

            @Override
            public int getRow() throws SQLException {
                return 0;
            }

            @Override
            public void setFetchSize(int rows) throws SQLException {

            }

            @Override
            public int getFetchSize() throws SQLException {
                return 0;
            }

            @Override
            public Statement getStatement() throws SQLException {
                return null;
            }

            @Override
            public boolean isClosed() throws SQLException {
                return false;
            }
        };
    }
}
