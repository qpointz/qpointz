/**
 * Metadata Layout Component
 * 
 * Main layout for the metadata browser with split view:
 * - Left: Collapsible sidebar with navigation and table list
 * - Right: Entity Details + Facet Viewer
 */
import { MetadataProvider, useMetadataContext } from "./MetadataProvider";
import { MetadataSidebar } from "./MetadataSidebar";
import EntityDetails from "./EntityDetails";
import { Box, Group } from "@mantine/core";
import { useParams, useNavigate } from "react-router";
import { useEffect } from "react";

function MetadataContent() {
    const params = useParams<{ schema?: string; table?: string; attribute?: string }>();
    const { entity } = useMetadataContext();
    const navigate = useNavigate();

    // Load entity from URL params when they change
    useEffect(() => {
        if (params.schema && params.table) {
            // Only load if the current entity doesn't match the URL
            const current = entity.selected;
            const urlMatches = current?.schemaName === params.schema && 
                             current?.tableName === params.table &&
                             (params.attribute ? current?.attributeName === params.attribute : !current?.attributeName);
            
            if (!urlMatches) {
                entity.selectByLocation(params.schema, params.table, params.attribute);
            }
        }
    }, [params.schema, params.table, params.attribute]);

    // Update URL when entity selection changes (but not from URL params)
    useEffect(() => {
        if (entity.selected && !entity.loading) {
            const selected = entity.selected;
            
            // Only update URL if entity has location info
            if (selected.schemaName && selected.tableName) {
                let newPath = `/explore/${selected.schemaName}/${selected.tableName}`;
                if (selected.attributeName) {
                    newPath += `/${selected.attributeName}`;
                }
                
                // Only update if different to avoid loops
                const currentPath = `/explore${params.schema ? `/${params.schema}` : ''}${params.table ? `/${params.table}` : ''}${params.attribute ? `/${params.attribute}` : ''}`;
                if (currentPath !== newPath) {
                    navigate(newPath, { replace: true });
                }
            }
        }
    }, [entity.selected?.id, entity.loading]);

    return (
        <Group h="100%" p={0} align="top">
            {/* Left Sidebar - Navigation and Table List */}
            <MetadataSidebar />
            
            {/* Right Panel - Entity Details */}
            <Box flex="1" p={0} mx={0} style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
                {/* Entity Details Content */}
                <Box style={{ flex: 1, overflow: 'auto' }}>
                    <EntityDetails />
                </Box>
            </Box>
        </Group>
    );
}

export default function MetadataLayout() {
    return (
        <MetadataProvider>
            <MetadataContent />
        </MetadataProvider>
    );
}

