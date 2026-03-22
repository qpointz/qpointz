package io.qpointz.mill.metadata.api.dto

/**
 * REST DTO summarising the result of a metadata import operation.
 *
 * Returned by `POST /api/v1/metadata/import`.
 *
 * @property entitiesImported   number of entities saved (inserted or updated)
 * @property facetTypesImported number of custom facet types registered from the `facet-types:`
 *                              section of the imported YAML
 * @property errors             list of non-fatal per-entity error messages; the import
 *                              continues after each error rather than aborting
 */
data class ImportResultDto(
    val entitiesImported: Int,
    val facetTypesImported: Int,
    val errors: List<String>
)
