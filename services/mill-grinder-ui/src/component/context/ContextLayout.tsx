/**
 * Context Layout Component
 * 
 * Main layout for the context browser.
 * Sidebar content is rendered via AppSidebar in App.tsx.
 */
import { MetadataProvider, useMetadataContext } from "../data-model/MetadataProvider";
import EntityDetails from "../data-model/EntityDetails";
import { Box } from "@mantine/core";
import { useParams, useNavigate, useLocation } from "react-router";
import { useEffect, useRef } from "react";

function ContextContent() {
    const params = useParams<{ contextId?: string }>();
    const location = useLocation();
    const { entity } = useMetadataContext();
    const navigate = useNavigate();
    const isUpdatingFromUrl = useRef(false);

    // Load entity from URL params when they change
    useEffect(() => {
        isUpdatingFromUrl.current = true;
        
        if (params.contextId) {
            // Load context entity by ID
            const current = entity.selected;
            if (current?.id !== params.contextId) {
                entity.select(params.contextId);
            } else {
                // Already matches, reset flag immediately
                isUpdatingFromUrl.current = false;
            }
        } else {
            // On context route without contextId - clear selected entity to show list
            if (entity.selected && entity.selected.type !== 'CONCEPT') {
                entity.clear();
            }
            isUpdatingFromUrl.current = false;
        }
    }, [params.contextId, entity]);

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
            
            // Don't redirect if we're on context route without a contextId (showing list)
            if (!params.contextId && selected.type !== 'CONCEPT') {
                // On context route but selected entity is not a concept - don't redirect
                return;
            }
            
            // Build expected path based on entity
            let expectedPath: string | null = null;
            if (selected.type === 'CONCEPT') {
                expectedPath = `/context/${selected.id}`;
            }
            
            // Only navigate if we have an expected path and it's different from current
            if (expectedPath && location.pathname !== expectedPath) {
                // Only navigate if we're on the correct route type
                if (selected.type === 'CONCEPT') {
                    navigate(expectedPath, { replace: true });
                }
            }
        }
    }, [entity.selected?.id, entity.loading, location.pathname, params.contextId, navigate]);

    return (
        <Box h="100%">
            <EntityDetails />
        </Box>
    );
}

export default function ContextLayout() {
    return (
        <MetadataProvider>
            <ContextContent />
        </MetadataProvider>
    );
}
