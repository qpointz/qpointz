package io.qpointz.mill;

import io.qpointz.mill.sql.RecordReaderResultSetBase;
import io.qpointz.mill.sql.VectorBlockRecordIterator;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

class MillRecordReaderResultSet extends RecordReaderResultSetBase {

    public MillRecordReaderResultSet(VectorBlockRecordIterator recordIterator) throws SQLException {
        super(recordIterator);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
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
        throw new UnsupportedOperationException("Non scrollable dataset");
    }

    @Override
    public void afterLast() throws SQLException {
        throw new UnsupportedOperationException("Non scrollable dataset");
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
        throw new UnsupportedOperationException("Fetch size cant be set");
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

}
