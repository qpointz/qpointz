import {
    Card,
    Stack,
    Group,
    Text,
    Badge,
    Divider,
    Code,
} from "@mantine/core";
import { TbArrowRight, TbKey, TbLink, TbHierarchy2 } from "react-icons/tb";
import type { ReactNode } from "react";

interface RelationFacetViewProps {
    data: any;
    toggleButton?: ReactNode;
}

export default function RelationFacetView({ data, toggleButton }: RelationFacetViewProps) {
    // Handle both direct data and scoped data (global, user:xxx, etc.)
    const facetData = data?.global || data;
    const relations = facetData?.relations || [];

    const formatEntityReference = (ref: any): string => {
        if (!ref) return '';
        if (ref.attribute) {
            return `${ref.schema}.${ref.table}.${ref.attribute}`;
        }
        return `${ref.schema}.${ref.table}`;
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
            <Card withBorder>
                <Text c="dimmed" size="sm" ta="center" py="md">
                    No relations defined
                </Text>
            </Card>
        );
    }

    return (
        <Stack gap="md">
            {relations.map((relation: any, index: number) => (
                <Card key={index} withBorder>
                    <Stack gap="sm">
                        {/* Header with Relation Name and Toggle (only on first relation) */}
                        {relation.name && (
                            <Group justify="space-between" align="flex-start">
                                <div style={{ flex: 1 }}>
                                    <Text size="sm" c="dimmed" mb={4}>Relation</Text>
                                    <Text fw={600} size="lg">{relation.name}</Text>
                                </div>
                                {index === 0 && toggleButton && (
                                    <div style={{ marginTop: 4 }}>
                                        {toggleButton}
                                    </div>
                                )}
                            </Group>
                        )}
                        {!relation.name && index === 0 && toggleButton && (
                            <Group justify="flex-end">
                                {toggleButton}
                            </Group>
                        )}

                        {/* Description */}
                        {relation.description && (
                            <Text size="sm" c="dimmed">{relation.description}</Text>
                        )}

                        <Divider />

                        {/* Source → Target */}
                        {relation.sourceTable && relation.targetTable && (
                            <div>
                                <Text size="sm" c="dimmed" mb={8}>Relationship</Text>
                                <Group gap="sm" align="center" wrap="nowrap">
                                    <div style={{ flex: 1 }}>
                                        <Text size="xs" c="dimmed" mb={2}>Source</Text>
                                        <Group gap={4}>
                                            <TbKey size={14} />
                                            <Text size="sm" fw={500}>{formatEntityReference(relation.sourceTable)}</Text>
                                        </Group>
                                        {relation.sourceAttributes && relation.sourceAttributes.length > 0 && (
                                            <Group gap={4} mt={4}>
                                                {relation.sourceAttributes.map((attr: string, i: number) => (
                                                    <Badge key={i} variant="outline" size="xs">{attr}</Badge>
                                                ))}
                                            </Group>
                                        )}
                                    </div>
                                    <TbArrowRight size={20} style={{ flexShrink: 0 }} />
                                    <div style={{ flex: 1 }}>
                                        <Text size="xs" c="dimmed" mb={2}>Target</Text>
                                        <Group gap={4}>
                                            <TbLink size={14} />
                                            <Text size="sm" fw={500}>{formatEntityReference(relation.targetTable)}</Text>
                                        </Group>
                                        {relation.targetAttributes && relation.targetAttributes.length > 0 && (
                                            <Group gap={4} mt={4}>
                                                {relation.targetAttributes.map((attr: string, i: number) => (
                                                    <Badge key={i} variant="outline" size="xs">{attr}</Badge>
                                                ))}
                                            </Group>
                                        )}
                                    </div>
                                </Group>
                            </div>
                        )}

                        {/* Metadata */}
                        <Group gap="sm">
                            {relation.cardinality && (
                                <Badge 
                                    variant="light" 
                                    color={getCardinalityColor(relation.cardinality)}
                                    leftSection={<TbHierarchy2 size={12} />}
                                >
                                    {relation.cardinality}
                                </Badge>
                            )}
                            {relation.type && (
                                <Badge 
                                    variant="light" 
                                    color={getRelationTypeColor(relation.type)}
                                >
                                    {relation.type}
                                </Badge>
                            )}
                        </Group>

                        {/* Join SQL */}
                        {relation.joinSql && (
                            <div>
                                <Text size="sm" c="dimmed" mb={4}>Join SQL</Text>
                                <Code block style={{ fontSize: '12px' }}>{relation.joinSql}</Code>
                            </div>
                        )}

                        {/* Business Meaning */}
                        {relation.businessMeaning && (
                            <div>
                                <Text size="sm" c="dimmed" mb={4}>Business Meaning</Text>
                                <Text size="sm">{relation.businessMeaning}</Text>
                            </div>
                        )}
                    </Stack>
                </Card>
            ))}
        </Stack>
    );
}

