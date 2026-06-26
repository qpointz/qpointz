package io.qpointz.mill.ai.capabilities.metadata

/**
 * Facet category index row for `list_facet_categories` — catalog category plus optional guidance.
 *
 * @property category category slug (e.g. `general`, `relation`, `data-quality`)
 * @property title optional display title from guidance content
 * @property description optional narrative from guidance content
 * @property guidance parsed `facet-type-category` body when present
 */
data class FacetCategoryWire(
  val category: String,
  val title: String? = null,
  val description: String? = null,
  val guidance: Map<String, Any?>? = null,
)
