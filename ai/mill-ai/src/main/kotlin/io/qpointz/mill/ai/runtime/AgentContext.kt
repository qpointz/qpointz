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
 */
data class AgentContext(
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
}
