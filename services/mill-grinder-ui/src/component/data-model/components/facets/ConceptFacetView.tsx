import {
    Card,
    Stack,
    Group,
    Text,
    Badge,
    Divider,
    Code,
} from "@mantine/core";
import { TbBulb, TbTag, TbDatabase, TbColumns, TbCode } from "react-icons/tb";
import type { ReactNode } from "react";

interface ConceptFacetViewProps {
    data: any;
    toggleButton?: ReactNode;
}

export default function ConceptFacetView({ data, toggleButton }: ConceptFacetViewProps) {
    // Handle both direct data and scoped data (global, user:xxx, etc.)
    const facetData = data?.global || data;
    const concepts = facetData?.concepts || [];

    const formatTarget = (target: any): string => {
        if (!target) return '';
        const parts = [target.schema, target.table];
        if (target.attribute) {
            parts.push(target.attribute);
        }
        return parts.filter(Boolean).join('.');
    };

    const getSourceColor = (source?: string): string => {
        switch (source?.toUpperCase()) {
            case 'MANUAL':
                return 'blue';
            case 'INFERRED':
                return 'green';
            case 'NL2SQL':
                return 'purple';
            default:
                return 'gray';
        }
    };

    if (concepts.length === 0) {
        return (
            <Card withBorder>
                <Text c="dimmed" size="sm" ta="center" py="md">
                    No concepts defined
                </Text>
            </Card>
        );
    }

    return (
        <Stack gap="md">
            {concepts.map((concept: any, index: number) => (
                <Card key={index} withBorder>
                    <Stack gap="sm">
                        {/* Header with Concept Name and Toggle (only on first concept) */}
                        {concept.name && (
                            <Group justify="space-between" align="flex-start">
                                <div style={{ flex: 1 }}>
                                    <Group gap={4} mb={4}>
                                        <TbBulb size={18} />
                                        <Text size="sm" c="dimmed">Concept</Text>
                                    </Group>
                                    <Text fw={600} size="lg">{concept.name}</Text>
                                </div>
                                {index === 0 && toggleButton && (
                                    <div style={{ marginTop: 4 }}>
                                        {toggleButton}
                                    </div>
                                )}
                            </Group>
                        )}
                        {!concept.name && index === 0 && toggleButton && (
                            <Group justify="flex-end">
                                {toggleButton}
                            </Group>
                        )}

                        {/* Description */}
                        {concept.description && (
                            <Text size="sm" c="dimmed">{concept.description}</Text>
                        )}

                        <Divider />

                        {/* SQL Definition */}
                        {concept.sql && (
                            <div>
                                <Group gap={4} mb={4}>
                                    <TbCode size={16} />
                                    <Text size="sm" c="dimmed">SQL Definition</Text>
                                </Group>
                                <Code block style={{ fontSize: '12px' }}>{concept.sql}</Code>
                            </div>
                        )}

                        {/* Targets */}
                        {concept.targets && concept.targets.length > 0 && (
                            <div>
                                <Group gap={4} mb={8}>
                                    <TbDatabase size={16} />
                                    <Text size="sm" c="dimmed">Targets</Text>
                                </Group>
                                <Stack gap={4}>
                                    {concept.targets.map((target: any, i: number) => (
                                        <Group key={i} gap="xs" align="flex-start">
                                            <TbColumns size={14} style={{ marginTop: 2 }} />
                                            <Text size="sm" style={{ flex: 1 }}>
                                                {formatTarget(target)}
                                            </Text>
                                            {target.attributes && target.attributes.length > 0 && (
                                                <Group gap={4}>
                                                    {target.attributes.map((attr: string, j: number) => (
                                                        <Badge key={j} variant="outline" size="xs">
                                                            {attr}
                                                        </Badge>
                                                    ))}
                                                </Group>
                                            )}
                                        </Group>
                                    ))}
                                </Stack>
                            </div>
                        )}

                        {/* Metadata */}
                        <Group gap="sm">
                            {concept.category && (
                                <Badge variant="light" color="blue">
                                    {concept.category}
                                </Badge>
                            )}
                            {concept.source && (
                                <Badge 
                                    variant="light" 
                                    color={getSourceColor(concept.source)}
                                >
                                    {concept.source}
                                </Badge>
                            )}
                            {concept.sourceSession && (
                                <Badge variant="outline" size="sm">
                                    Session: {concept.sourceSession}
                                </Badge>
                            )}
                        </Group>

                        {/* Tags */}
                        {concept.tags && concept.tags.length > 0 && (
                            <div>
                                <Group gap={4} mb={4}>
                                    <TbTag size={16} />
                                    <Text size="sm" c="dimmed">Tags</Text>
                                </Group>
                                <Group gap={4}>
                                    {concept.tags.map((tag: string, i: number) => (
                                        <Badge key={i} variant="light" color="blue" size="sm">
                                            #{tag}
                                        </Badge>
                                    ))}
                                </Group>
                            </div>
                        )}
                    </Stack>
                </Card>
            ))}
        </Stack>
    );
}

