package io.qpointz.delta.calcite;

import io.qpointz.delta.proto.PreparedStatement;
import io.qpointz.delta.proto.SQLStatement;
import io.qpointz.delta.proto.VectorBlock;
import io.qpointz.delta.service.ExecutionProvider;
import io.qpointz.delta.service.SqlExecutionProvider;
import io.qpointz.delta.service.SubstraitExecutionProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;

import java.sql.SQLException;
import java.util.Iterator;

@AllArgsConstructor
public class CalciteExecutionProvider extends SubstraitExecutionProvider implements ExecutionProvider, SqlExecutionProvider  {

    @Getter(AccessLevel.PROTECTED)
    private CalciteConnection calciteConnection;

    @Override
    public SqlExecutionProvider getSqlExecutionProvider() {
        return this;
    }

    @Override
    public SubstraitExecutionProvider getSubstraitExecutionProvider() {
        return this;
    }

    @Override
    public Iterator<VectorBlock> execute(PreparedStatement statement, int batchSize) {
        try {
            if (statement.hasSql()) {
                    return executeSql(statement.getSql(), batchSize);
            }

            throw new RuntimeException("Unsuported prepared statement");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Iterator<VectorBlock> executeSql(SQLStatement sql, int batchSize) throws SQLException {
        val stmt = this.getCalciteConnection().createStatement();
        val resultSet = stmt.executeQuery(sql.getStatement());
        throw new RuntimeException("Not implemented yet");
    }
}
