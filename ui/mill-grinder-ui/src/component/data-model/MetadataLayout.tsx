/**
 * Metadata Layout Component
 * 
 * Main layout for the metadata browser.
 * Sidebar content is rendered via AppSidebar in App.tsx.
 */
import { MetadataProvider, useMetadataContext } from "./MetadataProvider";
import EntityDetails from "./EntityDetails";
import { Box } from "@mantine/core";
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
    }, [params.schema, params.table, params.attribute, entity.selected?.id, entity.selectByLocation]);

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
            
            // Only handle data model entities (not contexts)
            if (selected.type === 'CONCEPT') {
                return; // Contexts are handled by ContextLayout
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
    }, [entity.selected?.id, entity.selected?.schemaName, entity.selected?.tableName, entity.selected?.attributeName, entity.selected?.type, entity.loading, location.pathname, navigate]);

    return (
        <Box h="100%">
            <EntityDetails />
        </Box>
    );
}

export default function MetadataLayout() {
    return (
        <MetadataProvider>
            <MetadataContent />
        </MetadataProvider>
    );
}

