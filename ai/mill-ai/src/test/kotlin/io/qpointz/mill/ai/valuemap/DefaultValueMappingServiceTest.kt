package io.qpointz.mill.ai.valuemap

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DefaultValueMappingServiceTest {

    private val sync: VectorMappingSynchronizer = mock()

    @Test
    fun `syncFromSource dedupes last wins on content`() {
        val svc = DefaultValueMappingService(sync, 2048)
        val source = StaticListValueSource("a" to "1", "a" to "2")
        svc.syncFromSource("urn:t", source, 1L, null)
        val captor = argumentCaptor<List<AttributeValueEntry>>()
        verify(sync).sync(eq("urn:t"), captor.capture(), eq(1L), isNull())
        assertThat(captor.firstValue).hasSize(1)
        assertThat(captor.firstValue[0].metadata["value"]).isEqualTo("2")
    }

    @Test
    fun `syncFromSource invokes progress before sync`() {
        val svc = DefaultValueMappingService(sync, 2048)
        val progress = mock<ValueMappingSyncProgressCallback>()
        svc.syncFromSource("urn:t", StaticListValueSource("x" to "X"), 1L, progress)
        val order = org.mockito.Mockito.inOrder(progress, sync)
        order.verify(progress).onBegin("urn:t", 1)
        verify(sync).sync(eq("urn:t"), org.mockito.kotlin.any(), eq(1L), eq(progress))
    }
}
