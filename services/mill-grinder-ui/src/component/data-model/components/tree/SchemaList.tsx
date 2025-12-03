import { Box, NavLink, Text } from "@mantine/core";
import { TbDatabase } from "react-icons/tb";
import { useMantineTheme } from "@mantine/core";
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
    const theme = useMantineTheme();

    if (collapsed) {
        return null;
    }

    if (loading) {
        return <Text mt={10} size="sm" c="dimmed">Loading...</Text>;
    }

    if (schemas.length === 0) {
        return <Text mt={10} size="sm" c="dimmed">No schemas found</Text>;
    }

    return (
        <>
            <Text size="sm" color="gray.5">Schemas</Text>
            {schemas.map((schemaNode) => {
                const schemaName = schemaNode.name || schemaNode.displayName || schemaNode.id || '';
                const isSchemaSelected = selectedSchema === schemaName;
                
                return (
                    <Box 
                        m={0} 
                        mt={6} 
                        p={0} 
                        bg={isSchemaSelected ? "gray.3" : "transparent"} 
                        style={{ borderRadius: 6 }} 
                        key={schemaNode.id}
                    >
                        <NavLink
                            component="div"
                            label={schemaName}
                            p={3} 
                            m={0}
                            leftSection={<TbDatabase size={14} />}
                            style={{
                                borderRadius: 8,
                                marginBottom: 4,
                                background: isSchemaSelected ? theme.colors.blue[0] : undefined,
                                transition: 'background 0.2s',
                                cursor: 'pointer',
                            }}
                            onClick={(e) => {
                                e.stopPropagation();
                                if (schemaName) {
                                    onSchemaSelect(schemaName, schemaNode.id);
                                }
                            }}
                            onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
                            onMouseLeave={e => e.currentTarget.style.background = isSchemaSelected ? theme.colors.blue[0] : ''}
                        />
                    </Box>
                );
            })}
        </>
    );
}

