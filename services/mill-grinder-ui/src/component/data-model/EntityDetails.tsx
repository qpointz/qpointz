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
import { TbInfoCircle, TbDatabase, TbTable, TbColumns, TbBulb } from "react-icons/tb";
import FacetViewer from "./FacetViewer";
import RelatedItems from "./components/RelatedItems";
import { buildLocation } from "./utils/entityUtils";

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
            case 'CONCEPT':
                return <TbBulb size={20} />;
            default:
                return null;
        }
    };

    const location = buildLocation(selected);
    // Handle both merged facets (from DTO) and scoped facets format
    const rawFacets = selected.facets || {};
    const facets: Record<string, any> = {};
    
    // Convert scoped format to merged format if needed
    // DTO should return merged format, but handle both cases
    Object.entries(rawFacets).forEach(([facetType, facetData]) => {
        // If facetData is an object with scope keys (like { global: {...} }), extract the scope data
        if (facetData && typeof facetData === 'object' && !Array.isArray(facetData)) {
            const keys = Object.keys(facetData);
            // Check if it looks like scoped format (has "global", "user:", etc.)
            if (keys.length > 0 && (keys.includes('global') || keys.some(k => k.startsWith('user:') || k.startsWith('team:') || k.startsWith('role:')))) {
                // It's scoped format, use the current scope or global
                const scopeData = facetData[scope.current] || facetData['global'] || facetData[keys[0]];
                facets[facetType] = scopeData;
            } else {
                // It's already merged format
                facets[facetType] = facetData;
            }
        } else {
            // Not an object or is array, use as-is
            facets[facetType] = facetData;
        }
    });

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

                {/* Related Items */}
                <RelatedItems
                    entityId={selected.id!}
                    scope={scope.current}
                />
            </Stack>
        </Box>
    );
}

