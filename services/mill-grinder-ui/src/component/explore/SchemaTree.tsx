/**
 * Schema Tree Component
 * 
 * Displays a hierarchical tree view of schemas → tables → attributes.
 * Allows selection of nodes to view entity details.
 */
import { useState } from "react";
import { useMetadataContext } from "./MetadataProvider";
import { Box, ScrollArea, Text, Loader, Collapse, Group, ActionIcon } from "@mantine/core";
import { TbChevronRight, TbChevronDown, TbDatabase, TbTable, TbColumns } from "react-icons/tb";
import type { TreeNodeDto } from "../../api/mill/api.ts";

interface TreeNodeProps {
    node: TreeNodeDto;
    level: number;
    selectedId?: string;
    onSelect: (node: TreeNodeDto) => void;
}

const TreeNode: React.FC<TreeNodeProps> = ({ node, level, selectedId, onSelect }) => {
    const [opened, setOpened] = useState(level === 0); // Auto-expand first level
    const hasChildren = node.children && node.children.length > 0;
    const isSelected = node.id === selectedId;

    const handleClick = () => {
        if (hasChildren) {
            setOpened(!opened);
        }
        if (node.id) {
            onSelect(node);
        }
    };

    const getIcon = () => {
        switch (node.type) {
            case 'SCHEMA':
                return <TbDatabase size={16} />;
            case 'TABLE':
                return <TbTable size={16} />;
            case 'ATTRIBUTE':
                return <TbColumns size={16} />;
            default:
                return null;
        }
    };

    return (
        <Box>
            <Group
                gap="xs"
                p="xs"
                style={{
                    paddingLeft: `${level * 16 + 8}px`,
                    cursor: 'pointer',
                    backgroundColor: isSelected ? 'var(--mantine-color-primary-1)' : 'transparent',
                    borderRadius: '4px',
                }}
                onClick={handleClick}
                className="tree-node"
            >
                {hasChildren && (
                    <ActionIcon variant="subtle" size="sm">
                        {opened ? <TbChevronDown size={14} /> : <TbChevronRight size={14} />}
                    </ActionIcon>
                )}
                {!hasChildren && <Box w={20} />}
                {getIcon()}
                <Text size="sm" fw={isSelected ? 600 : 400}>
                    {node.displayName || node.name || node.id}
                </Text>
            </Group>
            {hasChildren && (
                <Collapse in={opened}>
                    {node.children?.map((child) => (
                        <TreeNode
                            key={child.id}
                            node={child}
                            level={level + 1}
                            selectedId={selectedId}
                            onSelect={onSelect}
                        />
                    ))}
                </Collapse>
            )}
        </Box>
    );
};

export default function SchemaTree() {
    const { tree, entity } = useMetadataContext();

    const handleNodeSelect = (node: TreeNodeDto) => {
        if (node.id) {
            entity.select(node.id);
        }
    };

    if (tree.loading) {
        return (
            <Box p="md" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                <Loader size="sm" />
            </Box>
        );
    }

    if (tree.data.length === 0) {
        return (
            <Box p="md">
                <Text c="dimmed" size="sm">No schemas found</Text>
            </Box>
        );
    }

    return (
        <ScrollArea h="100%">
            <Box p="xs">
                {tree.data.map((node) => (
                    <TreeNode
                        key={node.id}
                        node={node}
                        level={0}
                        selectedId={entity.selected?.id}
                        onSelect={handleNodeSelect}
                    />
                ))}
            </Box>
        </ScrollArea>
    );
}

