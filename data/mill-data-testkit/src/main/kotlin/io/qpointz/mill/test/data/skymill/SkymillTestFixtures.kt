package io.qpointz.mill.test.data.skymill

import io.qpointz.mill.source.descriptor.LocalStorageDescriptor
import io.qpointz.mill.source.descriptor.ReaderDescriptor
import io.qpointz.mill.source.descriptor.RegexTableMappingDescriptor
import io.qpointz.mill.source.descriptor.SourceDescriptor
import io.qpointz.mill.source.descriptor.TableDescriptor
import io.qpointz.mill.source.format.avro.AvroFormatDescriptor
import io.qpointz.mill.source.format.parquet.ParquetFormatDescriptor
import io.qpointz.mill.source.format.text.CsvFormatDescriptor
import java.nio.file.Files
import java.nio.file.Path

/**
 * Shared Skymill dataset fixtures: paths, descriptors, and flow backend configs.
 *
 * Analogous to [io.qpointz.mill.source.calcite.FlowCalciteTestFixtures] but backed by the
 * real Skymill tree under `test/datasets/skymill/`. Calcite session wiring and
 * story-specific SQL live in `mill-data-source-calcite` test sources.
 */
object SkymillTestFixtures {

    const val SCHEMA_NAME = "skymill"

    fun descriptorFor(dataset: SkymillDataset): SourceDescriptor {
        val root = skymillDatasetRoot(dataset)
        return when (dataset) {
            SkymillDataset.CSV -> SourceDescriptor(
                name = SCHEMA_NAME,
                storage = LocalStorageDescriptor(rootPath = root.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "csv",
                        format = CsvFormatDescriptor(delimiter = ";", hasHeader = true),
                    ),
                ),
                table = TableDescriptor(
                    mapping = RegexTableMappingDescriptor(pattern = "(?<table>[^/]+)\\.csv"),
                ),
            )

            SkymillDataset.PARQUET -> SourceDescriptor(
                name = SCHEMA_NAME,
                storage = LocalStorageDescriptor(rootPath = root.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "parquet",
                        format = ParquetFormatDescriptor(),
                    ),
                ),
                table = TableDescriptor(
                    mapping = RegexTableMappingDescriptor(pattern = "(?<table>[^/]+)\\.parquet"),
                ),
            )

            SkymillDataset.AVRO -> SourceDescriptor(
                name = SCHEMA_NAME,
                storage = LocalStorageDescriptor(rootPath = root.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "avro",
                        format = AvroFormatDescriptor(),
                    ),
                ),
                table = TableDescriptor(
                    mapping = RegexTableMappingDescriptor(pattern = "(?<table>[^/]+)\\.avro"),
                ),
            )
        }
    }

    /**
     * Repository root for integration tests (`mill.repo.root` system property).
     */
    fun projectRoot(): Path =
        Path.of(System.getProperty("mill.repo.root", "."))
            .toAbsolutePath()
            .normalize()

    fun skymillDatasetRoot(dataset: SkymillDataset): Path =
        skymillDatasetRoot(dataset, projectRoot())

    fun skymillDatasetRoot(dataset: SkymillDataset, projectRoot: Path): Path =
        projectRoot.resolve("test/datasets/skymill/${dataset.directoryName}")

    fun isSkymillAvroDatasetComplete(): Boolean =
        Files.exists(skymillDatasetRoot(SkymillDataset.AVRO).resolve("flight_instances.avro"))

    fun flowSkymillCsvConfig(): Path = testkitModuleConfig("flow-skymill.yaml")

    fun flowSkymillParquetConfig(): Path = testkitModuleConfig("flow-skymill-parquet.yaml")

    fun flowSkymillAvroConfig(): Path = testkitModuleConfig("flow-skymill-avro.yaml")

    fun flowSkymillCsvConfig(projectRoot: Path): Path =
        projectRoot.resolve("data/mill-data-testkit/config/test/flow-skymill.yaml")

    fun flowSkymillParquetConfig(projectRoot: Path): Path =
        projectRoot.resolve("data/mill-data-testkit/config/test/flow-skymill-parquet.yaml")

    fun flowSkymillAvroConfig(projectRoot: Path): Path =
        projectRoot.resolve("data/mill-data-testkit/config/test/flow-skymill-avro.yaml")

    private fun testkitModuleConfig(fileName: String): Path =
        Path.of("./config/test/$fileName")
}

enum class SkymillDataset(val directoryName: String) {
    CSV("csv"),
    PARQUET("parquet"),
    AVRO("avro"),
}
