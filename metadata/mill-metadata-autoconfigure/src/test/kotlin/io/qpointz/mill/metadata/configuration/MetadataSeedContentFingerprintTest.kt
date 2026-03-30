package io.qpointz.mill.metadata.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetadataSeedContentFingerprintTest {

    @Test
    fun `md5Fingerprint matches known empty payload`() {
        assertThat(MetadataSeedContentFingerprint.md5Fingerprint(ByteArray(0)))
            .isEqualTo("md5:d41d8cd98f00b204e9800998ecf8427e")
    }

    @Test
    fun `md5Fingerprint is stable for same bytes`() {
        val b = "hello seed\n".toByteArray(Charsets.UTF_8)
        val a = MetadataSeedContentFingerprint.md5Fingerprint(b)
        val c = MetadataSeedContentFingerprint.md5Fingerprint(b)
        assertThat(a).isEqualTo(c).startsWith("md5:")
    }
}
