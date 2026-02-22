package io.qpointz.mill.source.factory

import io.qpointz.mill.source.BlobToTableMapper
import io.qpointz.mill.source.DirectoryTableMapper
import io.qpointz.mill.source.GlobTableMapper
import io.qpointz.mill.source.RegexTableMapper
import io.qpointz.mill.source.descriptor.DirectoryTableMappingDescriptor
import io.qpointz.mill.source.descriptor.GlobTableMappingDescriptor
import io.qpointz.mill.source.descriptor.RegexTableMappingDescriptor
import io.qpointz.mill.source.descriptor.TableMappingDescriptor

/**
 * Built-in [TableMapperFactory] for [RegexTableMappingDescriptor].
 */
class RegexTableMapperFactory : TableMapperFactory {

    override val descriptorType: Class<out TableMappingDescriptor>
        get() = RegexTableMappingDescriptor::class.java

    override fun create(descriptor: TableMappingDescriptor): BlobToTableMapper {
        require(descriptor is RegexTableMappingDescriptor) {
            "Expected RegexTableMappingDescriptor, got ${descriptor::class.java.name}"
        }
        return RegexTableMapper(
            pattern = Regex(descriptor.pattern),
            tableNameGroup = descriptor.tableNameGroup
        )
    }
}

/**
 * Built-in [TableMapperFactory] for [DirectoryTableMappingDescriptor].
 */
class DirectoryTableMapperFactory : TableMapperFactory {

    override val descriptorType: Class<out TableMappingDescriptor>
        get() = DirectoryTableMappingDescriptor::class.java

    override fun create(descriptor: TableMappingDescriptor): BlobToTableMapper {
        require(descriptor is DirectoryTableMappingDescriptor) {
            "Expected DirectoryTableMappingDescriptor, got ${descriptor::class.java.name}"
        }
        return DirectoryTableMapper(depth = descriptor.depth)
    }
}

/**
 * Built-in [TableMapperFactory] for [GlobTableMappingDescriptor].
 */
class GlobTableMapperFactory : TableMapperFactory {

    override val descriptorType: Class<out TableMappingDescriptor>
        get() = GlobTableMappingDescriptor::class.java

    override fun create(descriptor: TableMappingDescriptor): BlobToTableMapper {
        require(descriptor is GlobTableMappingDescriptor) {
            "Expected GlobTableMappingDescriptor, got ${descriptor::class.java.name}"
        }
        return GlobTableMapper(pattern = descriptor.pattern, tableName = descriptor.table)
    }
}
