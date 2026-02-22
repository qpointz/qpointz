package io.qpointz.mill.source.verify

import io.qpointz.mill.source.descriptor.*
import io.qpointz.mill.source.factory.SourceMaterializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class SourceVerifierTest {

    private val materializer = SourceMaterializer()
    private val csvRoot = Paths.get("../../test/datasets/airlines/csv").toAbsolutePath().normalize()

    private fun regexTable(pattern: String = ".*?(?<table>[^/]+)\\.csv$") =
        TableDescriptor(mapping = RegexTableMappingDescriptor(pattern = pattern))

    private fun directoryTable(depth: Int = 1) =
        TableDescriptor(mapping = DirectoryTableMappingDescriptor(depth = depth))

    // ------------------------------------------------------------------
    // Static descriptor verification
    // ------------------------------------------------------------------

    @Nested
    inner class StaticDescriptorTests {

        @Test
        fun shouldPassValidDescriptor() {
            val desc = SourceDescriptor(
                name = "valid",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val report = SourceVerifier.verifyDescriptor(desc)
            assertTrue(report.isValid, "Valid descriptor should pass. Errors: ${report.errors.map { it.message }}")
        }

        @Test
        fun shouldReportBlankName() {
            val desc = SourceDescriptor(
                name = "",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = directoryTable())
                )
            )
            val report = SourceVerifier.verifyDescriptor(desc)
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("name") && it.phase == Phase.DESCRIPTOR })
        }

        @Test
        fun shouldReportBlankStorageRootPath() {
            val desc = SourceDescriptor(
                name = "test",
                storage = LocalStorageDescriptor(rootPath = ""),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = directoryTable())
                )
            )
            val report = SourceVerifier.verifyDescriptor(desc)
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.phase == Phase.STORAGE })
        }

        @Test
        fun shouldReportDuplicateLabels() {
            val desc = SourceDescriptor(
                name = "test",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(type = "stub", label = "raw", format = StubFormatDescriptor(), table = directoryTable()),
                    ReaderDescriptor(type = "stub", label = "raw", format = StubFormatDescriptor(), table = directoryTable())
                )
            )
            val report = SourceVerifier.verifyDescriptor(desc)
            assertTrue(report.warnings.any { it.message.contains("Duplicate reader label") })
        }

        @Test
        fun shouldReportMissingTableMappingWhenNoDefault() {
            val desc = SourceDescriptor(
                name = "test",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                table = null,
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = null)
                )
            )
            val report = SourceVerifier.verifyDescriptor(desc)
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("no table mapping") })
        }

        @Test
        fun shouldNotReportMissingTableMappingWhenDefaultExists() {
            val desc = SourceDescriptor(
                name = "test",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                table = directoryTable(),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = null)
                )
            )
            val report = SourceVerifier.verifyDescriptor(desc)
            assertTrue(report.isValid, "Errors: ${report.errors.map { it.message }}")
        }

        @Test
        fun shouldReportBlankReaderType() {
            val desc = SourceDescriptor(
                name = "test",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(type = "", format = StubFormatDescriptor(), table = directoryTable())
                )
            )
            val report = SourceVerifier.verifyDescriptor(desc)
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.phase == Phase.READER && it.message.contains("type") })
        }
    }

    // ------------------------------------------------------------------
    // Table mapping descriptor verification
    // ------------------------------------------------------------------

    @Nested
    inner class TableMappingDescriptorTests {

        @Test
        fun shouldPassValidRegexTableMapping() {
            val desc = RegexTableMappingDescriptor(pattern = ".*?(?<table>[^/]+)\\.csv$")
            val report = desc.verify()
            assertTrue(report.isValid, "Errors: ${report.errors.map { it.message }}")
        }

        @Test
        fun shouldReportBlankRegexPattern() {
            val desc = RegexTableMappingDescriptor(pattern = "")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("pattern") })
        }

        @Test
        fun shouldReportInvalidRegexPattern() {
            val desc = RegexTableMappingDescriptor(pattern = "[invalid")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("Invalid regex") })
        }

        @Test
        fun shouldReportMissingNamedGroup() {
            val desc = RegexTableMappingDescriptor(pattern = ".*\\.csv$", tableNameGroup = "table")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("named group") })
        }

        @Test
        fun shouldReportMissingCustomNamedGroup() {
            val desc = RegexTableMappingDescriptor(
                pattern = ".*?(?<table>[^/]+)\\.csv$", tableNameGroup = "custom")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("custom") })
        }

        @Test
        fun shouldPassDirectoryTableMappingWithValidDepth() {
            val desc = DirectoryTableMappingDescriptor(depth = 2)
            val report = desc.verify()
            assertTrue(report.isValid)
        }

        @Test
        fun shouldReportInvalidDirectoryDepth() {
            val desc = DirectoryTableMappingDescriptor(depth = 0)
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("depth") })
        }

        @Test
        fun shouldPassValidGlobTableMapping() {
            val desc = GlobTableMappingDescriptor(pattern = "**/*.csv", table = "orders")
            val report = desc.verify()
            assertTrue(report.isValid, "Errors: ${report.errors.map { it.message }}")
        }

        @Test
        fun shouldReportBlankGlobPattern() {
            val desc = GlobTableMappingDescriptor(pattern = "", table = "orders")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("pattern") })
        }

        @Test
        fun shouldReportBlankGlobTable() {
            val desc = GlobTableMappingDescriptor(pattern = "**/*.csv", table = "")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("table") })
        }

        @Test
        fun shouldReportBothBlankGlobFields() {
            val desc = GlobTableMappingDescriptor(pattern = "  ", table = "  ")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.size >= 2)
        }

        @Test
        fun shouldReportInvalidGlobSyntax() {
            val desc = GlobTableMappingDescriptor(pattern = "**/*.csv[", table = "data")
            val report = desc.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("Invalid glob") },
                "Should report invalid glob: ${report.errors.map { it.message }}")
        }

        @Test
        fun shouldPassGlobWithBraces() {
            val desc = GlobTableMappingDescriptor(pattern = "**/*.{csv,tsv}", table = "text_files")
            val report = desc.verify()
            assertTrue(report.isValid, "Brace alternatives should be valid: ${report.errors.map { it.message }}")
        }

        @Test
        fun shouldPassGlobWithQuestionMark() {
            val desc = GlobTableMappingDescriptor(pattern = "/data/file?.csv", table = "files")
            val report = desc.verify()
            assertTrue(report.isValid)
        }
    }

    // ------------------------------------------------------------------
    // Table attribute descriptor verification
    // ------------------------------------------------------------------

    @Nested
    inner class TableAttributeDescriptorTests {

        @Test
        fun shouldPassValidRegexAttribute() {
            val attr = TableAttributeDescriptor(
                name = "year", source = AttributeSource.REGEX,
                pattern = ".*_(?<year>\\d{4})\\.csv$", group = "year", type = AttributeType.INT
            )
            assertTrue(attr.verify().isValid)
        }

        @Test
        fun shouldPassValidConstantAttribute() {
            val attr = TableAttributeDescriptor(
                name = "pipeline", source = AttributeSource.CONSTANT, value = "raw"
            )
            assertTrue(attr.verify().isValid)
        }

        @Test
        fun shouldReportBlankAttributeName() {
            val attr = TableAttributeDescriptor(
                name = "", source = AttributeSource.CONSTANT, value = "x"
            )
            val report = attr.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("name") })
        }

        @Test
        fun shouldReportMissingPatternForRegex() {
            val attr = TableAttributeDescriptor(
                name = "x", source = AttributeSource.REGEX, group = "x"
            )
            val report = attr.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("pattern") })
        }

        @Test
        fun shouldReportMissingGroupForRegex() {
            val attr = TableAttributeDescriptor(
                name = "x", source = AttributeSource.REGEX,
                pattern = ".*(?<table>\\w+)\\.csv$"
            )
            val report = attr.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("group") })
        }

        @Test
        fun shouldReportGroupNotInPattern() {
            val attr = TableAttributeDescriptor(
                name = "x", source = AttributeSource.REGEX,
                pattern = ".*(?<table>\\w+)\\.csv$", group = "year"
            )
            val report = attr.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("year") })
        }

        @Test
        fun shouldReportMissingValueForConstant() {
            val attr = TableAttributeDescriptor(
                name = "x", source = AttributeSource.CONSTANT
            )
            val report = attr.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("value") })
        }

        @Test
        fun shouldReportMissingFormatForDate() {
            val attr = TableAttributeDescriptor(
                name = "d", source = AttributeSource.REGEX,
                pattern = ".*(?<d>\\d{8})\\.csv$", group = "d",
                type = AttributeType.DATE
            )
            val report = attr.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("format") })
        }

        @Test
        fun shouldPassDateWithValidFormat() {
            val attr = TableAttributeDescriptor(
                name = "d", source = AttributeSource.REGEX,
                pattern = ".*(?<d>\\d{8})\\.csv$", group = "d",
                type = AttributeType.DATE, format = "ddMMyyyy"
            )
            assertTrue(attr.verify().isValid)
        }

        @Test
        fun shouldReportInvalidDateFormat() {
            val attr = TableAttributeDescriptor(
                name = "d", source = AttributeSource.REGEX,
                pattern = ".*(?<d>\\d{8})\\.csv$", group = "d",
                type = AttributeType.DATE, format = "'unclosed"
            )
            val report = attr.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("invalid date format") })
        }
    }

    // ------------------------------------------------------------------
    // TableDescriptor verification
    // ------------------------------------------------------------------

    @Nested
    inner class TableDescriptorTests {

        @Test
        fun shouldPassValidTableDescriptor() {
            val td = TableDescriptor(
                mapping = DirectoryTableMappingDescriptor(),
                attributes = listOf(
                    TableAttributeDescriptor(name = "x", source = AttributeSource.CONSTANT, value = "v")
                )
            )
            assertTrue(td.verify().isValid)
        }

        @Test
        fun shouldReportMissingMapping() {
            val td = TableDescriptor(mapping = null)
            val report = td.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("mapping") })
        }

        @Test
        fun shouldReportDuplicateAttributeNames() {
            val td = TableDescriptor(
                mapping = DirectoryTableMappingDescriptor(),
                attributes = listOf(
                    TableAttributeDescriptor(name = "dup", source = AttributeSource.CONSTANT, value = "a"),
                    TableAttributeDescriptor(name = "dup", source = AttributeSource.CONSTANT, value = "b")
                )
            )
            val report = td.verify()
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("Duplicate table attribute name") })
        }
    }

    // ------------------------------------------------------------------
    // Deep verification — full source
    // ------------------------------------------------------------------

    @Nested
    inner class DeepVerificationTests {

        @Test
        fun shouldVerifyValidSourceEndToEnd() {
            val desc = SourceDescriptor(
                name = "airlines",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val report = SourceVerifier.verify(desc, materializer)
            assertTrue(report.isValid, "Errors: ${report.errors.map { it.message }}")
            assertTrue(report.tables.isNotEmpty(), "Should discover tables")
        }

        @Test
        fun shouldReportNonExistentStoragePath() {
            val desc = SourceDescriptor(
                name = "bad-path",
                storage = LocalStorageDescriptor(rootPath = "/nonexistent/path/xyz"),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = directoryTable())
                )
            )
            val report = SourceVerifier.verify(desc, materializer)
            assertFalse(report.isValid)
            assertTrue(report.errors.isNotEmpty())
        }

        @Test
        fun shouldReportEmptyStorage() {
            val tempDir = java.nio.file.Files.createTempDirectory("empty-verify-test")
            try {
                val desc = SourceDescriptor(
                    name = "empty",
                    storage = LocalStorageDescriptor(rootPath = tempDir.toString()),
                    readers = listOf(
                        ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = directoryTable())
                    )
                )
                val report = SourceVerifier.verify(desc, materializer)
                assertTrue(report.isValid, "Empty storage is a warning, not an error")
                assertTrue(report.warnings.any { it.message.contains("empty") })
            } finally {
                java.nio.file.Files.delete(tempDir)
            }
        }

        @Test
        fun shouldReportUnknownFormatType() {
            val desc = SourceDescriptor(
                name = "unknown-format",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "nonexistent",
                        format = object : FormatDescriptor {},
                        table = directoryTable()
                    )
                )
            )
            val report = SourceVerifier.verify(desc, materializer)
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.message.contains("Materialization failed") })
        }
    }

    // ------------------------------------------------------------------
    // Deep verification — conflict analysis
    // ------------------------------------------------------------------

    @Nested
    inner class ConflictVerificationTests {

        @Test
        fun shouldReportRejectCollision() {
            val desc = SourceDescriptor(
                name = "collision",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(default = ConflictStrategy.REJECT),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable()),
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val report = SourceVerifier.verify(desc, materializer)
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.phase == Phase.CONFLICT && it.message.contains("reject") })
        }

        @Test
        fun shouldReportUnionAsInfo() {
            val desc = SourceDescriptor(
                name = "union-verify",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(default = ConflictStrategy.UNION),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable()),
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val report = SourceVerifier.verify(desc, materializer)
            assertTrue(report.isValid)
            assertTrue(report.infos.any { it.phase == Phase.CONFLICT && it.message.contains("union") })
        }

        @Test
        fun shouldReportLabelDisambiguationAsInfo() {
            val desc = SourceDescriptor(
                name = "label-verify",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(default = ConflictStrategy.REJECT),
                readers = listOf(
                    ReaderDescriptor(type = "stub", label = "a", format = StubFormatDescriptor(), table = regexTable()),
                    ReaderDescriptor(type = "stub", label = "b", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val report = SourceVerifier.verify(desc, materializer)
            assertTrue(report.isValid, "Labels should resolve collisions. Errors: ${report.errors.map { it.message }}")
            assertTrue(report.infos.any { it.phase == Phase.CONFLICT && it.message.contains("label") })
        }

        @Test
        fun shouldReportPerTableRejectOverride() {
            val desc = SourceDescriptor(
                name = "selective-reject",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(
                    default = ConflictStrategy.UNION,
                    rules = mapOf("cities" to ConflictStrategy.REJECT)
                ),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable()),
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val report = SourceVerifier.verify(desc, materializer)
            assertFalse(report.isValid)
            assertTrue(report.errors.any { it.phase == Phase.CONFLICT && it.message.contains("cities") })
            assertTrue(report.infos.any { it.phase == Phase.CONFLICT && it.message.contains("union") })
        }

        @Test
        fun shouldReportUnusedConflictRules() {
            val desc = SourceDescriptor(
                name = "unused-rules",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(
                    default = ConflictStrategy.REJECT,
                    rules = mapOf("nonexistent_table" to ConflictStrategy.UNION)
                ),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val report = SourceVerifier.verify(desc, materializer)
            assertTrue(report.isValid, "Unused rules are warnings, not errors")
            assertTrue(report.warnings.any {
                it.phase == Phase.CONFLICT && it.message.contains("nonexistent_table")
            })
        }
    }

    // ------------------------------------------------------------------
    // Reader-level verification
    // ------------------------------------------------------------------

    @Nested
    inner class ReaderLevelTests {

        @Test
        fun shouldVerifySingleReaderWithBlobs() {
            val desc = SourceDescriptor(
                name = "reader-test",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val materialized = materializer.materialize(desc)
            val blobs = materialized.blobSource.listBlobs().toList()
            val readerReport = materialized.readers[0].verify(blobs, materialized.blobSource)

            assertTrue(readerReport.isValid, "Errors: ${readerReport.errors.map { it.message }}")
            assertTrue(readerReport.tables.isNotEmpty())
            assertTrue(readerReport.tables.all { it.blobCount > 0 })
            assertTrue(readerReport.tables.all { it.schema != null })

            materialized.close()
        }

        @Test
        fun shouldReportReaderWithNoMatchingBlobs() {
            val desc = SourceDescriptor(
                name = "no-match",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(),
                        table = TableDescriptor(
                            mapping = RegexTableMappingDescriptor(pattern = ".*\\.parquet$")
                        )
                    )
                )
            )
            val materialized = materializer.materialize(desc)
            val blobs = materialized.blobSource.listBlobs().toList()
            val readerReport = materialized.readers[0].verify(blobs, materialized.blobSource)

            assertTrue(readerReport.isValid, "No match is a warning, not an error")
            assertTrue(readerReport.warnings.any { it.message.contains("no blobs matched") })
            assertTrue(readerReport.tables.isEmpty())

            materialized.close()
        }

        @Test
        fun shouldReportUnmappedBlobsAsInfo() {
            val desc = SourceDescriptor(
                name = "partial-match",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(),
                        table = TableDescriptor(
                            mapping = RegexTableMappingDescriptor(pattern = ".*?(?<table>cities)\\.csv$")
                        )
                    )
                )
            )
            val materialized = materializer.materialize(desc)
            val blobs = materialized.blobSource.listBlobs().toList()
            val readerReport = materialized.readers[0].verify(blobs, materialized.blobSource)

            assertTrue(readerReport.isValid)
            assertEquals(1, readerReport.tables.size)
            assertEquals("cities", readerReport.tables[0].name)
            assertTrue(readerReport.infos.any { it.message.contains("skipped") })

            materialized.close()
        }
    }

    // ------------------------------------------------------------------
    // VerificationReport composability
    // ------------------------------------------------------------------

    @Nested
    inner class ReportTests {

        @Test
        fun shouldCombineReportsWithPlusOperator() {
            val r1 = VerificationReport.of(
                VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR, "error 1")
            )
            val r2 = VerificationReport(
                issues = listOf(VerificationIssue(Severity.WARNING, Phase.STORAGE, "warning 1")),
                tables = listOf(TableSummary("t1", 3, "csv"))
            )
            val combined = r1 + r2
            assertEquals(2, combined.issues.size)
            assertEquals(1, combined.tables.size)
            assertFalse(combined.isValid)
        }

        @Test
        fun shouldReportEmptyAsValid() {
            assertTrue(VerificationReport.EMPTY.isValid)
            assertTrue(VerificationReport.EMPTY.issues.isEmpty())
            assertTrue(VerificationReport.EMPTY.tables.isEmpty())
        }

        @Test
        fun shouldFilterBySeverity() {
            val report = VerificationReport(
                issues = listOf(
                    VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR, "e"),
                    VerificationIssue(Severity.WARNING, Phase.STORAGE, "w"),
                    VerificationIssue(Severity.INFO, Phase.READER, "i")
                )
            )
            assertEquals(1, report.errors.size)
            assertEquals(1, report.warnings.size)
            assertEquals(1, report.infos.size)
        }
    }
}
