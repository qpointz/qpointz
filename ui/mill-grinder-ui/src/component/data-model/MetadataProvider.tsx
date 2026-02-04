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
 * Helper function to extract schemas from tree nodes.
 */
function getSchemas(nodes: TreeNodeDto[]): TreeNodeDto[] {
    const schemas: TreeNodeDto[] = [];
    for (const node of nodes) {
        if (node.type === 'SCHEMA') {
            schemas.push(node);
        } else if (node.children) {
            // Recursively search in children
            schemas.push(...getSchemas(node.children));
        }
    }
    return schemas;
}

/**
 * Extract categories from concept entities.
 */
function extractCategories(concepts: MetadataEntityDto[]): string[] {
    const categories = new Set<string>();
    for (const concept of concepts) {
        const conceptFacet = concept.facets?.concept;
        if (conceptFacet) {
            const globalFacet = conceptFacet.global || conceptFacet;
            const conceptsList = globalFacet.concepts || [];
            for (const c of conceptsList) {
                if (c.category) {
                    categories.add(c.category);
                }
            }
        }
    }
    return Array.from(categories).sort();
}

/**
 * Extract tags from concept entities.
 */
function extractTags(concepts: MetadataEntityDto[]): string[] {
    const tags = new Set<string>();
    for (const concept of concepts) {
        const conceptFacet = concept.facets?.concept;
        if (conceptFacet) {
            const globalFacet = conceptFacet.global || conceptFacet;
            const conceptsList = globalFacet.concepts || [];
            for (const c of conceptsList) {
                if (c.tags && Array.isArray(c.tags)) {
                    c.tags.forEach((tag: string) => tags.add(tag));
                }
            }
        }
    }
    return Array.from(tags).sort();
}

/**
 * Filter concepts by category.
 */
function filterConceptsByCategory(concepts: MetadataEntityDto[], category: string): MetadataEntityDto[] {
    return concepts.filter(concept => {
        const conceptFacet = concept.facets?.concept;
        if (conceptFacet) {
            const globalFacet = conceptFacet.global || conceptFacet;
            const conceptsList = globalFacet.concepts || [];
            return conceptsList.some((c: any) => c.category === category);
        }
        return false;
    });
}

/**
 * Filter concepts by tag.
 */
function filterConceptsByTag(concepts: MetadataEntityDto[], tag: string): MetadataEntityDto[] {
    return concepts.filter(concept => {
        const conceptFacet = concept.facets?.concept;
        if (conceptFacet) {
            const globalFacet = conceptFacet.global || conceptFacet;
            const conceptsList = globalFacet.concepts || [];
            return conceptsList.some((c: any) => 
                c.tags && Array.isArray(c.tags) && c.tags.includes(tag)
            );
        }
        return false;
    });
}

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
    schema: {
        selected?: string;
        schemas: TreeNodeDto[];
        select: (schemaName: string) => void;
    };
    concepts: {
        data: MetadataEntityDto[];
        loading: boolean;
        categories: string[];
        tags: string[];
        getByCategory: (category: string) => MetadataEntityDto[];
        getByTag: (tag: string) => MetadataEntityDto[];
        reload: () => void;
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
    const [selectedSchema, setSelectedSchema] = useState<string | undefined>(undefined);
    const [conceptsData, setConceptsData] = useState<MetadataEntityDto[]>([]);
    const [conceptsLoading, setConceptsLoading] = useState(false);

    /**
     * Load schema tree from backend.
     */
    const loadTree = useCallback(async () => {
        if (loadingRef.current) {
            return; // Prevent concurrent loads
        }
        loadingRef.current = true;
        setTreeLoading(true);
        try {
            // Add timeout to prevent hanging
            const timeoutPromise = new Promise((_, reject) => 
                setTimeout(() => reject(new Error("Request timeout after 10 seconds")), 10000)
            );
            
            const response = await Promise.race([
                explorerApi.getTree(undefined, currentScope),
                timeoutPromise
            ]) as any;
            
            // Backend returns List<TreeNodeDto> (array), but OpenAPI spec says TreeNodeDto (single)
            // The actual response is an array, so handle it as such
            let data: TreeNodeDto[] = [];
            if (Array.isArray(response.data)) {
                data = response.data;
            } else if (response.data) {
                // If it's a single object, wrap it in an array
                data = [response.data];
            }
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
            if (!response.data) {
                throw new Error(`Entity not found: ${schema}.${table}${attribute ? '.' + attribute : ''}`);
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
     * Select schema.
     */
    const selectSchema = useCallback((schemaName: string) => {
        setSelectedSchema(schemaName);
    }, []);

    /**
     * Load concepts from backend.
     */
    const loadConcepts = useCallback(async () => {
        setConceptsLoading(true);
        try {
            const response = await metadataApi.getEntities('CONCEPT', currentScope);
            
            // Handle both single entity and array responses
            let allEntities: MetadataEntityDto[] = [];
            if (Array.isArray(response.data)) {
                allEntities = response.data;
            } else if (response.data) {
                allEntities = [response.data];
            }
            
            // Filter to only include entities with type === 'CONCEPT'
            const concepts = allEntities.filter(entity => entity.type === 'CONCEPT');
            setConceptsData(concepts);
        } catch (err) {
            console.error("Failed to load concepts", err);
            showNotification({
                title: 'Failed to load concepts',
                message: err instanceof Error ? err.message : String(err),
                color: 'red',
                icon: <TbRadioactive />,
            });
            setConceptsData([]);
        } finally {
            setConceptsLoading(false);
        }
    }, [metadataApi, currentScope]);

    /**
     * Extract schemas from tree data.
     */
    const schemas = useMemo(() => {
        return getSchemas(treeData);
    }, [treeData]);

    /**
     * Extract categories and tags from concepts.
     */
    const conceptCategories = useMemo(() => {
        return extractCategories(conceptsData);
    }, [conceptsData]);

    const conceptTags = useMemo(() => {
        return extractTags(conceptsData);
    }, [conceptsData]);

    /**
     * Auto-select first schema when tree loads (if no schema selected).
     */
    useEffect(() => {
        if (selectedSchema === undefined && schemas.length > 0) {
            const firstSchema = schemas[0];
            const schemaName = firstSchema.name || firstSchema.displayName || firstSchema.id;
            if (schemaName) {
                setSelectedSchema(schemaName);
            }
        }
    }, [schemas, selectedSchema]);

    /**
     * Initial load of tree on mount and when scope changes.
     */
    useEffect(() => {
        loadTree();
    }, [currentScope, loadTree]);

    /**
     * Load concepts on mount and when scope changes.
     */
    useEffect(() => {
        loadConcepts();
    }, [currentScope, loadConcepts]);

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
        schema: {
            selected: selectedSchema,
            schemas: schemas,
            select: selectSchema,
        },
        concepts: {
            data: conceptsData,
            loading: conceptsLoading,
            categories: conceptCategories,
            tags: conceptTags,
            getByCategory: (category: string) => filterConceptsByCategory(conceptsData, category),
            getByTag: (tag: string) => filterConceptsByTag(conceptsData, tag),
            reload: loadConcepts,
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

