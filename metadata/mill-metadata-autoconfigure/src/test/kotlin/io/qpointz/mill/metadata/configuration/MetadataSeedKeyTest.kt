package io.qpointz.mill.metadata.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader
import java.io.File

class MetadataSeedKeyTest {

    private val loader = DefaultResourceLoader()

    @Test
    fun `stableKey returns trimmed classpath location unchanged`() {
        val loc = "  classpath:metadata/platform-bootstrap.yaml  "
        assertThat(MetadataSeedKey.stableKey(loader, loc)).isEqualTo("classpath:metadata/platform-bootstrap.yaml")
    }

    @Test
    fun `stableKey canonicalizes file URLs for same physical file`() {
        val f = File.createTempFile("mill-seed-key", ".yaml")
        f.deleteOnExit()
        f.writeText("---\n")
        val canonicalUri = f.canonicalFile.toURI().toString()
        val viaAbsolutePath = "file:${f.absolutePath}"
        assertThat(MetadataSeedKey.stableKey(loader, viaAbsolutePath)).isEqualTo(canonicalUri)
        assertThat(MetadataSeedKey.stableKey(loader, canonicalUri)).isEqualTo(canonicalUri)
    }

    @Test
    fun `stableKey strips query from s3 location`() {
        val loc = "s3://bucket/path/seed.yaml?X-Amz-Signature=abc&X-Amz-Credential=secret"
        assertThat(MetadataSeedKey.stableKey(loader, loc)).isEqualTo("s3://bucket/path/seed.yaml")
    }

    @Test
    fun `stableKey leaves gs without query unchanged`() {
        val loc = "gs://bucket/object.yaml"
        assertThat(MetadataSeedKey.stableKey(loader, loc)).isEqualTo(loc)
    }
}
