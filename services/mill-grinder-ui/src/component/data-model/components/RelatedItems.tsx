import { useState, useEffect } from "react";
import {
    Box,
    Card,
    Text,
    Stack,
    Group,
    Loader,
    Divider,
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
                return <TbDatabase size={16} />;
            case 'TABLE':
                return <TbTable size={16} />;
            case 'ATTRIBUTE':
                return <TbColumns size={16} />;
            case 'CONCEPT':
                return <TbBulb size={16} />;
            default:
                return null;
        }
    };

    if (loading) {
        return (
            <Card withBorder>
                <Stack gap="md">
                    <Text fw={600} size="lg">Related Items</Text>
                    <Box style={{ display: 'flex', justifyContent: 'center', padding: '20px' }}>
                        <Loader size="sm" />
                    </Box>
                </Stack>
            </Card>
        );
    }

    if (error) {
        return (
            <Card withBorder>
                <Stack gap="md">
                    <Text fw={600} size="lg">Related Items</Text>
                    <Text c="red" size="sm">Failed to load related items: {error}</Text>
                </Stack>
            </Card>
        );
    }

    if (relatedEntities.length === 0) {
        return (
            <Card withBorder>
                <Stack gap="md">
                    <Text fw={600} size="lg">Related Items</Text>
                    <Text c="dimmed" size="sm">No related items found for this entity.</Text>
                </Stack>
            </Card>
        );
    }

    // Order: Concepts, Tables, Attributes, Schemas, then others
    const typeOrder = ['CONCEPT', 'TABLE', 'ATTRIBUTE', 'SCHEMA'];
    const orderedTypes = [
        ...typeOrder.filter(type => groupedEntities[type]),
        ...Object.keys(groupedEntities).filter(type => !typeOrder.includes(type))
    ];

    return (
        <Card withBorder>
            <Stack gap="md">
                <Text fw={600} size="lg">Related Items</Text>
                
                {orderedTypes.map((type, index) => {
                    const entities = groupedEntities[type];
                    if (!entities || entities.length === 0) return null;

                    return (
                        <Box key={type}>
                            {index > 0 && <Divider mb="md" />}
                            <Group gap="xs" mb="sm">
                                {getTypeIcon(type)}
                                <Text fw={500} size="sm" c="dimmed">
                                    {getTypeLabel(type)} ({entities.length})
                                </Text>
                            </Group>
                            <Stack gap={4}>
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
        </Card>
    );
}

