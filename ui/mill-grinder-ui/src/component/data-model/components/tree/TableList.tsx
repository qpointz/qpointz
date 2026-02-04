import { Text } from "@mantine/core";
import type { TreeNodeDto } from "../../../../api/mill/api.ts";
import TableNode from "./TableNode";

interface TableListProps {
    tables: TreeNodeDto[];
    selectedEntityId?: string;
    expandedTables: Set<string>;
    loading: boolean;
    hasSelectedSchema: boolean;
    onTableToggle: (tableId: string) => void;
    onTableSelect: (tableId: string) => void;
    onColumnSelect: (columnId: string) => void;
    collapsed: boolean;
}

export default function TableList({ 
    tables, 
    selectedEntityId,
    expandedTables, 
    loading, 
    hasSelectedSchema,
    onTableToggle,
    onTableSelect,
    onColumnSelect,
    collapsed 
}: TableListProps) {
    if (collapsed) {
        return null;
    }

    if (loading) {
        return <Text mt={10} size="sm" c="dimmed">Loading...</Text>;
    }

    if (!hasSelectedSchema) {
        return <Text mt={10} size="sm" c="dimmed">Select a schema to view tables</Text>;
    }

    if (tables.length === 0) {
        return <Text mt={10} size="sm" c="dimmed">No tables found</Text>;
    }

    const getColumns = (table: TreeNodeDto): TreeNodeDto[] => {
        return table.children?.filter(child => child.type === 'ATTRIBUTE') || [];
    };

    return (
        <>
            <Text mt={20} size="sm" color="gray.5">Tables</Text>
            {tables.map((table) => {
                const columns = getColumns(table);
                const isExpanded = table.id ? expandedTables.has(table.id) : false;
                const isTableSelected = selectedEntityId === table.id;
                
                return (
                    <TableNode
                        key={table.id}
                        table={table}
                        columns={columns}
                        isExpanded={isExpanded}
                        isSelected={isTableSelected}
                        selectedEntityId={selectedEntityId}
                        onToggle={onTableToggle}
                        onSelect={onTableSelect}
                        onColumnSelect={onColumnSelect}
                    />
                );
            })}
        </>
    );
}

