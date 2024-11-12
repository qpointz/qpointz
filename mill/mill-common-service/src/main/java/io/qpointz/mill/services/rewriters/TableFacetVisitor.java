package io.qpointz.mill.services.rewriters;

import io.qpointz.mill.MillException;
import io.substrait.extension.SimpleExtension;
import io.substrait.relation.*;
import io.substrait.relation.physical.HashJoin;
import io.substrait.relation.physical.MergeJoin;
import io.substrait.relation.physical.NestedLoopJoin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.management.relation.Relation;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Builder
@Slf4j
public class TableFacetVisitor extends RelCopyOnWriteVisitor<MillException> {

    @Getter
    private final TableFacetsCollection facets;

    @Getter
    private final SimpleExtension.ExtensionCollection extensionCollection;

    @Override
    public Optional<Rel> visit(NamedScan namedScan) throws MillException {
        val tableFacet = this.getFacets().getOrDefault(namedScan.getNames(), null);
        if (tableFacet == null || tableFacet.recordFacet == null || tableFacet.recordFacet.getExpression() == null) {
            return Optional.of(namedScan);
        }

        log.trace("Applying facet on {}", namedScan.getNames());

        val builder = new io.substrait.dsl.SubstraitBuilder(this.extensionCollection);

        return Optional.of(builder
                .filter(r-> tableFacet.recordFacet.getExpression(), namedScan));
    }

}