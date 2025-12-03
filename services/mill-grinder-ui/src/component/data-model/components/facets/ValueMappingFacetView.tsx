import {
    Card,
    Stack,
    Group,
    Text,
    Badge,
    Divider,
    Code,
    Table,
} from "@mantine/core";
import { TbDatabase, TbCode, TbClock, TbCheck, TbX } from "react-icons/tb";
import type { ReactNode } from "react";

interface ValueMappingFacetViewProps {
    data: any;
    toggleButton?: ReactNode;
}

export default function ValueMappingFacetView({ data, toggleButton }: ValueMappingFacetViewProps) {
    // Handle both direct merged data and scoped data (global, user:xxx, etc.)
    const facetData = data?.global || data;
    
    const context = facetData?.context;
    const similarityThreshold = facetData?.['similarity-threshold'] || facetData?.similarityThreshold;
    const sources = facetData?.sources || [];

    const getSourceTypeColor = (type?: string): string => {
        switch (type?.toUpperCase()) {
            case 'SQL_QUERY':
                return 'blue';
            case 'API':
                return 'green';
            case 'VECTOR_STORE':
                return 'purple';
            default:
                return 'gray';
        }
    };

    return (
        <Card withBorder>
            <Stack gap="md">
                {/* Header with Context and Toggle */}
                {context && (
                    <Group justify="space-between" align="flex-start">
                        <div style={{ flex: 1 }}>
                            <Text size="sm" c="dimmed" mb={4}>Context</Text>
                            <Text fw={600} size="lg">{context}</Text>
                        </div>
                        {toggleButton && (
                            <div style={{ marginTop: 4 }}>
                                {toggleButton}
                            </div>
                        )}
                    </Group>
                )}
                {!context && toggleButton && (
                    <Group justify="flex-end">
                        {toggleButton}
                    </Group>
                )}

                {/* Similarity Threshold */}
                {similarityThreshold !== undefined && similarityThreshold !== null && (
                    <div>
                        <Text size="sm" c="dimmed" mb={4}>Similarity Threshold</Text>
                        <Badge variant="light" color="blue" size="lg">
                            {similarityThreshold}
                        </Badge>
                    </div>
                )}

                <Divider />

                {/* Sources */}
                {sources.length > 0 && (
                    <div>
                        <Group gap={4} mb={8}>
                            <TbDatabase size={16} />
                            <Text size="sm" c="dimmed">Mapping Sources</Text>
                        </Group>
                        <Stack gap="md">
                            {sources.map((source: any, index: number) => (
                                <Card key={index} withBorder p="sm">
                                    <Stack gap="sm">
                                        {/* Source Header */}
                                        <Group justify="space-between" align="flex-start">
                                            <div style={{ flex: 1 }}>
                                                {source.name && (
                                                    <Text fw={600} size="md" mb={4}>{source.name}</Text>
                                                )}
                                                {source.description && (
                                                    <Text size="sm" c="dimmed">{source.description}</Text>
                                                )}
                                            </div>
                                            <Group gap="xs">
                                                {source.type && (
                                                    <Badge 
                                                        variant="light" 
                                                        color={getSourceTypeColor(source.type)}
                                                    >
                                                        {source.type}
                                                    </Badge>
                                                )}
                                                {source.enabled !== undefined && (
                                                    <Badge 
                                                        variant={source.enabled ? "light" : "outline"}
                                                        color={source.enabled ? "green" : "gray"}
                                                        leftSection={source.enabled ? <TbCheck size={12} /> : <TbX size={12} />}
                                                    >
                                                        {source.enabled ? "Enabled" : "Disabled"}
                                                    </Badge>
                                                )}
                                            </Group>
                                        </Group>

                                        {/* SQL Definition */}
                                        {source.definition && (
                                            <div>
                                                <Group gap={4} mb={4}>
                                                    <TbCode size={14} />
                                                    <Text size="xs" c="dimmed">SQL Query</Text>
                                                </Group>
                                                <Code block style={{ fontSize: '11px' }}>
                                                    {source.definition}
                                                </Code>
                                            </div>
                                        )}

                                        {/* Cron Expression */}
                                        {source.cronExpression && (
                                            <Group gap={4}>
                                                <TbClock size={14} />
                                                <Text size="xs" c="dimmed">Schedule:</Text>
                                                <Text size="xs">{source.cronExpression}</Text>
                                            </Group>
                                        )}
                                    </Stack>
                                </Card>
                            ))}
                        </Stack>
                    </div>
                )}

                {/* Empty State */}
                {!context && similarityThreshold === undefined && sources.length === 0 && (
                    <Text c="dimmed" size="sm" ta="center" py="md">
                        No value mapping configuration available
                    </Text>
                )}
            </Stack>
        </Card>
    );
}

