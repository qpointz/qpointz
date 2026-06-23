package io.qpointz.mill.data.odata.render

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ODataJsonFeedSerializerTest {
    private val serializer = ODataJsonFeedSerializer()

    @Test
    fun shouldSerializeFeedWithContextAndValueArray() {
        val json = serializer.serializeFeed(
            rows = listOf(mapOf("id" to 1, "city" to "Paris")),
            entitySetName = "cities",
            serviceRoot = "http://localhost/services/odata/skymill.svc",
        )
        assertThat(json).contains("\"@odata.context\"")
        assertThat(json).contains("http://localhost/services/odata/skymill.svc/\$metadata#cities")
        assertThat(json).contains("\"value\"")
        assertThat(json).contains("Paris")
    }
}
