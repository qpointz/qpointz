package io.qpointz.mill.ai.nlsql.metadata;

import io.qpointz.mill.metadata.domain.FacetPayloadUtils;
import io.qpointz.mill.metadata.domain.MetadataEntityUrn;
import io.qpointz.mill.metadata.domain.MetadataUrns;
import io.qpointz.mill.metadata.domain.facet.FacetAssignment;
import io.qpointz.mill.metadata.repository.FacetRepository;

import java.util.List;
import java.util.Optional;

/**
 * Reads merged global-scope facet payloads for NL-SQL flows without putting facet logic on
 * {@link io.qpointz.mill.metadata.domain.MetadataEntity}.
 */
public final class NlsqlMetadataFacets {

    private NlsqlMetadataFacets() {
    }

    /**
     * @param facetTypeKeyShortOrUrn short key (e.g. {@code descriptive}) or full facet-type URN
     */
    public static <T> Optional<T> readGlobalFacet(
            FacetRepository repo,
            String entityId,
            String facetTypeKeyShortOrUrn,
            Class<T> clazz) {
        String eid = MetadataEntityUrn.canonicalize(entityId);
        String tid = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(facetTypeKeyShortOrUrn));
        String global = MetadataEntityUrn.canonicalize(MetadataUrns.SCOPE_GLOBAL);
        List<FacetAssignment> rows = repo.findByEntityAndType(eid, tid);
        Optional<FacetAssignment> last = Optional.empty();
        for (FacetAssignment f : rows) {
            if (MetadataEntityUrn.canonicalize(f.getScopeKey()).equals(global)) {
                last = Optional.of(f);
            }
        }
        return last.flatMap(f -> FacetPayloadUtils.convert(f.getPayload(), clazz));
    }
}
