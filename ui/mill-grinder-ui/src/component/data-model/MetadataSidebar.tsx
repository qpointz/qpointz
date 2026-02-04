import { Box, ScrollArea, Button } from "@mantine/core";
import { useMantineTheme } from "@mantine/core";
import { useMediaQuery } from "@mantine/hooks";
import { TbLayoutSidebarRightExpand, TbLayoutSidebarLeftExpand } from "react-icons/tb";
import { useState } from "react";
import { useMetadataContext } from "./MetadataProvider";
import { getSchemas, getTablesWithColumns } from "./utils/entityUtils";
import TopNavigation from "./components/TopNavigation";
import SidebarNavSection from "./components/SidebarNavSection";
import SchemaList from "./components/tree/SchemaList";
import TableList from "./components/tree/TableList";
import ScopeSelector from "./components/ScopeSelector";

export function MetadataSidebar() {
    const theme = useMantineTheme();
    const { tree, entity, scope, schema } = useMetadataContext();
    const [isCollapsed, setIsCollapsed] = useState(false);
    const [expandedTables, setExpandedTables] = useState<Set<string>>(new Set());
    const isMobile = useMediaQuery('(max-width: 768px)');
    
    // Auto-collapse on mobile only, manual toggle on tablet and desktop
    const shouldCollapse = isMobile || isCollapsed;
    
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

    return (
        <Box 
            w={shouldCollapse ? 50 : 350} 
            style={{ 
                height: "100vh", 
                boxSizing: "border-box", 
                borderRight: `1px solid ${theme.colors.gray[3]}`,
                transition: 'width 0.3s ease, border 0.3s ease',
                overflow: 'hidden',
                position: 'relative'
            }}
        >
            {/* Toggle Button - only show on tablet and larger */}
            {!isMobile && (
                <Button
                    variant="subtle"
                    size="xs"
                    pos="absolute"
                    top={10}
                    right={shouldCollapse ? 0 : -5}
                    onClick={() => setIsCollapsed(!isCollapsed)}
                    style={{ 
                        transition: 'right 0.3s ease',
                        zIndex: 10
                    }}
                >
                    {shouldCollapse ? <TbLayoutSidebarLeftExpand size={20} /> : <TbLayoutSidebarRightExpand size={20} />}
                </Button>
            )}

            <ScrollArea type="hover" style={{ minHeight: "100%", height: "100vh" }}>
                <Box m={0} pl={shouldCollapse ? 5 : 10} pr={shouldCollapse ? 5 : 10} pt={40} style={{ width: "100%", boxSizing: "border-box" }}>
                    
                    {/* Top Navigation */}
                    <TopNavigation collapsed={shouldCollapse} />
                    
                    {!shouldCollapse && (
                        <SidebarNavSection collapsed={shouldCollapse}>
                            {/* Schemas and Tables */}
                            <Box mt={15}>
                                <SchemaList
                                    schemas={schemas}
                                    selectedSchema={schema.selected}
                                    loading={tree.loading}
                                    onSchemaSelect={handleSchemaSelect}
                                    collapsed={shouldCollapse}
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
                                    collapsed={shouldCollapse}
                                />
                            </Box>

                            {/* Scope Selector */}
                            <ScopeSelector
                                currentScope={scope.current}
                                onScopeChange={scope.set}
                                collapsed={shouldCollapse}
                            />
                        </SidebarNavSection>
                    )}
                </Box>
            </ScrollArea>
        </Box>
    );
}
