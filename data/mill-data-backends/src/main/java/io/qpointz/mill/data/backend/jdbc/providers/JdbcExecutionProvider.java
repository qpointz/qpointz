package io.qpointz.mill.data.backend.jdbc.providers;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.data.backend.ExecutionProvider;
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.qpointz.mill.vectors.sql.ResultSetVectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.AllArgsConstructor;
import lombok.val;

import java.sql.SQLException;

@AllArgsConstructor
public class JdbcExecutionProvider implements ExecutionProvider {


    private final PlanConverter planConverter;

    private final JdbcContextFactory ctxFactory;


    @Override
    public VectorBlockIterator execute(Plan plan, QueryExecutionConfig config) {
        try {
            val statement = planConverter.toSql(plan);
            val ctx = ctxFactory.createContext();
            val con = ctx.getConnection();
            val stmt = con.createStatement();
            val rs = stmt.executeQuery(statement.sql());
            return new ResultSetVectorBlockIterator(rs, config.getFetchSize(), statement.names());
        } catch (SQLException e) {
            throw new MillRuntimeException("Jdbc Executor failed execution:", e);
        }
    }

}
