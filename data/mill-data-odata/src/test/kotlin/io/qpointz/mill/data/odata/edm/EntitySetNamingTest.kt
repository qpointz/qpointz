package io.qpointz.mill.data.odata.edm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EntitySetNamingTest {

    @Test
    fun shouldExtractSchemaFromServiceRoot() {
        assertThat(
            EntitySetNaming.extractSchemaFromServiceRoot("http://localhost:8080/services/odata/skymill.svc"),
        ).isEqualTo("skymill")
    }

    @Test
    fun shouldExtractSchemaFromServiceRootWithTrailingSlash() {
        assertThat(
            EntitySetNaming.extractSchemaFromServiceRoot("http://localhost/services/odata/skymill_ref.svc/"),
        ).isEqualTo("skymill_ref")
    }

    @Test
    fun shouldExtractSchemaFromEncodedServiceRoot() {
        assertThat(
            EntitySetNaming.extractSchemaFromServiceRoot("http://localhost/services/odata/skymill%5Fref.svc"),
        ).isEqualTo("skymill_ref")
    }

    @Test
    fun shouldReturnNullWhenServiceRootMissingSvcMarker() {
        assertThat(
            EntitySetNaming.extractSchemaFromServiceRoot("http://localhost/services/odata/skymill"),
        ).isNull()
    }

    @Test
    fun shouldReturnNullWhenSchemaSegmentContainsTraversal() {
        assertThat(
            EntitySetNaming.extractSchemaFromServiceRoot("http://localhost/services/odata/../evil.svc"),
        ).isNull()
    }
}
