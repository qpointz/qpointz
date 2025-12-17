import { useState, useEffect } from "react";
import {
    Box,
    Text,
    Stack,
    Group,
    Loader,
    Badge,
} from "@mantine/core";
import { TbDatabase, TbTable, TbColumns, TbBulb } from "react-icons/tb";
import type { MetadataEntityDto } from "../../../api/mill/api.ts";
import RelatedItemLink from "./RelatedItemLink";

interface RelatedItemsProps {
    entityId: string;
    scope: string;
}

export default function RelatedItems({ entityId, scope }: RelatedItemsProps) {
    const [relatedEntities, setRelatedEntities] = useState<MetadataEntityDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!entityId) {
            setRelatedEntities([]);
            return;
        }

        setLoading(true);
        setError(null);

        // TODO: getRelatedEntities method does not exist in MetadataApi
        // This feature is not yet available in the API
        // For now, we'll show an empty state
        setLoading(false);
        setRelatedEntities([]);
        
        // const metadataApi = new MetadataApi(new Configuration());
        // metadataApi.getRelatedEntities(entityId, scope)
        //     .then((response: any) => {
        //         setRelatedEntities(response.data || []);
        //     })
        //     .catch((err: any) => {
        //         console.error("Failed to load related entities", err);
        //         setError(err instanceof Error ? err.message : String(err));
        //         setRelatedEntities([]);
        //     })
        //     .finally(() => {
        //         setLoading(false);
        //     });
    }, [entityId, scope]);

    // Group entities by type
    const groupedEntities = relatedEntities.reduce((acc, entity) => {
        const type = entity.type || 'UNKNOWN';
        if (!acc[type]) {
            acc[type] = [];
        }
        acc[type].push(entity);
        return acc;
    }, {} as Record<string, MetadataEntityDto[]>);

    const getTypeLabel = (type: string): string => {
        switch (type) {
            case 'CONCEPT':
                return 'Concepts';
            case 'TABLE':
                return 'Tables';
            case 'ATTRIBUTE':
                return 'Attributes';
            case 'SCHEMA':
                return 'Schemas';
            default:
                return type;
        }
    };

    const getTypeIcon = (type: string) => {
        switch (type) {
            case 'SCHEMA':
                return <TbDatabase size={14} />;
            case 'TABLE':
                return <TbTable size={14} />;
            case 'ATTRIBUTE':
                return <TbColumns size={14} />;
            case 'CONCEPT':
                return <TbBulb size={14} />;
            default:
                return null;
        }
    };

    const boxStyle = { borderRadius: 'var(--mantine-radius-md)', border: '1px solid var(--mantine-color-gray-3)' };

    if (loading) {
        return (
            <Box p="sm" style={boxStyle}>
                <Group gap="xs" mb="xs">
                    <Text size="xs" fw={500} c="dimmed">Related Items</Text>
                </Group>
                <Box style={{ display: 'flex', justifyContent: 'center', padding: '8px' }}>
                    <Loader size="xs" />
                </Box>
            </Box>
        );
    }

    if (error) {
        return (
            <Box p="sm" style={boxStyle}>
                <Group gap="xs" mb="xs">
                    <Text size="xs" fw={500} c="dimmed">Related Items</Text>
                </Group>
                <Text c="red" size="xs">Failed to load: {error}</Text>
            </Box>
        );
    }

    if (relatedEntities.length === 0) {
        return (
            <Box p="sm" style={boxStyle}>
                <Group gap="xs">
                    <Text size="xs" fw={500} c="dimmed">Related Items</Text>
                    <Text c="dimmed" size="xs">None</Text>
                </Group>
            </Box>
        );
    }

    // Order: Concepts, Tables, Attributes, Schemas, then others
    const typeOrder = ['CONCEPT', 'TABLE', 'ATTRIBUTE', 'SCHEMA'];
    const orderedTypes = [
        ...typeOrder.filter(type => groupedEntities[type]),
        ...Object.keys(groupedEntities).filter(type => !typeOrder.includes(type))
    ];

    return (
        <Box p="sm" style={boxStyle}>
            <Text size="xs" fw={500} c="dimmed" mb="xs">Related Items</Text>
            <Stack gap="xs">
                {orderedTypes.map((type) => {
                    const entities = groupedEntities[type];
                    if (!entities || entities.length === 0) return null;

                    return (
                        <Box key={type}>
                            <Group gap={4} mb={4}>
                                {getTypeIcon(type)}
                                <Text size="xs" c="dimmed">
                                    {getTypeLabel(type)}
                                </Text>
                                <Badge variant="light" size="xs" color="gray">{entities.length}</Badge>
                            </Group>
                            <Stack gap={2}>
                                {entities.map(entity => (
                                    <RelatedItemLink
                                        key={entity.id}
                                        entity={entity}
                                    />
                                ))}
                            </Stack>
                        </Box>
                    );
                })}
            </Stack>
        </Box>
    );
}

