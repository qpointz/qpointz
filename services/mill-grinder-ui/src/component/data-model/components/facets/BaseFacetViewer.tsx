import { useState } from "react";
import {
    Box,
    Select,
    Stack,
    Group,
    Text,
    Loader,
    ActionIcon,
    Tooltip,
} from "@mantine/core";
import { TbCode, TbEye } from "react-icons/tb";
import JsonFacetView from "./JsonFacetView";
import type { ReactNode } from "react";

interface BaseFacetViewerProps {
    facetType: string;
    entityId: string;
    data: any;
    availableScopes: string[];
    selectedScope: string;
    onScopeChange: (scope: string) => void;
    loading?: boolean;
    hideScope?: boolean;
    children: (props: { toggleButton: ReactNode; showJson: boolean }) => ReactNode; // Render function that receives toggle button
}

export default function BaseFacetViewer({
    facetType,
    data,
    availableScopes,
    selectedScope,
    onScopeChange,
    loading = false,
    hideScope = false,
    children,
}: BaseFacetViewerProps) {
    const [showJson, setShowJson] = useState(false);

    // Removed unused function - facet type label is handled by individual components

    if (loading) {
        return (
            <Box p="md" style={{ display: 'flex', justifyContent: 'center' }}>
                <Loader size="sm" />
            </Box>
        );
    }

    // Handle null, undefined, or empty object (but allow arrays and objects with properties)
    if (data === null || data === undefined) {
        return (
            <Text c="dimmed" size="sm">No facet data available</Text>
        );
    }
    
    // Check for empty object (but allow arrays and objects with properties)
    if (typeof data === 'object' && !Array.isArray(data) && Object.keys(data).length === 0) {
        return (
            <Text c="dimmed" size="sm">No facet data available</Text>
        );
    }

    const toggleButton = (
        <Tooltip label={showJson ? "Show normal view" : "Show JSON view"}>
            <ActionIcon
                variant="subtle"
                onClick={() => setShowJson(!showJson)}
                size="sm"
            >
                {showJson ? <TbEye size={16} /> : <TbCode size={16} />}
            </ActionIcon>
        </Tooltip>
    );

    return (
        <Stack gap="sm">
            {/* Scope Selector - only shown if not hidden */}
            {!hideScope && availableScopes.length > 0 && (
                <Group gap="sm">
                    <Text size="sm" fw={500}>Scope:</Text>
                    <Select
                        value={selectedScope}
                        onChange={(value) => {
                            if (value) {
                                onScopeChange(value);
                            }
                        }}
                        data={availableScopes}
                        style={{ width: 200 }}
                        size="sm"
                    />
                </Group>
            )}

            {/* Content: Normal View or JSON View */}
            {showJson ? (
                <JsonFacetView 
                    data={data} 
                    toggleButton={toggleButton}
                    facetType={facetType}
                />
            ) : (
                <>{children({ toggleButton, showJson })}</>
            )}
        </Stack>
    );
}

