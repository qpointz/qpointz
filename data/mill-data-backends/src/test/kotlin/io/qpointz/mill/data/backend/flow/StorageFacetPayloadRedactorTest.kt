package io.qpointz.mill.data.backend.flow

import io.qpointz.mill.source.descriptor.StorageFacetRedactMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StorageFacetPayloadRedactorTest {

    @Nested
    inner class BasicMode {

        @Test
        fun shouldStripConnectionStringAndSetFlagForAdls() {
            val params =
                mapOf(
                    "endpoint" to "https://acct.blob.core.windows.net",
                    "container" to "data",
                    "connectionString" to "DefaultEndpointsProtocol=https;AccountKey=SECRET;",
                    "prefix" to "p/",
                )
            val out = StorageFacetPayloadRedactor.redact("adls", params, StorageFacetRedactMode.BASIC)
            assertFalse(out.containsKey("connectionString"))
            assertEquals(true, out["connectionStringConfigured"])
            assertEquals("https://acct.blob.core.windows.net", out["endpoint"])
            assertEquals("data", out["container"])
        }

        @Test
        fun shouldStripSecretsFromAuthAndKeepAccountName() {
            val params =
                mapOf(
                    "endpoint" to "https://acct.blob.core.windows.net",
                    "container" to "c",
                    "auth" to
                        mapOf(
                            "accountName" to "acct",
                            "accountKey" to "SECRET",
                            "sasToken" to "sig=NO",
                        ),
                )
            val out = StorageFacetPayloadRedactor.redact("adls", params, StorageFacetRedactMode.BASIC)
            @Suppress("UNCHECKED_CAST")
            val auth = out["auth"] as Map<*, *>
            assertEquals("acct", auth["accountName"])
            assertFalse(auth.containsKey("accountKey"))
            assertFalse(auth.containsKey("sasToken"))
            assertEquals(true, out["delegatedAuthConfigured"])
        }

        @Test
        fun shouldStripSasQueryFromEndpoint() {
            val params =
                mapOf(
                    "endpoint" to "https://acct.blob.core.windows.net?sv=2021-06-08&sig=abc&se=2099",
                    "container" to "c",
                )
            val out = StorageFacetPayloadRedactor.redact("adls", params, StorageFacetRedactMode.BASIC)
            assertEquals("https://acct.blob.core.windows.net", out["endpoint"])
        }

        @Test
        fun shouldStripAwsStaticKeys() {
            val params =
                mapOf(
                    "bucket" to "b",
                    "auth" to
                        mapOf(
                            "accessKey" to "AKIA",
                            "secretKey" to "secret",
                            "preferAmbientCredentials" to false,
                        ),
                )
            val out = StorageFacetPayloadRedactor.redact("s3", params, StorageFacetRedactMode.BASIC)
            assertNull(out["auth"])
            assertEquals(true, out["delegatedAuthConfigured"])
        }

        @Test
        fun shouldStripLegacyAwsStaticKeyNames() {
            val params =
                mapOf(
                    "bucket" to "b",
                    "auth" to
                        mapOf(
                            "accessKeyId" to "AKIA",
                            "secretAccessKey" to "secret",
                        ),
                )
            val out = StorageFacetPayloadRedactor.redact("s3", params, StorageFacetRedactMode.BASIC)
            assertNull(out["auth"])
            assertEquals(true, out["delegatedAuthConfigured"])
        }
    }

    @Nested
    inner class SafeMode {

        @Test
        fun shouldAllowListAdlsStructuralFieldsOnly() {
            val params =
                mapOf(
                    "endpoint" to "https://acct.blob.core.windows.net",
                    "container" to "data",
                    "prefix" to "warehouse/",
                    "connectionString" to "secret",
                    "auth" to mapOf("accountKey" to "k"),
                )
            val out = StorageFacetPayloadRedactor.redact("adls", params, StorageFacetRedactMode.SAFE)
            assertEquals(setOf("container", "prefix", "connectionStringConfigured", "delegatedAuthConfigured"), out.keys)
            assertEquals("data", out["container"])
            assertEquals("warehouse/", out["prefix"])
            assertFalse(out.containsKey("endpoint"))
        }
    }

    @Nested
    inner class NoneMode {

        @Test
        fun shouldPassThroughUnchanged() {
            val params = mapOf("endpoint" to "https://x?sig=1", "auth" to mapOf("accountKey" to "k"))
            val out = StorageFacetPayloadRedactor.redact("adls", params, StorageFacetRedactMode.NONE)
            assertEquals(params.size, out.size)
            assertEquals(params["endpoint"], out["endpoint"])
            assertEquals("k", (out["auth"] as Map<*, *>)["accountKey"])
        }
    }
}
