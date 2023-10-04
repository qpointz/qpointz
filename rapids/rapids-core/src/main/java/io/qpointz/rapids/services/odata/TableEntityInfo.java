package io.qpointz.rapids.services.odata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

@AllArgsConstructor
public class TableEntityInfo {

    @Getter
    private final String schema;

    @Getter
    private final String tableName;

    @Getter
    private final String entityName;

    @Getter
    private final String entityTypeName;

    @Getter
    private final String containerName;

    @Getter
    final String namespace;

    public FullQualifiedName getEntityTypeFqdn() {
        return new FullQualifiedName(this.namespace, this.entityTypeName);
    }

    public static TableEntityInfo create(String schemaName, String tableName, String namespace, String containerName) {
        return new TableEntityInfo(schemaName, tableName, tableName, "%sType".formatted(tableName), containerName, namespace);
    }
}
