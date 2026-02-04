import { Card, Group, Stack, Text } from "@mantine/core";
import { CodeHighlight } from "@mantine/code-highlight";
import type { ReactNode } from "react";

interface JsonFacetViewProps {
    data: any;
    toggleButton?: ReactNode;
    facetType?: string;
}

export default function JsonFacetView({ data, toggleButton, facetType }: JsonFacetViewProps) {
    const formatJson = (obj: any): string => {
        try {
            if (obj === null || obj === undefined) {
                return "null";
            }
            return JSON.stringify(obj, null, 2);
        } catch {
            return String(obj);
        }
    };

    // For value-mapping facets, extract the actual facet data if it's in scoped format
    // This matches the processing done in ValueMappingFacetView
    let processedData = data;
    if (facetType === 'value-mapping' && data && typeof data === 'object' && !Array.isArray(data)) {
        processedData = data.global || data;
    }

    // Handle null, undefined, or empty data
    if (!processedData || (typeof processedData === 'object' && Object.keys(processedData).length === 0)) {
        return (
            <Card withBorder>
                <Stack gap="sm">
                    {toggleButton && (
                        <Group justify="flex-end">
                            {toggleButton}
                        </Group>
                    )}
                    <Text c="dimmed" size="sm">No data available</Text>
                </Stack>
            </Card>
        );
    }

    return (
        <Card withBorder>
            <Stack gap="sm">
                {toggleButton && (
                    <Group justify="flex-end">
                        {toggleButton}
                    </Group>
                )}
                <CodeHighlight
                    code={formatJson(processedData)}
                    language="json"
                    withCopyButton
                    expandCodeLabel=""
                />
            </Stack>
        </Card>
    );
}

