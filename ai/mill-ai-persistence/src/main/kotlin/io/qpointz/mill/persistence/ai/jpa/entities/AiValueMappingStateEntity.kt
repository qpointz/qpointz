package io.qpointz.mill.persistence.ai.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * JPA row for `ai_value_mapping_state` (WI-184).
 */
@Entity
@Table(name = "ai_value_mapping_state")
class AiValueMappingStateEntity(
    @Id
    @Column(name = "entity_res", nullable = false, length = 512)
    var entityRes: String,

    @Column(name = "last_refresh_at")
    var lastRefreshAt: Instant? = null,

    @Column(name = "next_scheduled_refresh_at")
    var nextScheduledRefreshAt: Instant? = null,

    /**
     * **Locked:** **`BIGINT` nullable.** Semantics: count of **value elements that completed successfully**
     * (embed + persist per § *Integration* — **`onElementProcessed`** success) in the **last completed**
     * **`syncFromSource`** run for this **`entity_res`**. **Excludes** elements that failed or threw.
     * **Set to `null`** if no run completed cleanly (e.g. only **`STALE`** skip, or catastrophic abort before **`onRunComplete`**).
     * **Set to `0`** if a run completed but **zero** successes. JPA field KDoc copies this paragraph **verbatim** — **no**
     * further interpretation.
     */
    @Column(name = "last_refresh_values_count")
    var lastRefreshValuesCount: Long? = null,

    @Column(name = "last_refresh_status", nullable = false, length = 32)
    var lastRefreshStatus: String = "FAILED",

    @Column(name = "current_state", nullable = false, length = 32)
    var currentState: String = "IDLE",

    @Column(name = "refresh_progress_percent")
    var refreshProgressPercent: Int? = null,

    @Column(name = "status_detail", columnDefinition = "TEXT")
    var statusDetail: String? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
