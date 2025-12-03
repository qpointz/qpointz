/**
 * Concepts Layout Component
 * 
 * Main layout for the concepts browser with split view:
 * - Left: Collapsible sidebar with navigation and concepts list
 * - Right: Entity Details
 */
import { MetadataProvider, useMetadataContext } from "../data-model/MetadataProvider";
import { ConceptsSidebar } from "./ConceptsSidebar";
import EntityDetails from "../data-model/EntityDetails";
import { Box, Group } from "@mantine/core";
import { useParams, useNavigate, useLocation } from "react-router";
import { useEffect, useRef } from "react";

function ConceptsContent() {
    const params = useParams<{ conceptId?: string }>();
    const location = useLocation();
    const { entity } = useMetadataContext();
    const navigate = useNavigate();
    const isUpdatingFromUrl = useRef(false);

    // Load entity from URL params when they change
    useEffect(() => {
        isUpdatingFromUrl.current = true;
        
        if (params.conceptId) {
            // Load concept entity by ID
            const current = entity.selected;
            if (current?.id !== params.conceptId) {
                entity.select(params.conceptId);
            } else {
                // Already matches, reset flag immediately
                isUpdatingFromUrl.current = false;
            }
        } else {
            // On concepts route without conceptId - clear selected entity to show list
            if (entity.selected && entity.selected.type !== 'CONCEPT') {
                entity.clear();
            }
            isUpdatingFromUrl.current = false;
        }
    }, [params.conceptId, entity]);

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
            
            // Don't redirect if we're on concepts route without a conceptId (showing list)
            if (!params.conceptId && selected.type !== 'CONCEPT') {
                // On concepts route but selected entity is not a concept - don't redirect
                return;
            }
            
            // Build expected path based on entity
            let expectedPath: string | null = null;
            if (selected.type === 'CONCEPT') {
                expectedPath = `/concepts/${selected.id}`;
            }
            
            // Only navigate if we have an expected path and it's different from current
            if (expectedPath && location.pathname !== expectedPath) {
                // Only navigate if we're on the correct route type
                if (selected.type === 'CONCEPT') {
                    navigate(expectedPath, { replace: true });
                }
            }
        }
    }, [entity.selected?.id, entity.loading, location.pathname, params.conceptId, navigate]);

    return (
        <Group h="100%" p={0} align="top">
            {/* Left Sidebar - Navigation and Concepts List */}
            <ConceptsSidebar />
            
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

export default function ConceptsLayout() {
    return (
        <MetadataProvider>
            <ConceptsContent />
        </MetadataProvider>
    );
}

