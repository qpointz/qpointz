package io.qpointz.delta.lineage.statements;

import io.qpointz.delta.lineage.model.LineageRepository;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;

public interface Statement<T extends SqlNode> {
    SqlKind kind();

    void apply(T statement, LineageRepository repository);

    default void applyNode(SqlNode statement, LineageRepository repository) {
        if (kind()!=statement.getKind()) {
            throw new IllegalArgumentException("Statement kind should be " + statement.getKind().name() + " but is " + kind().name());
        }
        apply((T)statement, repository);
    }

}

