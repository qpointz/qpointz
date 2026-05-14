package io.qpointz.mill.cloud.azure.blob

import io.qpointz.mill.source.verify.Phase
import io.qpointz.mill.source.verify.Severity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AdlsStorageDescriptorTest {

    @Nested
    inner class EndpointValidation {

        @Test
        fun shouldReportError_whenEndpointAndConnectionStringBothMissing() {
            val desc = AdlsStorageDescriptor(endpoint = "", container = "data")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("endpoint") && it.message.contains("connectionString") })
        }

        @Test
        fun shouldPass_whenConnectionStringOnlyWithoutEndpoint() {
            val desc = AdlsStorageDescriptor(
                endpoint = "",
                container = "data",
                connectionString = "DefaultEndpointsProtocol=https;AccountName=x;AccountKey=y;EndpointSuffix=core.windows.net"
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldPass_whenEndpointPresent() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data"
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }
    }

    @Nested
    inner class ContainerValidation {

        @Test
        fun shouldReportError_whenContainerIsBlank() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = ""
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("container") })
        }
    }

    @Nested
    inner class AuthValidation {

        @Test
        fun shouldReportError_whenConnectionStringAndAccountKeyBothProvided() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data",
                connectionString = "DefaultEndpointsProtocol=https;...",
                auth = AdlsAuthDescriptor(accountKey = "k")
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("connectionString") && it.message.contains("accountKey") })
        }

        @Test
        fun shouldReportError_whenOnlyAccountNameProvided() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data",
                auth = AdlsAuthDescriptor(accountName = "myaccount")
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("accountKey") })
        }

        @Test
        fun shouldPass_whenAccountNameAndKeyPaired() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data",
                auth = AdlsAuthDescriptor(accountName = "myaccount", accountKey = "mykey")
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldPass_whenAccountKeyOnlyAndDerivableEndpoint() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data",
                auth = AdlsAuthDescriptor(accountKey = "mykey")
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldReportError_whenAccountKeyOnlyAndNonDerivableEndpoint() {
            val desc = AdlsStorageDescriptor(
                endpoint = "http://127.0.0.1:10000/devstoreaccount1",
                container = "data",
                auth = AdlsAuthDescriptor(accountKey = "mykey")
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("accountName") })
        }

        @Test
        fun shouldReportError_whenAccountKeyAndSasTogether() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data",
                auth = AdlsAuthDescriptor(accountKey = "k", sasToken = "?sv=1&sig=x")
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("accountKey") && it.message.contains("sasToken") })
        }

        @Test
        fun shouldPass_whenSasTokenWithEndpoint() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data",
                auth = AdlsAuthDescriptor(sasToken = "sv=2021-06-08&sig=fake")
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldReportError_whenPreferAmbientWithExplicitCredentials() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data",
                auth = AdlsAuthDescriptor(preferAmbientCredentials = true, accountKey = "k")
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("preferAmbientCredentials") })
        }

        @Test
        fun shouldPass_whenPreferAmbientWithTopLevelConnectionString() {
            val desc = AdlsStorageDescriptor(
                endpoint = "",
                container = "c",
                connectionString = "DefaultEndpointsProtocol=https;AccountName=x;AccountKey=y;EndpointSuffix=core.windows.net",
                auth = AdlsAuthDescriptor(preferAmbientCredentials = true)
            )
            assertTrue(desc.verify().isValid)
        }

        @Test
        fun shouldPass_whenNoAuthProvided() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data"
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldPass_whenAuthIsAllBlank() {
            val desc = AdlsStorageDescriptor(
                endpoint = "https://myaccount.blob.core.windows.net",
                container = "data",
                auth = AdlsAuthDescriptor()
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }
    }

    @Nested
    inner class IssueMetadata {

        @Test
        fun shouldUseDescriptorPhase() {
            val desc = AdlsStorageDescriptor(endpoint = "", container = "")
            val report = desc.verify()
            assertTrue(report.errors.all { it.phase == Phase.DESCRIPTOR })
        }

        @Test
        fun shouldUseErrorSeverity() {
            val desc = AdlsStorageDescriptor(endpoint = "", container = "")
            val report = desc.verify()
            assertTrue(report.issues.all { it.severity == Severity.ERROR })
        }
    }
}
