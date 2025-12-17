import { NavLink, Text, Stack } from "@mantine/core";
import { TbDatabase } from "react-icons/tb";
import type { TreeNodeDto } from "../../../../api/mill/api.ts";

interface SchemaListProps {
    schemas: TreeNodeDto[];
    selectedSchema?: string;
    loading: boolean;
    onSchemaSelect: (schemaName: string, schemaId?: string) => void;
    collapsed: boolean;
}

export default function SchemaList({ 
    schemas, 
    selectedSchema, 
    loading, 
    onSchemaSelect,
    collapsed 
}: SchemaListProps) {
    if (collapsed) {
        return null;
    }

    if (loading) {
        return <Text mt="sm" size="sm" c="dimmed">Loading...</Text>;
    }

    if (schemas.length === 0) {
        return <Text mt="sm" size="sm" c="dimmed">No schemas found</Text>;
    }

    return (
        <Stack gap="xs">
            <Text size="sm" c="dimmed">Schemas</Text>
            {schemas.map((schemaNode) => {
                const schemaName = schemaNode.name || schemaNode.displayName || schemaNode.id || '';
                const isSchemaSelected = selectedSchema === schemaName;
                
                return (
                    <NavLink
                        key={schemaNode.id}
                        component="div"
                        label={schemaName}
                        active={isSchemaSelected}
                        leftSection={<TbDatabase size={14} />}
                        onClick={(e) => {
                            e.stopPropagation();
                            if (schemaName) {
                                onSchemaSelect(schemaName, schemaNode.id);
                            }
                        }}
                    />
                );
            })}
        </Stack>
    );
}

