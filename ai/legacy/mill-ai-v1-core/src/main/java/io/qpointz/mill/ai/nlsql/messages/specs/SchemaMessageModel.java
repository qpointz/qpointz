package io.qpointz.mill.ai.nlsql.messages.specs;

import lombok.val;

import java.util.List;

public interface SchemaMessageModel {

    record Schema(String name, String description, List<Table> tables) {
    }

    record Table(String schema, String name, String description, List<Attribute> attributes) {
        public String fullName() {
            return schema != null && !schema.isBlank()
                    ? schema + "." + name
                    : name;
        }
    }

    record Attribute(String schemaName, String tableName, String name, String description, String typeName, Boolean nullable) {
    }

    record Relation(String sourceSchema, String sourceTable, String sourceAttribute,
                           String targetSchema, String targetTable, String targetAttribute,
                           String cardinality,
                           String description) {

        public String sourceFullTableName() {
            return fullName(sourceSchema, sourceTable, null);
        }

        public String sourceFullAttributeName() {
            return fullName(sourceSchema, sourceTable, sourceAttribute);
        }

        public String targetFullTableName() {
            return fullName(targetSchema, targetTable, null);
        }

        public String targetFullAttributeName() {
            return fullName(targetSchema, targetTable, targetAttribute);
        }

        private static String fullName(String schema, String table, String attribute) {
            val builder = new StringBuilder();
            if (schema != null && !schema.isBlank()) {
                builder.append(schema.trim());
                builder.append(".");
            }
            builder.append(table.trim());
            if (attribute!=null && !attribute.isEmpty()) {
                builder.append(".");
                builder.append(attribute);
            }
            return builder.toString();
        }
    }
}
