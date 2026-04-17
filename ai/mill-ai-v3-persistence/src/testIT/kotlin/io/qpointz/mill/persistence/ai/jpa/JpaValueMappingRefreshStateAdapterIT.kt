package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshOutcome
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import io.qpointz.mill.ai.valuemap.state.ValueMappingRunState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaValueMappingRefreshStateAdapterIT {

    @Autowired
    private lateinit var repository: ValueMappingRefreshStateRepository

    @Test
    fun shouldRoundTripLifecycleAndStale() {
        val urn = "urn:mill/model/attribute:test.schema.t.col"
        assertThat(repository.findByEntityRes(urn)).isNull()

        repository.markRunBeginning(urn)
        val mid = repository.findByEntityRes(urn)!!
        assertThat(mid.currentState).isEqualTo(ValueMappingRunState.REFRESHING)
        assertThat(mid.refreshProgressPercent).isEqualTo(0)

        repository.updateProgressPercent(urn, 50)
        assertThat(repository.findByEntityRes(urn)!!.refreshProgressPercent).isEqualTo(50)

        val doneAt = Instant.parse("2026-01-01T12:00:00Z")
        repository.markIdleAfterSync(
            entityRes = urn,
            lastRefreshStatus = ValueMappingRefreshOutcome.COMPLETED,
            lastRefreshValuesCount = 12L,
            lastRefreshAt = doneAt,
            statusDetail = null,
            refreshProgressPercent = 100,
            nextScheduledRefreshAt = null,
        )
        val done = repository.findByEntityRes(urn)!!
        assertThat(done.currentState).isEqualTo(ValueMappingRunState.IDLE)
        assertThat(done.lastRefreshStatus).isEqualTo(ValueMappingRefreshOutcome.COMPLETED)
        assertThat(done.lastRefreshValuesCount).isEqualTo(12L)
        assertThat(done.lastRefreshAt).isEqualTo(doneAt)

        repository.markStale(urn, "no such column")
        val stale = repository.findByEntityRes(urn)!!
        assertThat(stale.lastRefreshStatus).isEqualTo(ValueMappingRefreshOutcome.STALE)
        assertThat(stale.statusDetail).contains("column")
        assertThat(stale.currentState).isEqualTo(ValueMappingRunState.IDLE)
    }
}
