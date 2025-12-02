/**
 * Facet Viewer Component
 * 
 * Displays facet data with syntax highlighting and scope selection.
 */
import { useState, useEffect } from "react";
import { useMetadataContext } from "./MetadataProvider";
import {
    Box,
    Card,
    Select,
    Stack,
    Group,
    Text,
    Loader,
} from "@mantine/core";
import { CodeHighlight } from "@mantine/code-highlight";
import { FacetsApi } from "../../api/mill/api.ts";
import { Configuration } from "../../api/mill";

interface FacetViewerProps {
    entityId: string;
    facetType: string;
    data?: any;
}

export default function FacetViewer({ entityId, facetType, data }: FacetViewerProps) {
    const { scope } = useMetadataContext();
    const [facetData, setFacetData] = useState<any>(data);
    const [availableScopes, setAvailableScopes] = useState<string[]>([]);
    const [loading, setLoading] = useState(false);
    const [selectedScope, setSelectedScope] = useState<string>(scope.current);

    const facetsApi = new FacetsApi(new Configuration());

    useEffect(() => {
        setSelectedScope(scope.current);
    }, [scope.current]);

    useEffect(() => {
        if (data !== undefined) {
            setFacetData(data);
        } else if (entityId && facetType) {
            loadFacet();
        }
    }, [entityId, facetType, selectedScope, data]);

    const loadFacet = async () => {
        if (!entityId || !facetType) return;

        setLoading(true);
        try {
            const response = await facetsApi.getFacetByScope(entityId, facetType, selectedScope);
            setFacetData(response.data.data);
            if (response.data.availableScopes) {
                // Convert Set to Array if needed
                const scopes = Array.isArray(response.data.availableScopes)
                    ? response.data.availableScopes
                    : Array.from(response.data.availableScopes || []);
                setAvailableScopes(scopes);
            }
        } catch (err) {
            console.error("Failed to load facet", err);
            setFacetData(null);
        } finally {
            setLoading(false);
        }
    };

    const formatJson = (obj: any): string => {
        try {
            return JSON.stringify(obj, null, 2);
        } catch {
            return String(obj);
        }
    };

    if (loading) {
        return (
            <Box p="md" style={{ display: 'flex', justifyContent: 'center' }}>
                <Loader size="sm" />
            </Box>
        );
    }

    if (!facetData) {
        return (
            <Card withBorder>
                <Text c="dimmed" size="sm">No facet data available</Text>
            </Card>
        );
    }

    return (
        <Stack gap="md">
            {/* Scope Selector */}
            {availableScopes.length > 0 && (
                <Group>
                    <Text size="sm" fw={500}>Scope:</Text>
                    <Select
                        value={selectedScope}
                        onChange={(value) => {
                            if (value) {
                                setSelectedScope(value);
                                scope.set(value);
                            }
                        }}
                        data={availableScopes}
                        style={{ flex: 1, maxWidth: 300 }}
                    />
                </Group>
            )}

            {/* Facet Data */}
            <Card withBorder>
                <CodeHighlight
                    code={formatJson(facetData)}
                    language="json"
                    withCopyButton
                />
            </Card>
        </Stack>
    );
}

