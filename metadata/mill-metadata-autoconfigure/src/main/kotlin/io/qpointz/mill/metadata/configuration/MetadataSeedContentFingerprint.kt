package io.qpointz.mill.metadata.configuration

import java.security.MessageDigest

/**
 * Content digest for metadata seed YAML bytes, stored in **`metadata_seed.fingerprint`**.
 *
 * <p>Format is **`md5:`** + lowercase hex (32 chars). The prefix leaves room to add other
 * algorithms later without ambiguity.
 */
object MetadataSeedContentFingerprint {

    private const val PREFIX = "md5:"

    /**
     * @param bytes raw seed file bytes (UTF-8 YAML)
     * @return fingerprint string such as `md5:d41d8cd98f00b204e9800998ecf8427e`
     */
    @JvmStatic
    fun md5Fingerprint(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(bytes)
        val hex = digest.joinToString("") { b -> "%02x".format(b) }
        return PREFIX + hex
    }
}
