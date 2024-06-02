package io.qpointz.delta.calcite.providers;

import io.qpointz.delta.proto.QueryExecutionConfig;
import io.qpointz.delta.service.ExecutionProvider;
import io.qpointz.delta.sql.VectorBlockIterator;
import io.qpointz.delta.sql.VectorBlockIterators;
import io.substrait.extension.ImmutableSimpleExtension;
import io.substrait.plan.Plan;
import io.substrait.plan.ProtoPlanConverter;
import lombok.*;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.tools.RelRunner;

import java.io.IOException;
import java.sql.SQLException;

@AllArgsConstructor
public class CalciteExecutionProvider implements ExecutionProvider {

    @Getter(AccessLevel.PROTECTED)
    private CalciteConnection calciteConnection;

    public VectorBlockIterator execute(Plan plan, QueryExecutionConfig config) {
        val extensions = ImmutableSimpleExtension.ExtensionCollection.builder().build();
        val a = new io.substrait.isthmus.SubstraitToCalcite(extensions, this.calciteConnection.getTypeFactory());
        try {
            val node = a.convert(plan.getRoots().get(0).getInput());
            val relRunner = this.getCalciteConnection().unwrap(RelRunner.class);
            val stmt = relRunner.prepareStatement(node);
            val resultSet = stmt.executeQuery();
            return VectorBlockIterators.fromResultSet(resultSet, config.getBatchSize());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
