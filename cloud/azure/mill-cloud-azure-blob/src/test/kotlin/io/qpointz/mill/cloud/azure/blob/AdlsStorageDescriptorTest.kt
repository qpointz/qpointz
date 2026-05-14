package io.qpointz.mill.cloud.azure.blob

import io.qpointz.mill.source.verify.Phase
import io.qpointz.mill.source.verify.Severity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AdlsStorageDescriptorTest {

    @Nested
    inner class AccountUrlValidation {

        @Test
        fun shouldReportError_whenAccountUrlIsBlank() {
            val desc = AdlsStorageDescriptor(accountUrl = "", filesystem = "data")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("accountUrl") })
        }

        @Test
        fun shouldPass_whenAccountUrlIsPresent() {
            val desc = AdlsStorageDescriptor(
                accountUrl = "https://myaccount.blob.core.windows.net",
                filesystem = "data"
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }
    }

    @Nested
    inner class FilesystemValidation {

        @Test
        fun shouldReportError_whenFilesystemIsBlank() {
            val desc = AdlsStorageDescriptor(
                accountUrl = "https://myaccount.blob.core.windows.net",
                filesystem = ""
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("filesystem") })
        }
    }

    @Nested
    inner class AuthValidation {

        @Test
        fun shouldReportError_whenBothConnectionStringAndAccountNameProvided() {
            val desc = AdlsStorageDescriptor(
                accountUrl = "https://myaccount.blob.core.windows.net",
                filesystem = "data",
                auth = AdlsAuthDescriptor(
                    connectionString = "DefaultEndpointsProtocol=https;...",
                    accountName = "myaccount",
                    accountKey = "mykey"
                )
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("Mutually exclusive") })
        }

        @Test
        fun shouldReportError_whenOnlyAccountNameProvided() {
            val desc = AdlsStorageDescriptor(
                accountUrl = "https://myaccount.blob.core.windows.net",
                filesystem = "data",
                auth = AdlsAuthDescriptor(accountName = "myaccount")
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("together") })
        }

        @Test
        fun shouldReportError_whenOnlyAccountKeyProvided() {
            val desc = AdlsStorageDescriptor(
                accountUrl = "https://myaccount.blob.core.windows.net",
                filesystem = "data",
                auth = AdlsAuthDescriptor(accountKey = "mykey")
            )
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("together") })
        }

        @Test
        fun shouldPass_whenConnectionStringAlone() {
            val desc = AdlsStorageDescriptor(
                accountUrl = "https://myaccount.blob.core.windows.net",
                filesystem = "data",
                auth = AdlsAuthDescriptor(connectionString = "DefaultEndpointsProtocol=https;...")
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldPass_whenAccountNameAndKeyPaired() {
            val desc = AdlsStorageDescriptor(
                accountUrl = "https://myaccount.blob.core.windows.net",
                filesystem = "data",
                auth = AdlsAuthDescriptor(accountName = "myaccount", accountKey = "mykey")
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldPass_whenNoAuthProvided() {
            val desc = AdlsStorageDescriptor(
                accountUrl = "https://myaccount.blob.core.windows.net",
                filesystem = "data"
            )
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldPass_whenAuthIsAllBlank() {
            val desc = AdlsStorageDescriptor(
                accountUrl = "https://myaccount.blob.core.windows.net",
                filesystem = "data",
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
            val desc = AdlsStorageDescriptor(accountUrl = "", filesystem = "")
            val report = desc.verify()
            assertTrue(report.errors.all { it.phase == Phase.DESCRIPTOR })
        }

        @Test
        fun shouldUseErrorSeverity() {
            val desc = AdlsStorageDescriptor(accountUrl = "", filesystem = "")
            val report = desc.verify()
            assertTrue(report.issues.all { it.severity == Severity.ERROR })
        }
    }
}
