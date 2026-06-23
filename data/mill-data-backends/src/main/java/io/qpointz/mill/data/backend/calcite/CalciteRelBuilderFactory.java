package io.qpointz.mill.data.backend.calcite;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.tools.RelBuilder;

import java.util.function.Function;

/**
 * Calcite-backed {@link RelBuilderFactory} using an injected {@link CalciteContextFactory}.
 */
@RequiredArgsConstructor
public class CalciteRelBuilderFactory implements RelBuilderFactory {

    private final CalciteContextFactory calciteContextFactory;

    @Override
    public <T> T withRelBuilder(Function<RelBuilder, T> action) {
        try (CalciteContext ctx = calciteContextFactory.createContext()) {
            RelBuilder builder = RelBuilder.create(ctx.getFrameworkConfig());
            return action.apply(builder);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to run RelBuilder action", e);
        }
    }
}
