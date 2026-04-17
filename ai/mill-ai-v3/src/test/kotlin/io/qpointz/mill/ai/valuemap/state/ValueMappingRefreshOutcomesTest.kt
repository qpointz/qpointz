package io.qpointz.mill.ai.valuemap.state

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValueMappingRefreshOutcomesTest {

    @Test
    fun `completed when all succeed`() {
        assertThat(ValueMappingRefreshOutcomes.fromSuccessFailure(3, 0))
            .isEqualTo(ValueMappingRefreshOutcome.COMPLETED)
    }

    @Test
    fun `failed when all fail`() {
        assertThat(ValueMappingRefreshOutcomes.fromSuccessFailure(0, 2))
            .isEqualTo(ValueMappingRefreshOutcome.FAILED)
    }

    @Test
    fun `partial when mixed`() {
        assertThat(ValueMappingRefreshOutcomes.fromSuccessFailure(1, 1))
            .isEqualTo(ValueMappingRefreshOutcome.PARTIAL)
    }

    @Test
    fun `empty run is completed`() {
        assertThat(ValueMappingRefreshOutcomes.fromSuccessFailure(0, 0))
            .isEqualTo(ValueMappingRefreshOutcome.COMPLETED)
    }
}
