package io.qpointz.mill.data.backend.rewriters;

import io.substrait.expression.Expression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
public final class TableFacet {

    @Getter
    public final AttributeFacet attributeFacet;

    @Getter
    public final RecordFacet recordFacet;


    public static class TableFacetBuilder {

        public TableFacetBuilder recordFacetExpression(final Expression filterExpression) {
            return this.recordFacet(new RecordFacet(filterExpression));
        }

    }

}
