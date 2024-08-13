package io.qpointz.mill;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.sql.RecordReaderResultSetBase;
import io.qpointz.mill.sql.VectorBlockRecordIterator;
import lombok.*;

import java.sql.*;
import java.sql.Statement;
import java.util.Iterator;


public class MillStatement implements Statement {

    public MillStatement(MillConnection connection) {
        this.connection = connection;
    }

    @Getter
    private final MillConnection connection;

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        val client = this.connection.createClient();
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

    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {

    }

    @Getter
    @Setter
    private int maxRows = 0;


    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Getter
    @Setter
    private int queryTimeout = 0;

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Getter
    @Setter
    private int fetchSize = 10000;


    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {

    }

    @Override
    public void clearBatch() throws SQLException {

    }

    @Override
    public int[] executeBatch() throws SQLException {
        return new int[0];
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
