package io.qpointz.mill.lineage.statements;

import io.qpointz.mill.lineage.model.LineageRepository;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;

import java.util.Map;

public final class Statements {

    private static Map<SqlKind, Statement<?>> statements = Map.of(
            CreateTable.INSTANCE.kind(), CreateTable.INSTANCE,
            SelectStatement.INSTANCE.kind(), SelectStatement.INSTANCE
    );

    public static Statement<?> getBySqlKind(SqlKind kind) {
        return statements.get(kind);
    }

    public static Statement<?> getBySqlKind(SqlNode node) {
        return statements.get(node.getKind());
    }

    public static void apply(SqlNode node, LineageRepository repository) {
        getBySqlKindOrNope(node)
                .applyNode(node, repository);
    }

    private static Statement<?> getBySqlKindOrNope(SqlNode node) {
        return statements.getOrDefault(node.getKind(), NoopStatement.createNopeStatement(node));
    }


}
