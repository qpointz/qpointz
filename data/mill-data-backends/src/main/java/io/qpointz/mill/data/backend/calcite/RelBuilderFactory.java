package io.qpointz.mill.data.backend.calcite;

import org.apache.calcite.tools.RelBuilder;

import java.util.function.Function;

/**
 * Supplies catalog-bound {@link RelBuilder} instances for RelNode composers (OData, future APIs).
 */
public interface RelBuilderFactory {

    /**
     * Runs {@code action} with a catalog-bound {@link RelBuilder}; the underlying Calcite context
     * remains open for the duration of the callback.
     *
     * @param action composer callback
     * @param <T> result type
     * @return callback result
     */
    <T> T withRelBuilder(Function<RelBuilder, T> action);
}
