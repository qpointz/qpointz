package io.qpointz.mill.cloud.aws.blob

import io.qpointz.mill.source.verify.Phase
import io.qpointz.mill.source.verify.Severity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class S3StorageDescriptorTest {

    @Test
    fun shouldReportError_whenBucketIsBlank() {
        val descriptor = S3StorageDescriptor(bucket = "  ")
        val report = descriptor.verify()
        assertThat(report.isValid).isFalse()
        assertThat(report.errors).anyMatch {
            it.message.contains("bucket") && it.phase == Phase.STORAGE
        }
    }

    @Test
    fun shouldReportError_whenBucketIsEmpty() {
        val descriptor = S3StorageDescriptor(bucket = "")
        val report = descriptor.verify()
        assertThat(report.isValid).isFalse()
        assertThat(report.errors).hasSize(1)
    }

    @Test
    fun shouldPass_whenBucketIsValid() {
        val descriptor = S3StorageDescriptor(bucket = "my-bucket")
        val report = descriptor.verify()
        assertThat(report.isValid).isTrue()
        assertThat(report.issues).isEmpty()
    }

    @Test
    fun shouldReportError_whenPartialAuthBundle_accessKeyOnly() {
        val descriptor = S3StorageDescriptor(
            bucket = "my-bucket",
            auth = S3AuthDescriptor(accessKeyId = "AKIA123", secretAccessKey = null)
        )
        val report = descriptor.verify()
        assertThat(report.isValid).isFalse()
        assertThat(report.errors).anyMatch {
            it.severity == Severity.ERROR && it.message.contains("Partial auth bundle")
        }
    }

    @Test
    fun shouldReportError_whenPartialAuthBundle_secretOnly() {
        val descriptor = S3StorageDescriptor(
            bucket = "my-bucket",
            auth = S3AuthDescriptor(accessKeyId = "", secretAccessKey = "secret123")
        )
        val report = descriptor.verify()
        assertThat(report.isValid).isFalse()
        assertThat(report.errors).anyMatch {
            it.message.contains("Partial auth bundle")
        }
    }

    @Test
    fun shouldPass_whenDelegatedCredentialsComplete() {
        val descriptor = S3StorageDescriptor(
            bucket = "my-bucket",
            auth = S3AuthDescriptor(accessKeyId = "AKIA123", secretAccessKey = "secret123")
        )
        val report = descriptor.verify()
        assertThat(report.isValid).isTrue()
    }

    @Test
    fun shouldPass_whenAmbientCredentials_nullAuth() {
        val descriptor = S3StorageDescriptor(bucket = "my-bucket", auth = null)
        val report = descriptor.verify()
        assertThat(report.isValid).isTrue()
    }

    @Test
    fun shouldPass_whenAmbientCredentials_allBlank() {
        val descriptor = S3StorageDescriptor(
            bucket = "my-bucket",
            auth = S3AuthDescriptor(accessKeyId = "", secretAccessKey = "")
        )
        val report = descriptor.verify()
        assertThat(report.isValid).isTrue()
    }

    @Test
    fun shouldInferDelegatedCredentials_whenKeysPresent() {
        val auth = S3AuthDescriptor(accessKeyId = "AKIA123", secretAccessKey = "secret123")
        assertThat(auth.useDelegatedCredentials).isTrue()
    }

    @Test
    fun shouldInferAmbientCredentials_whenKeysBlank() {
        val auth = S3AuthDescriptor(accessKeyId = "", secretAccessKey = "")
        assertThat(auth.useDelegatedCredentials).isFalse()
    }

    @Test
    fun shouldInferAmbientCredentials_whenKeysNull() {
        val auth = S3AuthDescriptor()
        assertThat(auth.useDelegatedCredentials).isFalse()
    }

    @Test
    fun shouldForceAmbient_whenPreferAmbientCredentialsIsTrue() {
        val auth = S3AuthDescriptor(
            accessKeyId = "AKIA123",
            secretAccessKey = "secret123",
            preferAmbientCredentials = true
        )
        assertThat(auth.useDelegatedCredentials).isFalse()
    }

    @Test
    fun shouldPass_whenPreferAmbientWithDelegatedKeys() {
        val descriptor = S3StorageDescriptor(
            bucket = "my-bucket",
            auth = S3AuthDescriptor(
                accessKeyId = "AKIA123",
                secretAccessKey = "secret123",
                preferAmbientCredentials = true
            )
        )
        val report = descriptor.verify()
        assertThat(report.isValid).isTrue()
    }
}
