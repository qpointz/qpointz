/**
 * Facet Viewer Component
 * 
 * Displays facet data with scope selection and routes to appropriate facet component.
 */
import { useState, useEffect, useCallback, useMemo } from "react";
import { useMetadataContext } from "./MetadataProvider";
import { MetadataApi, FacetsApi } from "../../api/mill/api.ts";
import { Configuration } from "../../api/mill";
import FacetViewerRouter from "./components/facets/FacetViewerRouter";

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

    // Memoize API instances to prevent recreation on every render
    const configuration = useMemo(() => new Configuration(), []);
    const metadataApi = useMemo(() => new MetadataApi(configuration), [configuration]);
    const facetsApi = useMemo(() => new FacetsApi(configuration), [configuration]);

    useEffect(() => {
        setSelectedScope(scope.current);
    }, [scope.current]);

    const loadFacetScopes = useCallback(async () => {
        if (!entityId || !facetType) return;
        try {
            const response = await metadataApi.getFacetScopes(entityId, facetType);
            if (response.data) {
                const scopes = Array.isArray(response.data)
                    ? response.data
                    : Array.from(response.data || []);
                setAvailableScopes(scopes);
            }
        } catch (err) {
            // If scope loading fails, it's not critical - just log it
            console.debug("Failed to load facet scopes", err);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [entityId, facetType]);

    const loadFacet = useCallback(async () => {
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
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [entityId, facetType, selectedScope]);

    // Load facet data when entityId, facetType, or data changes
    useEffect(() => {
        if (data !== undefined) {
            setFacetData(data);
            // Try to load available scopes even when data is provided
            loadFacetScopes();
        } else if (entityId && facetType) {
            loadFacet();
        }
    }, [entityId, facetType, data, loadFacet, loadFacetScopes]);
    
    // Reload facet when scope changes (only if we don't have data provided)
    useEffect(() => {
        if (data === undefined && entityId && facetType) {
            loadFacet();
        }
    }, [selectedScope, data, entityId, facetType, loadFacet]);

    const handleScopeChange = (newScope: string) => {
        setSelectedScope(newScope);
        scope.set(newScope);
    };

    return (
        <FacetViewerRouter
            facetType={facetType}
            entityId={entityId}
            data={facetData}
            availableScopes={availableScopes}
            selectedScope={selectedScope}
            onScopeChange={handleScopeChange}
            loading={loading}
        />
    );
}

