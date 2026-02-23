package io.qpointz.mill.metadata.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

/** External configuration binding for metadata v2 storage setup. */
@ConfigurationProperties(prefix = "mill.metadata.v2")
class MetadataProperties {
    var storage: Storage = Storage()
    var file: File = File()

    /** Storage backend selector. */
    class Storage {
        var type: String = "file"
    }

    /** File-storage specific configuration. */
    class File {
        var path: String = "classpath:metadata/example.yml"
        var watch: Boolean = false
    }
}
