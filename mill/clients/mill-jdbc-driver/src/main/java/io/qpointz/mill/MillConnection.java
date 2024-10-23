package io.qpointz.mill;

import io.qpointz.mill.client.MillClient;
import io.qpointz.mill.client.MillClientConfiguration;
import io.qpointz.mill.client.MillSqlQuery;
import io.qpointz.mill.proto.HandshakeRequest;
import lombok.Getter;
import lombok.extern.java.Log;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

@Log
public class MillConnection implements java.sql.Connection {

    private final MillClientConfiguration config;

    private boolean isClosed = false;

    public MillConnection(MillClientConfiguration config) {
        this.config = config;
    }

    @Getter(lazy = true)
    private final MillClient client = createClient();
    private MillClient createClient() {
        return MillClient.fromConfig(this.config);
    }

    public String getUrl() {
        return this.getClient().getClientUrl();
    }

    private CallableStatement createStatement(String sql, int[] columnIndexes, String[] columnNames ) throws SQLException {
        val query = MillSqlQuery.builder()
                .connection(this);
        if (sql != null && !sql.isEmpty()) {
            query.sql(sql);
        }

        query.selectedIndexed(columnIndexes)
                .selectedNamesArray(columnNames);

        return new MillCallableStatement(query);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new MillCallableStatement(this);
    }



    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return this.createStatement(sql, null, null);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return this.createStatement(sql, null, null);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return sql;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return true;
    }

    @Override
    public void commit() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void rollback() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void close() throws SQLException {
        this.isClosed = true;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return new MillDatabaseMetadata(this);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return true;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        throw new SQLFeatureNotSupportedException("Change catalog is not supported");
    }

    @Override
    public String getCatalog() throws SQLException {
        return null;
    }


    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        if (level!=Connection.TRANSACTION_NONE) {
            throw new SQLFeatureNotSupportedException("Transaction isolation level not supported");
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_NONE;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        validateResultSetConcurency(resultSetConcurrency);
        return new MillCallableStatement(this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        validateResultSetConcurency(resultSetConcurrency);
        validateResultSetType(resultSetType);
        return this.createStatement(sql, null, null);
    }

    private void validateResultSetType(int resultSetType) throws SQLFeatureNotSupportedException {
        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLFeatureNotSupportedException("Resultset supports only 'TYPE_FORWARD_ONLY' type");
        }
    }

    private void validateResultSetConcurency(int resultSetConcurrency) throws SQLFeatureNotSupportedException {
        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
            throw new SQLFeatureNotSupportedException("Resultset supports only 'CONCUR_READ_ONLY' concurrency");
        }
    }

    private void validateResultSetHoldability(int holdability) throws SQLFeatureNotSupportedException {
        if (holdability!=ResultSet.HOLD_CURSORS_OVER_COMMIT) {
            throw new SQLFeatureNotSupportedException("Resultset supports only 'HOLD_CURSORS_OVER_COMMIT' holdability");
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new MillCallableStatement(this, sql);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return Map.of();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("Type map is not supported");
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        validateResultSetHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoint is not supported");
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoint is not supported");
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        log.info("Transactions not supported:Ignore Rollback");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        log.info("Transactions not supported:Ignore 'release savepoint'");
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        validateResultSetConcurency(resultSetConcurrency);
        validateResultSetHoldability(resultSetHoldability);
        validateResultSetType(resultSetType);
        return new MillCallableStatement(this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return prepareStatementImpl(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return prepareStatementImpl(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @NotNull
    private CallableStatement prepareStatementImpl(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        validateResultSetConcurency(resultSetConcurrency);
        validateResultSetHoldability(resultSetHoldability);
        validateResultSetType(resultSetType);
        return this.createStatement(sql, null, null);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return this.createStatement(sql, null, null);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return this.createStatement(sql, columnIndexes, null);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return this.createStatement(sql, null, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("Clob is not supported");
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLFeatureNotSupportedException("Blob is not supported");
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob is not supported");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException("SQLXML is not supported");
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        try {
            val resp = this.createClient().handshake(HandshakeRequest.getDefaultInstance());
            return resp!=null;
        } catch (Exception exception) {
            log.warning(exception.getMessage());
            return false;
        }
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        throw new SQLClientInfoException("Feature not supported", Map.of());
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        throw new SQLClientInfoException("Feature not supported", Map.of());
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLFeatureNotSupportedException("Array is not supported");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLFeatureNotSupportedException("Struct is not supported");
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        throw new SQLFeatureNotSupportedException("Changing schema not suported");
    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob is not supported");
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throw new SQLFeatureNotSupportedException("Network Timeout is not supported");
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException("Network Timeout is not supported");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return iface.cast(this);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(MillConnection.class);
    }
}
