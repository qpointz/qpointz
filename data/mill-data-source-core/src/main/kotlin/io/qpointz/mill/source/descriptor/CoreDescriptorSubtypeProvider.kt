package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.databind.jsontype.NamedType

/**
 * Built-in SPI provider that registers the core descriptor subtypes.
 *
 * This registers:
 * - [LocalStorageDescriptor] (`"local"`)
 * - [RegexTableMappingDescriptor] (`"regex"`)
 * - [DirectoryTableMappingDescriptor] (`"directory"`)
 *
 * Format-specific descriptors (CSV, Parquet, Excel) are contributed
 * by their own modules through separate [DescriptorSubtypeProvider]
 * implementations.
 */
class CoreDescriptorSubtypeProvider : DescriptorSubtypeProvider {

    override fun subtypes(): List<NamedType> = listOf(
        // StorageDescriptor subtypes
        NamedType(LocalStorageDescriptor::class.java, "local"),
        // TableMappingDescriptor subtypes
        NamedType(RegexTableMappingDescriptor::class.java, "regex"),
        NamedType(DirectoryTableMappingDescriptor::class.java, "directory"),
    )
}
