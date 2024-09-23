package io.qpointz.mill.service.jdbc.providers;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.service.ExecutionProvider;
import io.qpointz.mill.service.calcite.providers.PlanConverter;
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
            val rs = stmt.executeQuery(statement);
            return new ResultSetVectorBlockIterator(rs, config.getFetchSize());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
