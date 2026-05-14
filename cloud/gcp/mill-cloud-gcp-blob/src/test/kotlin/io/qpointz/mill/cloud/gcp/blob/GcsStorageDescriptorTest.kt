package io.qpointz.mill.cloud.gcp.blob

import io.qpointz.mill.source.verify.Phase
import io.qpointz.mill.source.verify.Severity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GcsStorageDescriptorTest {

    @Nested
    inner class BucketValidation {

        @Test
        fun shouldReportError_whenBucketIsBlank() {
            val desc = GcsStorageDescriptor(bucket = "  ")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertEquals(1, report.errors.size)
            assertTrue(report.errors[0].message.contains("bucket"))
            assertEquals(Phase.DESCRIPTOR, report.errors[0].phase)
        }

        @Test
        fun shouldBeValid_whenBucketIsProvided() {
            val desc = GcsStorageDescriptor(bucket = "my-bucket")
            val report = desc.verify()
            assertTrue(report.isValid)
            assertTrue(report.errors.isEmpty())
        }
    }

    @Nested
    inner class AuthValidation {

        @Test
        fun shouldBeValid_whenAuthIsNull() {
            val desc = GcsStorageDescriptor(bucket = "b", auth = null)
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldBeValid_whenAuthIsAllBlank() {
            val desc = GcsStorageDescriptor(
                bucket = "b",
                auth = GcsAuthDescriptor()
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldBeValid_whenSingleDelegatedBundleIsSet() {
            val desc = GcsStorageDescriptor(
                bucket = "b",
                auth = GcsAuthDescriptor(accessToken = "tok")
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldReportError_whenMultipleDelegatedBundlesAreSet() {
            val desc = GcsStorageDescriptor(
                bucket = "b",
                auth = GcsAuthDescriptor(
                    accessToken = "tok",
                    serviceAccountJson = "{}"
                )
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertEquals(1, report.errors.size)
            assertTrue(report.errors[0].message.contains("delegated"))
        }

        @Test
        fun shouldReportError_whenServiceAccountJsonLooksLikeFilePath() {
            val desc = GcsStorageDescriptor(
                bucket = "b",
                auth = GcsAuthDescriptor(serviceAccountJson = "/home/user/sa.json")
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("serviceAccountJsonPath") })
        }

        @Test
        fun shouldReportError_whenAllThreeDelegatedBundlesAreSet() {
            val desc = GcsStorageDescriptor(
                bucket = "b",
                auth = GcsAuthDescriptor(
                    accessToken = "tok",
                    serviceAccountJson = "{}",
                    serviceAccountJsonPath = "/path/to/sa.json"
                )
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            val error = report.errors.single()
            assertEquals(Severity.ERROR, error.severity)
            assertEquals(Phase.DESCRIPTOR, error.phase)
        }
    }

    @Nested
    inner class OptionalFields {

        @Test
        fun shouldDefaultPrefixToNull() {
            val desc = GcsStorageDescriptor(bucket = "b")
            assertNull(desc.prefix)
        }

        @Test
        fun shouldDefaultProjectIdToNull() {
            val desc = GcsStorageDescriptor(bucket = "b")
            assertNull(desc.projectId)
        }

        @Test
        fun shouldDefaultEndpointToNull() {
            val desc = GcsStorageDescriptor(bucket = "b")
            assertNull(desc.endpoint)
        }

        @Test
        fun shouldDefaultAuthToNull() {
            val desc = GcsStorageDescriptor(bucket = "b")
            assertNull(desc.auth)
        }
    }
}
