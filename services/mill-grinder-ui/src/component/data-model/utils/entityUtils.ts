import type { TreeNodeDto } from "../../../api/mill/api.ts";

/**
 * Get all schemas from tree nodes
 */
export function getSchemas(nodes: TreeNodeDto[]): TreeNodeDto[] {
    const schemas: TreeNodeDto[] = [];
    for (const node of nodes) {
        if (node.type === 'SCHEMA') {
            schemas.push(node);
        } else if (node.children) {
            // Recursively search in children
            schemas.push(...getSchemas(node.children));
        }
    }
    return schemas;
}

/**
 * Get all tables with their children (columns) from tree nodes, filtered by selected schema
 */
export function getTablesWithColumns(nodes: TreeNodeDto[], selectedSchema?: string): TreeNodeDto[] {
    const tables: TreeNodeDto[] = [];
    
    // If no schema selected, return empty array
    if (!selectedSchema) {
        return tables;
    }
    
    for (const node of nodes) {
        if (node.type === 'SCHEMA') {
            // Check if this is the selected schema
            const schemaName = node.name || node.displayName || node.id;
            if (schemaName === selectedSchema) {
                // Get all tables from this schema's children
                if (node.children) {
                    for (const child of node.children) {
                        if (child.type === 'TABLE') {
                            tables.push(child);
                        }
                    }
                }
            }
        } else if (node.children) {
            // Recursively search in children
            tables.push(...getTablesWithColumns(node.children, selectedSchema));
        }
    }
    return tables;
}

/**
 * Extract schema and table name from entity ID
 * IDs are in format: schema.table or schema.table.attribute
 */
export function parseEntityId(entityId: string | undefined): { schema?: string; table?: string; attribute?: string } {
    if (!entityId) return {};
    const parts = entityId.split('.');
    if (parts.length >= 2) {
        return {
            schema: parts[0],
            table: parts[1],
            attribute: parts.length > 2 ? parts[2] : undefined
        };
    }
    return {};
}

/**
 * Get total count of all tables from tree nodes (across all schemas).
 */
export function getAllTablesCount(nodes: TreeNodeDto[]): number {
    let count = 0;
    for (const node of nodes) {
        if (node.type === 'TABLE') {
            count++;
        } else if (node.children) {
            count += getAllTablesCount(node.children);
        }
    }
    return count;
}

/**
 * Build location string from entity metadata
 */
export function buildLocation(entity: { schemaName?: string; tableName?: string; attributeName?: string }): string {
    const parts: string[] = [];
    if (entity.schemaName) parts.push(entity.schemaName);
    if (entity.tableName) parts.push(entity.tableName);
    if (entity.attributeName) parts.push(entity.attributeName);
    return parts.join('.');
}

