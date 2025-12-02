/**
 * Entity Details Component
 * 
 * Displays detailed information about a selected metadata entity,
 * including all facets (descriptive, structural, relation, concept).
 */
import { useMetadataContext } from "./MetadataProvider";
import {
    Box,
    Card,
    Text,
    Badge,
    Tabs,
    Loader,
    Stack,
    Group,
    Code,
} from "@mantine/core";
import { TbInfoCircle, TbDatabase, TbTable, TbColumns } from "react-icons/tb";
import FacetViewer from "./FacetViewer";
import type { MetadataEntityDto } from "../../api/mill/api.ts";

export default function EntityDetails() {
    const { entity, scope } = useMetadataContext();

    if (entity.loading) {
        return (
            <Box p="md" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                <Loader size="md" />
            </Box>
        );
    }

    if (!entity.selected) {
        return (
            <Box p="md" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                <Stack align="center" gap="xs">
                    <TbInfoCircle size={48} style={{ color: 'var(--mantine-color-gray-5)' }} />
                    <Text c="dimmed" size="sm">Select an entity from the tree to view details</Text>
                </Stack>
            </Box>
        );
    }

    const selected = entity.selected;

    const getTypeIcon = () => {
        switch (selected.type) {
            case 'SCHEMA':
                return <TbDatabase size={20} />;
            case 'TABLE':
                return <TbTable size={20} />;
            case 'ATTRIBUTE':
                return <TbColumns size={20} />;
            default:
                return null;
        }
    };

    const buildLocation = (entity: MetadataEntityDto): string => {
        const parts: string[] = [];
        if (entity.schemaName) parts.push(entity.schemaName);
        if (entity.tableName) parts.push(entity.tableName);
        if (entity.attributeName) parts.push(entity.attributeName);
        return parts.join('.');
    };

    const location = buildLocation(selected);
    const facets = selected.facets || {};

    return (
        <Box p="md">
            <Stack gap="md">
                {/* Entity Header */}
                <Card withBorder>
                    <Stack gap="xs">
                        <Group justify="space-between">
                            <Group gap="sm">
                                {getTypeIcon()}
                                <Text fw={600} size="lg">
                                    {selected.id}
                                </Text>
                                {selected.type && (
                                    <Badge variant="light" color="primary">
                                        {selected.type}
                                    </Badge>
                                )}
                            </Group>
                        </Group>
                        {location && (
                            <Group gap="xs">
                                <Text size="sm" c="dimmed">Location:</Text>
                                <Code>{location}</Code>
                            </Group>
                        )}
                        {scope.current && (
                            <Group gap="xs">
                                <Text size="sm" c="dimmed">Scope:</Text>
                                <Badge variant="outline">{scope.current}</Badge>
                            </Group>
                        )}
                    </Stack>
                </Card>

                {/* Facets */}
                {Object.keys(facets).length > 0 ? (
                    <Tabs defaultValue={Object.keys(facets)[0]}>
                        <Tabs.List>
                            {Object.keys(facets).map((facetType) => (
                                <Tabs.Tab key={facetType} value={facetType}>
                                    {facetType}
                                </Tabs.Tab>
                            ))}
                        </Tabs.List>

                        {Object.entries(facets).map(([facetType, facetData]) => (
                            <Tabs.Panel key={facetType} value={facetType} pt="md">
                                <FacetViewer
                                    entityId={selected.id!}
                                    facetType={facetType}
                                    data={facetData}
                                />
                            </Tabs.Panel>
                        ))}
                    </Tabs>
                ) : (
                    <Card withBorder>
                        <Text c="dimmed" size="sm">No facets available for this entity</Text>
                    </Card>
                )}
            </Stack>
        </Box>
    );
}

