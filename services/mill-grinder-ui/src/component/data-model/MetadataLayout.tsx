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
import { useParams, useNavigate, useLocation } from "react-router";
import { useEffect, useRef } from "react";

function MetadataContent() {
    const params = useParams<{ schema?: string; table?: string; attribute?: string }>();
    const location = useLocation();
    const { entity } = useMetadataContext();
    const navigate = useNavigate();
    const isUpdatingFromUrl = useRef(false);

    // Load entity from URL params when they change
    useEffect(() => {
        isUpdatingFromUrl.current = true;
        
        if (params.schema && params.table) {
            // Load table/attribute entity by location
            const current = entity.selected;
            const urlMatches = current?.schemaName === params.schema && 
                             current?.tableName === params.table &&
                             (params.attribute ? current?.attributeName === params.attribute : !current?.attributeName);
            
            if (urlMatches) {
                // Already matches, reset flag immediately
                isUpdatingFromUrl.current = false;
            } else {
                entity.selectByLocation(params.schema, params.table, params.attribute);
            }
        } else {
            // No params to load, reset flag
            isUpdatingFromUrl.current = false;
        }
    }, [params.schema, params.table, params.attribute, entity]);

    // Reset the flag when entity finishes loading
    useEffect(() => {
        if (!entity.loading && isUpdatingFromUrl.current) {
            // Small delay to ensure URL update effect has run
            const timer = setTimeout(() => {
                isUpdatingFromUrl.current = false;
            }, 200);
            return () => clearTimeout(timer);
        }
    }, [entity.loading]);

    // Update URL when entity selection changes (but not from URL params)
    useEffect(() => {
        // Skip if we're currently updating from URL to prevent loops
        if (isUpdatingFromUrl.current) {
            return;
        }
        
        if (entity.selected && !entity.loading) {
            const selected = entity.selected;
            
            // Only handle data model entities (not concepts)
            if (selected.type === 'CONCEPT') {
                return; // Concepts are handled by ConceptsLayout
            }
            
            // Build expected path based on entity
            let expectedPath: string | null = null;
            if (selected.schemaName && selected.tableName) {
                expectedPath = `/data-model/${selected.schemaName}/${selected.tableName}`;
                if (selected.attributeName) {
                    expectedPath += `/${selected.attributeName}`;
                }
            }
            
            // Only navigate if we have an expected path and it's different from current
            if (expectedPath && location.pathname !== expectedPath) {
                navigate(expectedPath, { replace: true });
            }
        }
    }, [entity.selected?.id, entity.loading, location.pathname, navigate]);

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

