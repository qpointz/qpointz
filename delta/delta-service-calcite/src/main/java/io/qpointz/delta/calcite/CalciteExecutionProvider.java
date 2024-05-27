package io.qpointz.delta.calcite;

import io.qpointz.delta.proto.PlanStatement;
import io.qpointz.delta.proto.PreparedStatement;
import io.qpointz.delta.proto.SQLStatement;
import io.qpointz.delta.proto.VectorBlock;
import io.qpointz.delta.service.ExecutionProvider;
import io.qpointz.delta.service.SqlExecutionProvider;
import io.qpointz.delta.service.SubstraitExecutionProvider;
import io.qpointz.delta.sql.BlockReader;
import io.substrait.extension.ExtensionCollector;
import io.substrait.extension.ImmutableSimpleExtension;
import io.substrait.extension.SimpleExtension;
import io.substrait.isthmus.SubstraitToCalcite;
import io.substrait.isthmus.SubstraitToSql;
import io.substrait.plan.ProtoPlanConverter;
import io.substrait.relation.Rel;
import lombok.*;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.tools.RelRunner;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

@AllArgsConstructor
public class CalciteExecutionProvider  implements SubstraitExecutionProvider, ExecutionProvider, SqlExecutionProvider  {

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
    public Iterator<VectorBlock> execute(SQLStatement sql, int batchSize) throws SQLException {
        return executeSql(sql.getStatement(), batchSize);
    }

    private BlockReader executeSql(String sql, int batchSize) throws SQLException {
        val stmt = this.getCalciteConnection().createStatement();
        val resultSet = stmt.executeQuery(sql);
        return new BlockReader(resultSet, batchSize);
    }

    @SneakyThrows
    @Override
    public Iterator<VectorBlock> execute(PlanStatement plan, int batchSize) throws SQLException {
        val extensions = ImmutableSimpleExtension.ExtensionCollection.builder().build();
        val a = new io.substrait.isthmus.SubstraitToCalcite(extensions, this.calciteConnection.getTypeFactory());
        val c = new ProtoPlanConverter();
        val p = c.from(plan.getPlan());
        val node = a.convert(p.getRoots().get(0).getInput());
        /*val relRunner = this.getCalciteConnection().unwrap(RelRunner.class);
        val stmt = relRunner.prepareStatement(node);
        val resultSet = stmt.executeQuery();
        return new BlockReader(resultSet, batchSize);*/
        val sql = io.substrait.isthmus.SubstraitToSql.toSql(node);
        return executeSql(sql, batchSize);
    }
}
