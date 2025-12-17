/**
 * Metadata Sidebar Content Component
 * 
 * Content displayed in the sidebar when on data-model routes.
 * Self-contained with its own MetadataProvider.
 */
import { Stack, Box, Loader } from "@mantine/core";
import { useState } from "react";
import { MetadataProvider, useMetadataContext } from "./MetadataProvider";
import { getSchemas, getTablesWithColumns } from "./utils/entityUtils";
import SchemaList from "./components/tree/SchemaList";
import TableList from "./components/tree/TableList";
import ScopeSelector from "./components/ScopeSelector";

function MetadataTreeContent() {
    const { tree, entity, scope, schema } = useMetadataContext();
    const [expandedTables, setExpandedTables] = useState<Set<string>>(new Set());
    
    // Get schemas from tree data
    const schemas = getSchemas(tree.data);
    
    // Get tables with their columns, filtered by selected schema
    const tables = getTablesWithColumns(tree.data, schema.selected);
    
    // Toggle table expansion
    const toggleTable = (tableId: string) => {
        setExpandedTables(prev => {
            const next = new Set(prev);
            if (next.has(tableId)) {
                next.delete(tableId);
            } else {
                next.add(tableId);
            }
            return next;
        });
    };

    const handleSchemaSelect = (schemaName: string, schemaId?: string) => {
        schema.select(schemaName);
        if (schemaId) {
            entity.select(schemaId);
        }
    };

    const handleTableSelect = (tableId: string) => {
        entity.select(tableId);
    };

    const handleColumnSelect = (columnId: string) => {
        entity.select(columnId);
    };

    if (tree.loading) {
        return (
            <Box p="md">
                <Loader size="sm" />
            </Box>
        );
    }

    return (
        <Stack gap="md">
            {/* Schemas and Tables */}
            <Box>
                <SchemaList
                    schemas={schemas}
                    selectedSchema={schema.selected}
                    loading={tree.loading}
                    onSchemaSelect={handleSchemaSelect}
                    collapsed={false}
                />
                
                <TableList
                    tables={tables}
                    selectedEntityId={entity.selected?.id}
                    expandedTables={expandedTables}
                    loading={tree.loading}
                    hasSelectedSchema={!!schema.selected}
                    onTableToggle={toggleTable}
                    onTableSelect={handleTableSelect}
                    onColumnSelect={handleColumnSelect}
                    collapsed={false}
                />
            </Box>

            {/* Scope Selector */}
            <ScopeSelector
                currentScope={scope.current}
                onScopeChange={scope.set}
                collapsed={false}
            />
        </Stack>
    );
}

export function MetadataSidebarContent() {
    return (
        <MetadataProvider>
            <MetadataTreeContent />
        </MetadataProvider>
    );
}
