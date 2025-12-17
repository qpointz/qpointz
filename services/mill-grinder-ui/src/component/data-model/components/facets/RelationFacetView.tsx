import {
    Stack,
    Group,
    Text,
    Badge,
    Box,
    Tooltip,
    UnstyledButton,
} from "@mantine/core";
import { TbArrowRight, TbKey, TbLink, TbHierarchy2, TbArrowsJoin2 } from "react-icons/tb";
import type { ReactNode } from "react";
import { useState } from "react";

interface RelationFacetViewProps {
    data: any;
    toggleButton?: ReactNode;
}

export default function RelationFacetView({ data, toggleButton }: RelationFacetViewProps) {
    // Handle both direct data and scoped data (global, user:xxx, etc.)
    const facetData = data?.global || data;
    const relations = facetData?.relations || [];
    const [expandedIndex, setExpandedIndex] = useState<number | null>(null);

    const formatEntityReference = (ref: any): string => {
        if (!ref) return '';
        if (ref.attribute) {
            return `${ref.schema}.${ref.table}.${ref.attribute}`;
        }
        return `${ref.schema}.${ref.table}`;
    };

    const formatShortRef = (ref: any): string => {
        if (!ref) return '';
        return ref.table || '';
    };

    const getCardinalityColor = (cardinality?: string): string => {
        switch (cardinality?.toUpperCase()) {
            case 'ONE_TO_ONE':
                return 'blue';
            case 'ONE_TO_MANY':
                return 'green';
            case 'MANY_TO_ONE':
                return 'orange';
            case 'MANY_TO_MANY':
                return 'purple';
            default:
                return 'gray';
        }
    };

    const formatCardinality = (cardinality?: string): string => {
        switch (cardinality?.toUpperCase()) {
            case 'ONE_TO_ONE':
                return '1:1';
            case 'ONE_TO_MANY':
                return '1:N';
            case 'MANY_TO_ONE':
                return 'N:1';
            case 'MANY_TO_MANY':
                return 'N:N';
            default:
                return cardinality || '';
        }
    };

    const getRelationTypeColor = (type?: string): string => {
        switch (type?.toUpperCase()) {
            case 'FOREIGN_KEY':
                return 'red';
            case 'LOGICAL':
                return 'blue';
            case 'HIERARCHICAL':
                return 'green';
            default:
                return 'gray';
        }
    };

    if (relations.length === 0) {
        return (
            <Group justify="space-between">
                <Text c="dimmed" size="sm">No relations defined</Text>
                {toggleButton}
            </Group>
        );
    }

    return (
        <Stack gap="xs">
            {/* Toggle button */}
            {toggleButton && (
                <Group justify="flex-end">
                    {toggleButton}
                </Group>
            )}

            {/* Compact relation list */}
            {relations.map((relation: any, index: number) => {
                const isExpanded = expandedIndex === index;
                const hasDetails = relation.description || relation.businessMeaning || relation.joinSql ||
                    (relation.sourceAttributes?.length > 0) || (relation.targetAttributes?.length > 0);

                return (
                    <Box key={index}>
                        <UnstyledButton
                            w="100%"
                            onClick={() => hasDetails && setExpandedIndex(isExpanded ? null : index)}
                            style={{ 
                                cursor: hasDetails ? 'pointer' : 'default',
                                borderRadius: 'var(--mantine-radius-sm)',
                                border: '1px solid var(--mantine-color-gray-2)',
                            }}
                            styles={{
                                root: {
                                    '&:hover': {
                                        backgroundColor: 'var(--mantine-color-gray-0)',
                                        borderColor: 'var(--mantine-color-gray-4)',
                                    },
                                },
                            }}
                        >
                            <Group gap="xs" p="xs" wrap="nowrap" justify="space-between">
                                {/* Left side: Icon + Source → Target */}
                                <Group gap="sm" wrap="nowrap" style={{ flex: 1, minWidth: 0 }}>
                                    {/* Relation icon */}
                                    <TbArrowsJoin2 size={16} style={{ flexShrink: 0, color: 'var(--mantine-color-primary-5)' }} />
                                    
                                    {/* Relation name or source */}
                                    {relation.name ? (
                                        <Text size="sm" fw={500} truncate>{relation.name}</Text>
                                    ) : (
                                        <Group gap={4} wrap="nowrap">
                                            <Tooltip label={formatEntityReference(relation.sourceTable)} withArrow>
                                                <Group gap={2} wrap="nowrap">
                                                    <TbKey size={12} style={{ flexShrink: 0 }} />
                                                    <Text size="xs" fw={500} truncate>{formatShortRef(relation.sourceTable)}</Text>
                                                </Group>
                                            </Tooltip>
                                            <TbArrowRight size={14} style={{ flexShrink: 0, color: 'var(--mantine-color-dimmed)' }} />
                                            <Tooltip label={formatEntityReference(relation.targetTable)} withArrow>
                                                <Group gap={2} wrap="nowrap">
                                                    <TbLink size={12} style={{ flexShrink: 0 }} />
                                                    <Text size="xs" fw={500} truncate>{formatShortRef(relation.targetTable)}</Text>
                                                </Group>
                                            </Tooltip>
                                        </Group>
                                    )}
                                </Group>

                                {/* Right side: Badges */}
                                <Group gap={4} wrap="nowrap">
                                    {relation.cardinality && (
                                        <Tooltip label={relation.cardinality} withArrow>
                                            <Badge 
                                                variant="light" 
                                                color={getCardinalityColor(relation.cardinality)}
                                                size="xs"
                                                leftSection={<TbHierarchy2 size={10} />}
                                            >
                                                {formatCardinality(relation.cardinality)}
                                            </Badge>
                                        </Tooltip>
                                    )}
                                    {relation.type && (
                                        <Badge 
                                            variant="outline" 
                                            color={getRelationTypeColor(relation.type)}
                                            size="xs"
                                        >
                                            {relation.type === 'FOREIGN_KEY' ? 'FK' : relation.type}
                                        </Badge>
                                    )}
                                </Group>
                            </Group>
                        </UnstyledButton>

                        {/* Expanded details */}
                        {isExpanded && hasDetails && (
                            <Box 
                                ml="xs" 
                                mt="xs" 
                                p="sm" 
                                style={{ 
                                    borderRadius: 'var(--mantine-radius-sm)',
                                    border: '1px solid var(--mantine-color-gray-3)',
                                }}
                            >
                                <Stack gap="sm">
                                    {/* Full source → target path */}
                                    {relation.sourceTable && relation.targetTable && (
                                        <Box>
                                            <Text size="xs" fw={500} c="dimmed" mb={4}>Path</Text>
                                            <Group gap="xs" wrap="wrap" align="center">
                                                <Badge variant="light" color="blue" size="sm">
                                                    {formatEntityReference(relation.sourceTable)}
                                                </Badge>
                                                {relation.sourceAttributes?.length > 0 && (
                                                    <Text size="xs" c="dimmed">({relation.sourceAttributes.join(', ')})</Text>
                                                )}
                                                <TbArrowRight size={14} style={{ color: 'var(--mantine-color-primary-5)' }} />
                                                <Badge variant="light" color="green" size="sm">
                                                    {formatEntityReference(relation.targetTable)}
                                                </Badge>
                                                {relation.targetAttributes?.length > 0 && (
                                                    <Text size="xs" c="dimmed">({relation.targetAttributes.join(', ')})</Text>
                                                )}
                                            </Group>
                                        </Box>
                                    )}
                                    
                                    {/* Description */}
                                    {relation.description && (
                                        <Box>
                                            <Text size="xs" fw={500} c="dimmed" mb={4}>Description</Text>
                                            <Text size="sm">{relation.description}</Text>
                                        </Box>
                                    )}
                                    
                                    {/* Business Meaning */}
                                    {relation.businessMeaning && (
                                        <Box>
                                            <Text size="xs" fw={500} c="dimmed" mb={4}>Business Meaning</Text>
                                            <Text size="sm" fs="italic" c="dark.4">{relation.businessMeaning}</Text>
                                        </Box>
                                    )}
                                    
                                    {/* Join SQL */}
                                    {relation.joinSql && (
                                        <Box>
                                            <Text size="xs" fw={500} c="dimmed" mb={4}>Join SQL</Text>
                                            <Box 
                                                p="xs" 
                                                bg="dark.8" 
                                                style={{ borderRadius: 'var(--mantine-radius-sm)' }}
                                            >
                                                <Text size="xs" ff="monospace" c="gray.4">{relation.joinSql}</Text>
                                            </Box>
                                        </Box>
                                    )}
                                </Stack>
                            </Box>
                        )}
                    </Box>
                );
            })}
        </Stack>
    );
}
