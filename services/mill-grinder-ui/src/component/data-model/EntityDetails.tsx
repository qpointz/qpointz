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
    Select,
} from "@mantine/core";
import { TbInfoCircle, TbDatabase, TbTable, TbColumns, TbBulb, TbKey, TbLink, TbShieldCheck } from "react-icons/tb";
import FacetViewer from "./FacetViewer";
import RelatedItems from "./components/RelatedItems";
import { buildLocation } from "./utils/entityUtils";
import { useState, useEffect, useMemo } from "react";
import { MetadataApi } from "../../api/mill/api.ts";
import { Configuration } from "../../api/mill";

export default function EntityDetails() {
    const { entity, scope } = useMetadataContext();
    const [availableScopes, setAvailableScopes] = useState<string[]>([]);
    
    // Memoize API instance
    const metadataApi = useMemo(() => new MetadataApi(new Configuration()), []);

    // Load available scopes for the selected entity
    useEffect(() => {
        if (!entity.selected?.id) {
            setAvailableScopes([]);
            return;
        }
        
        const loadScopes = async () => {
            try {
                // Try to get scopes from any facet (descriptive is common)
                const facetTypes = Object.keys(entity.selected?.facets || {});
                if (facetTypes.length > 0) {
                    const response = await metadataApi.getFacetScopes(entity.selected!.id!, facetTypes[0]);
                    if (response.data) {
                        const scopes = Array.isArray(response.data)
                            ? response.data
                            : Array.from(response.data || []);
                        setAvailableScopes(scopes);
                    }
                }
            } catch (err) {
                console.debug("Failed to load entity scopes", err);
            }
        };
        
        loadScopes();
    }, [entity.selected?.id, metadataApi]);

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

    // Extract structural data for header display
    const structuralData = facets.structural?.global || facets.structural || {};
    const formatType = (): string => {
        const { physicalType, precision, scale } = structuralData;
        if (!physicalType) return '';
        if (precision !== null && precision !== undefined) {
            if (scale !== null && scale !== undefined) {
                return `${physicalType}(${precision},${scale})`;
            }
            return `${physicalType}(${precision})`;
        }
        return physicalType;
    };
    const hasStructuralInfo = structuralData.physicalName || structuralData.physicalType || 
        structuralData.isPrimaryKey || structuralData.isForeignKey || 
        structuralData.isUnique || structuralData.nullable !== null;

    return (
        <Box p="md">
            <Stack gap="sm">
                {/* Entity Header */}
                <Card withBorder>
                    <Group justify="space-between" align="flex-start">
                        <Stack gap="xs" style={{ flex: 1 }}>
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
                            {location && (
                                <Group gap="xs">
                                    <Text size="sm" c="dimmed">Location:</Text>
                                    <Code>{location}</Code>
                                </Group>
                            )}
                            {/* Structural info merged into header */}
                            {hasStructuralInfo && (
                                <Group gap="xs" wrap="wrap">
                                    {structuralData.physicalName && (
                                        <Text size="xs" c="dimmed">{structuralData.physicalName}</Text>
                                    )}
                                    {structuralData.physicalType && (
                                        <Badge variant="outline" size="xs" color="blue">
                                            {formatType()}
                                        </Badge>
                                    )}
                                    {structuralData.isPrimaryKey && (
                                        <Badge variant="filled" color="yellow" size="xs" leftSection={<TbKey size={10} />}>
                                            PK
                                        </Badge>
                                    )}
                                    {structuralData.isForeignKey && (
                                        <Badge variant="filled" color="orange" size="xs" leftSection={<TbLink size={10} />}>
                                            FK
                                        </Badge>
                                    )}
                                    {structuralData.isUnique && (
                                        <Badge variant="filled" color="green" size="xs" leftSection={<TbShieldCheck size={10} />}>
                                            Unique
                                        </Badge>
                                    )}
                                    {structuralData.nullable !== null && structuralData.nullable !== undefined && (
                                        <Badge variant={structuralData.nullable ? "light" : "filled"} color={structuralData.nullable ? "gray" : "red"} size="xs">
                                            {structuralData.nullable ? "Null" : "Not Null"}
                                        </Badge>
                                    )}
                                    {structuralData.backendType && (
                                        <Group gap={2}>
                                            <TbDatabase size={12} style={{ color: 'var(--mantine-color-dimmed)' }} />
                                            <Text size="xs" c="dimmed">{structuralData.backendType}</Text>
                                        </Group>
                                    )}
                                    {structuralData.tableType && (
                                        <Text size="xs" c="dimmed">{structuralData.tableType}</Text>
                                    )}
                                </Group>
                            )}
                        </Stack>
                        {/* Scope Selector - top right */}
                        {availableScopes.length > 0 && (
                            <Select
                                label="Scope"
                                value={scope.current}
                                onChange={(value) => {
                                    if (value) {
                                        scope.set(value);
                                    }
                                }}
                                data={availableScopes}
                                size="xs"
                                w={180}
                            />
                        )}
                    </Group>
                </Card>

                {/* Descriptive Facet */}
                {facets.descriptive && (
                    <Box p="sm" style={{ borderRadius: 'var(--mantine-radius-md)', border: '1px solid var(--mantine-color-gray-3)' }}>
                        <Text size="xs" fw={500} c="dimmed" mb="xs">Description</Text>
                        <FacetViewer
                            entityId={selected.id!}
                            facetType="descriptive"
                            data={facets.descriptive}
                            hideScope
                        />
                    </Box>
                )}


                {/* Remaining Facets in Tabs (excluding descriptive and structural) */}
                {(() => {
                    const tabFacets = Object.entries(facets).filter(
                        ([facetType]) => facetType !== 'descriptive' && facetType !== 'structural'
                    );
                    
                    if (tabFacets.length === 0) {
                        // No tab facets and no inline facets either
                        if (!facets.descriptive && !facets.structural) {
                            return (
                                <Text c="dimmed" size="sm">No facets available for this entity</Text>
                            );
                        }
                        return null;
                    }
                    
                    return (
                        <Box p="sm" style={{ borderRadius: 'var(--mantine-radius-md)', border: '1px solid var(--mantine-color-gray-3)' }}>
                            <Tabs defaultValue={tabFacets[0][0]} variant="outline">
                                <Tabs.List>
                                    {tabFacets.map(([facetType]) => (
                                        <Tabs.Tab key={facetType} value={facetType} size="xs">
                                            {facetType}
                                        </Tabs.Tab>
                                    ))}
                                </Tabs.List>

                                {tabFacets.map(([facetType, facetData]) => (
                                    <Tabs.Panel key={facetType} value={facetType} pt="sm">
                                        <FacetViewer
                                            entityId={selected.id!}
                                            facetType={facetType}
                                            data={facetData}
                                            hideScope
                                        />
                                    </Tabs.Panel>
                                ))}
                            </Tabs>
                        </Box>
                    );
                })()}

                {/* Related Items */}
                <RelatedItems
                    entityId={selected.id!}
                    scope={scope.current}
                />
            </Stack>
        </Box>
    );
}

