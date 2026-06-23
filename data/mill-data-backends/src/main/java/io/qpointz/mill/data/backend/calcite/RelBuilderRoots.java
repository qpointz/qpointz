package io.qpointz.mill.data.backend.calcite;

import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.tools.RelBuilder;

/**
 * Helpers for turning {@link RelBuilder} stacks into {@link RelRoot} on Calcite 1.41+.
 */
public final class RelBuilderRoots {

    private RelBuilderRoots() {
    }

    /**
     * @param builder relational builder with a single plan on its stack
     * @return query root suitable for Substrait conversion
     */
    public static RelRoot toRoot(RelBuilder builder) {
        return RelRoot.of(builder.build(), SqlKind.SELECT);
    }
}
