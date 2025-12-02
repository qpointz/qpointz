import {Box, NavLink, ScrollArea, UnstyledButton, useMantineTheme, Button, Divider, Text, Select, Group, Collapse} from "@mantine/core";
import {useMediaQuery} from "@mantine/hooks";
import {TbLayoutSidebarRightExpand, TbLayoutSidebarLeftExpand, TbCompass, TbDatabase, TbTerminal2, TbTable, TbColumns, TbChevronRight, TbChevronDown} from "react-icons/tb";
import {Link, useLocation, useNavigate} from "react-router";
import {useState} from "react";
import {useMetadataContext} from "./MetadataProvider";
import type {TreeNodeDto} from "../../api/mill/api.ts";

/**
 * Get all tables with their children (columns) from tree nodes
 */
function getTablesWithColumns(nodes: TreeNodeDto[]): TreeNodeDto[] {
    const tables: TreeNodeDto[] = [];
    for (const node of nodes) {
        if (node.type === 'TABLE') {
            tables.push(node);
        } else if (node.children) {
            // Recursively search in children
            tables.push(...getTablesWithColumns(node.children));
        }
    }
    return tables;
}

/**
 * Extract schema and table name from entity ID
 * IDs are in format: schema.table or schema.table.attribute
 */
function parseEntityId(entityId: string | undefined): { schema?: string; table?: string; attribute?: string } {
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

export function MetadataSidebar() {
    const theme = useMantineTheme();
    const {tree, entity, scope} = useMetadataContext();
    const [isCollapsed, setIsCollapsed] = useState(false);
    const [expandedTables, setExpandedTables] = useState<Set<string>>(new Set());
    const isMobile = useMediaQuery('(max-width: 768px)');
    const location = useLocation();
    const navigate = useNavigate();
    
    // Auto-collapse on mobile only, manual toggle on tablet and desktop
    const shouldCollapse = isMobile || isCollapsed;
    
    // Get tables with their columns
    const tables = getTablesWithColumns(tree.data);
    
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
    
    // Get columns for a table
    const getColumns = (table: TreeNodeDto): TreeNodeDto[] => {
        return table.children?.filter(child => child.type === 'ATTRIBUTE') || [];
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
                    
                    {/* Explore Button */}
                    <Box p={1} mb={shouldCollapse ? 4 : 8} bg="transparent" style={{borderRadius: 6}} key="explore">
                        {shouldCollapse ? (
                            <UnstyledButton
                                component={Link}
                                to="/explore"
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    width: '100%',
                                    padding: '6px',
                                    borderRadius: 6,
                                }}
                                onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
                                onMouseLeave={e => e.currentTarget.style.background = ''}
                            >
                                <TbCompass size={20} />
                            </UnstyledButton>
                        ) : (
                            <NavLink 
                                to="/explore" 
                                component={Link} 
                                label="Explore" 
                                p={0} 
                                m={0} 
                                leftSection={<TbCompass size={20} />}
                                active={location.pathname.startsWith('/explore')}
                            />
                        )}
                    </Box>
                    
                    {/* Data Button */}
                    <Box p={1} mb={shouldCollapse ? 4 : 8} bg="transparent" style={{borderRadius: 6}} key="data">
                        {shouldCollapse ? (
                            <UnstyledButton
                                component={Link}
                                to="/data"
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    width: '100%',
                                    padding: '6px',
                                    borderRadius: 6,
                                }}
                                onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
                                onMouseLeave={e => e.currentTarget.style.background = ''}
                            >
                                <TbDatabase size={20} />
                            </UnstyledButton>
                        ) : (
                            <NavLink 
                                to="/data" 
                                component={Link} 
                                label="Data" 
                                p={0} 
                                m={0} 
                                leftSection={<TbDatabase size={20} />}
                                active={location.pathname.startsWith('/data')}
                            />
                        )}
                    </Box>

                    {/* Chat Button */}
                    <Box p={1} mb={shouldCollapse ? 4 : 8} bg="transparent" style={{borderRadius: 6}} key="chat">
                        {shouldCollapse ? (
                            <UnstyledButton
                                component={Link}
                                to="/chat"
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    width: '100%',
                                    padding: '6px',
                                    borderRadius: 6,
                                }}
                                onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
                                onMouseLeave={e => e.currentTarget.style.background = ''}
                            >
                                <TbTerminal2 size={20} />
                            </UnstyledButton>
                        ) : (
                            <NavLink 
                                to="/chat" 
                                component={Link} 
                                label="Chat" 
                                p={0} 
                                m={0} 
                                leftSection={<TbTerminal2 size={20} />}
                                active={location.pathname.startsWith('/chat')}
                            />
                        )}
                    </Box>
                    
                    {!shouldCollapse && (
                        <>
                            <Divider mt={15}/>
                            
                            <Text mt={15} size="sm" color="gray.5">Tables</Text>

                            {tree.loading ? (
                                <Text mt={10} size="sm" c="dimmed">Loading...</Text>
                            ) : tables.length === 0 ? (
                                <Text mt={10} size="sm" c="dimmed">No tables found</Text>
                            ) : (
                                tables.map((table) => {
                                    const columns = getColumns(table);
                                    const hasColumns = columns.length > 0;
                                    const isExpanded = table.id ? expandedTables.has(table.id) : false;
                                    const isTableSelected = entity.selected?.id === table.id;
                                    
                                    return (
                                        <Box 
                                            m={0} 
                                            mt={6} 
                                            p={0} 
                                            bg={isTableSelected ? "gray.3" : "transparent"} 
                                            style={{borderRadius: 6}} 
                                            key={table.id}
                                        >
                                            <NavLink
                                                component="div"
                                                label={table.name || parseEntityId(table.id).table || table.id}
                                                p={3} 
                                                m={0}
                                                leftSection={
                                                    <Group gap={4}>
                                                        {hasColumns && (
                                                            isExpanded ? <TbChevronDown size={12} /> : <TbChevronRight size={12} />
                                                        )}
                                                        <TbTable size={14} />
                                                    </Group>
                                                }
                                                rightSection={hasColumns ? (
                                                    <Text size="xs" c="dimmed">{columns.length}</Text>
                                                ) : undefined}
                                                style={{
                                                    borderRadius: 8,
                                                    marginBottom: 4,
                                                    background: isTableSelected ? theme.colors.blue[0] : undefined,
                                                    transition: 'background 0.2s',
                                                    cursor: 'pointer',
                                                }}
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    if (hasColumns && table.id) {
                                                        toggleTable(table.id);
                                                    }
                                                    if (table.id) {
                                                        const location = parseEntityId(table.id);
                                                        if (location.schema && location.table) {
                                                            navigate(`/explore/${location.schema}/${location.table}`);
                                                        } else {
                                                            entity.select(table.id);
                                                        }
                                                    }
                                                }}
                                                onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
                                                onMouseLeave={e => e.currentTarget.style.background = isTableSelected ? theme.colors.blue[0] : ''}
                                            />
                                            {hasColumns && (
                                                <Collapse in={isExpanded}>
                                                    <Box pl={20} pt={4}>
                                                        {columns.map((column) => {
                                                            const isColumnSelected = entity.selected?.id === column.id;
                                                            return (
                                                                <Box
                                                                    key={column.id}
                                                                    p={3}
                                                                    m={0}
                                                                    mb={2}
                                                                    style={{
                                                                        borderRadius: 6,
                                                                        background: isColumnSelected ? theme.colors.blue[0] : undefined,
                                                                        transition: 'background 0.2s',
                                                                        cursor: 'pointer',
                                                                    }}
                                                                    onClick={(e) => {
                                                                        e.stopPropagation();
                                                                        if (column.id) {
                                                                            const location = parseEntityId(column.id);
                                                                            if (location.schema && location.table && location.attribute) {
                                                                                navigate(`/explore/${location.schema}/${location.table}/${location.attribute}`);
                                                                            } else {
                                                                                entity.select(column.id);
                                                                            }
                                                                        }
                                                                    }}
                                                                    onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
                                                                    onMouseLeave={e => e.currentTarget.style.background = isColumnSelected ? theme.colors.blue[0] : ''}
                                                                >
                                                                    <Group gap="xs">
                                                                        <TbColumns size={12} />
                                                                        <Text size="sm" fw={isColumnSelected ? 600 : 400}>
                                                                            {column.name || parseEntityId(column.id).attribute || column.id}
                                                                        </Text>
                                                                    </Group>
                                                                </Box>
                                                            );
                                                        })}
                                                    </Box>
                                                </Collapse>
                                            )}
                                        </Box>
                                    );
                                })
                            )}
                            
                            {/* Scope Selector */}
                            <Box mt={20} mb={15}>
                                <Group gap="xs" mb={8}>
                                    <Text size="sm" c="dimmed">Scope:</Text>
                                </Group>
                                <Select
                                    value={scope.current}
                                    onChange={(value) => {
                                        if (value) {
                                            scope.set(value);
                                        }
                                    }}
                                    data={[
                                        { value: 'global', label: 'Global' },
                                        { value: 'user:default', label: 'User' },
                                    ]}
                                    size="sm"
                                />
                            </Box>
                        </>
                    )}
                </Box>
            </ScrollArea>
        </Box>
    );
}

