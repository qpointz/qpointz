package io.qpointz.mill.ai.runtime

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

/**
 * Metadata scope row on [AgentContext] for `list_metadata_scopes` and capture stamping.
 *
 * @property scopeUrn full scope URN (e.g. `urn:mill/metadata/scope:global`)
 * @property access `r`, `w`, or `rw`
 * @property label optional display label for tools
 */
data class AgentContextScope(
  val scopeUrn: String,
  val access: String,
  val label: String? = null,
)

/**
 * Runtime context passed into capability/profile resolution.
 *
 * The initial hello-world flow uses only the coarse context type, but the same model leaves
 * room for future focused agents bound to a table, concept, or analysis object.
 *
 * **Glossary (disambiguation):**
 * - [contextType] — chat binding category for capability provider filtering (e.g. `"general"`, `"model"`).
 *   Not a metadata scope URN and not a facet category slug.
 * - [focusEntityType] / [focusEntityId] — optional UI focus object (e.g. model entity type + id).
 * - [scopes] — metadata scope rows (`scopeUrn`, `access`) for QUERY reads and capture stamping;
 *   distinct from [contextType] and from facet category slugs like `general` in the catalog.
 *
 * @property contextType chat binding category for [io.qpointz.mill.ai.core.capability.CapabilityRegistry]
 *   provider selection; defaults to `"general"` when rehydrated from an unbound chat.
 * @property focusEntityType optional focused entity kind from chat metadata (e.g. model entity type).
 * @property focusEntityId optional focused entity id from chat metadata.
 * @property chatId conversation id; drives default chat metadata scopes when present.
 * @property scopes metadata scope rows visible to metadata tools in this turn.
 * @property capabilityDependencies assembled ports/services for schema, SQL, and metadata capabilities.
 */
data class AgentContext(
  /** Chat binding category — not a metadata scope URN or facet category slug. */
  val contextType: String,
  val focusEntityType: String? = null,
  val focusEntityId: String? = null,
  val chatId: String? = null,
  val scopes: List<AgentContextScope> = emptyList(),
  val capabilityDependencies: CapabilityDependencyContainer = CapabilityDependencyContainer.empty(),
) {
  /** Scope URNs with write access (`w` or `rw`) for facet capture stamping. */
  fun writeScopeUrns(): List<String> =
    scopes
      .filter { it.access.equals("w", ignoreCase = true) || it.access.equals("rw", ignoreCase = true) }
      .map { it.scopeUrn }

  /**
   * Comma-separated scope list for metadata QUERY reads (`list_entity_facets`).
   * Includes scopes with `r`, `w`, or `rw` access from [scopes].
   */
  fun readableScopesParam(): String? {
    val readable = scopes
      .filter {
        it.access.equals("r", ignoreCase = true) ||
          it.access.equals("w", ignoreCase = true) ||
          it.access.equals("rw", ignoreCase = true)
      }
      .map { it.scopeUrn }
    return readable.takeIf { it.isNotEmpty() }?.joinToString(",")
  }
}
