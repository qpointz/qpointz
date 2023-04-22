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
    private final FullQualifiedName containerFqdn;

    public static TableEntityInfo create(String schemaName, String tableName, FullQualifiedName container) {
        return new TableEntityInfo(schemaName, tableName, tableName, "%sType".formatted(tableName), container);
    }
}
