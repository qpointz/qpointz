package io.qpointz.mill.ai.nlsql.metadata;

import io.qpointz.mill.data.schema.MetadataEntityUrnCodec;
import io.qpointz.mill.metadata.repository.FacetReadSide;
import io.qpointz.mill.metadata.service.MetadataEntityService;

/**
 * Bundles read dependencies needed to build NL-to-SQL schema prompts: entity listing by kind,
 * relational coordinates from entity URNs, and global-scope facet payloads.
 *
 * @param metadataEntityService entity read API (core)
 * @param facetReadSide facet assignment reads
 * @param urnCodec        decodes relational entity ids to catalog coordinates
 */
public record SchemaMessageMetadataPorts(
        MetadataEntityService metadataEntityService,
        FacetReadSide facetReadSide,
        MetadataEntityUrnCodec urnCodec) {
}
