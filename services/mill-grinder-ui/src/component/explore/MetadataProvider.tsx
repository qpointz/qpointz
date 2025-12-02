/**
 * Metadata context and provider for the metadata browser UI.
 *
 * Responsibilities:
 * - Manage schema tree state and loading
 * - Track selected entity and cache entity details
 * - Handle scope selection for facets
 * - Expose a typed context API for child components
 */
import { createContext, useCallback, useContext, useEffect, useState, useMemo, useRef } from "react";
import {
    MetadataApi,
    SchemaExplorerApi,
    type MetadataEntityDto,
    type TreeNodeDto,
} from "../../api/mill/api.ts";
import { Configuration } from "../../api/mill";
import { showNotification } from "@mantine/notifications";
import { TbRadioactive } from "react-icons/tb";

/**
 * Public shape of the metadata context consumed by metadata UI components.
 */
interface MetadataContextType {
    tree: {
        data: TreeNodeDto[];
        loading: boolean;
        reload: () => void;
    };
    entity: {
        selected?: MetadataEntityDto;
        loading: boolean;
        select: (entityId: string) => void;
        selectByLocation: (schema: string, table: string, attribute?: string) => void;
        clear: () => void;
    };
    scope: {
        current: string;
        set: (scope: string) => void;
    };
}

const MetadataContext = createContext<MetadataContextType | undefined>(undefined);

/**
 * Provider that manages metadata state and API interactions.
 */
export const MetadataProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    // Memoize API instances to prevent recreation on every render
    const configuration = useMemo(() => new Configuration(), []);
    const metadataApi = useMemo(() => new MetadataApi(configuration), [configuration]);
    const explorerApi = useMemo(() => new SchemaExplorerApi(configuration), [configuration]);

    const [treeData, setTreeData] = useState<TreeNodeDto[]>([]);
    const [treeLoading, setTreeLoading] = useState(false);
    const loadingRef = useRef(false); // Ref to track loading state without causing re-renders

    const [selectedEntity, setSelectedEntity] = useState<MetadataEntityDto | undefined>(undefined);
    const [entityLoading, setEntityLoading] = useState(false);
    const [currentScope, setCurrentScope] = useState<string>("global");

    /**
     * Load schema tree from backend.
     */
    const loadTree = useCallback(async () => {
        if (loadingRef.current) {
            console.log("Tree already loading, skipping...");
            return; // Prevent concurrent loads
        }
        loadingRef.current = true;
        setTreeLoading(true);
        try {
            console.log("Loading tree with scope:", currentScope);
            // Add timeout to prevent hanging
            const timeoutPromise = new Promise((_, reject) => 
                setTimeout(() => reject(new Error("Request timeout after 10 seconds")), 10000)
            );
            
            const response = await Promise.race([
                explorerApi.getTree(undefined, currentScope),
                timeoutPromise
            ]) as any;
            
            console.log("Tree API response:", response);
            console.log("Response data:", response.data);
            console.log("Response data type:", typeof response.data, "IsArray:", Array.isArray(response.data));
            
            // Backend returns List<TreeNodeDto> (array), but OpenAPI spec says TreeNodeDto (single)
            // The actual response is an array, so handle it as such
            let data: TreeNodeDto[] = [];
            if (Array.isArray(response.data)) {
                data = response.data;
            } else if (response.data) {
                // If it's a single object, wrap it in an array
                data = [response.data];
            } else {
                console.warn("Unexpected response format:", response);
            }
            console.log("Processed tree data:", data, "Length:", data.length);
            setTreeData(data);
        } catch (err) {
            console.error("Failed to load schema tree", err);
            const errorMessage = err instanceof Error ? err.message : String(err);
            showNotification({
                title: 'Failed to load schema tree',
                message: errorMessage,
                color: 'red',
                icon: <TbRadioactive />,
            });
            setTreeData([]);
        } finally {
            loadingRef.current = false;
            setTreeLoading(false);
        }
    }, [explorerApi, currentScope]);

    /**
     * Load entity by ID.
     */
    const loadEntityById = useCallback(async (entityId: string) => {
        setEntityLoading(true);
        try {
            const response = await metadataApi.getEntityById(entityId, currentScope);
            setSelectedEntity(response.data);
        } catch (err) {
            console.error("Failed to load entity", err);
            showNotification({
                title: 'Failed to load entity',
                message: err instanceof Error ? err.message : String(err),
                color: 'red',
                icon: <TbRadioactive />,
            });
            setSelectedEntity(undefined);
        } finally {
            setEntityLoading(false);
        }
    }, [metadataApi, currentScope]);

    /**
     * Load entity by location (schema, table, optional attribute).
     */
    const loadEntityByLocation = useCallback(async (schema: string, table: string, attribute?: string) => {
        setEntityLoading(true);
        try {
            let response;
            if (attribute) {
                response = await metadataApi.getAttribute(schema, table, attribute, currentScope);
            } else {
                response = await metadataApi.getTable(schema, table, currentScope);
            }
            setSelectedEntity(response.data);
        } catch (err) {
            console.error("Failed to load entity by location", err);
            showNotification({
                title: 'Failed to load entity',
                message: err instanceof Error ? err.message : String(err),
                color: 'red',
                icon: <TbRadioactive />,
            });
            setSelectedEntity(undefined);
        } finally {
            setEntityLoading(false);
        }
    }, [metadataApi, currentScope]);

    /**
     * Select entity by ID.
     */
    const selectEntity = useCallback((entityId: string) => {
        loadEntityById(entityId);
    }, [loadEntityById]);

    /**
     * Select entity by location.
     */
    const selectEntityByLocation = useCallback((schema: string, table: string, attribute?: string) => {
        loadEntityByLocation(schema, table, attribute);
    }, [loadEntityByLocation]);

    /**
     * Clear selected entity.
     */
    const clearEntity = useCallback(() => {
        setSelectedEntity(undefined);
    }, []);

    /**
     * Update scope and reload data.
     */
    const updateScope = useCallback((scope: string) => {
        setCurrentScope(scope);
    }, []);

    /**
     * Initial load of tree on mount and when scope changes.
     */
    useEffect(() => {
        console.log("useEffect triggered - loading tree, scope:", currentScope);
        loadTree();
    }, [currentScope, loadTree]);

    /**
     * Reload selected entity when scope changes (but not on initial mount).
     */
    useEffect(() => {
        if (selectedEntity?.id) {
            loadEntityById(selectedEntity.id);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [currentScope]);

    const value: MetadataContextType = {
        tree: {
            data: treeData,
            loading: treeLoading,
            reload: loadTree,
        },
        entity: {
            selected: selectedEntity,
            loading: entityLoading,
            select: selectEntity,
            selectByLocation: selectEntityByLocation,
            clear: clearEntity,
        },
        scope: {
            current: currentScope,
            set: updateScope,
        },
    };

    return <MetadataContext.Provider value={value}>{children}</MetadataContext.Provider>;
};

/**
 * Hook to access the metadata context. Throws if used outside MetadataProvider.
 */
export const useMetadataContext = (): MetadataContextType => {
    const ctx = useContext(MetadataContext);
    if (!ctx) {
        throw new Error("useMetadataContext must be used within a MetadataProvider");
    }
    return ctx;
};

