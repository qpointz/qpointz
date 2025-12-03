import type { ReactNode } from "react";
import BaseFacetViewer from "./BaseFacetViewer";
import DescriptiveFacetView from "./DescriptiveFacetView";
import StructuralFacetView from "./StructuralFacetView";
import RelationFacetView from "./RelationFacetView";
import ConceptFacetView from "./ConceptFacetView";
import ValueMappingFacetView from "./ValueMappingFacetView";
import JsonFacetView from "./JsonFacetView";

interface FacetViewerRouterProps {
    facetType: string;
    entityId: string;
    data: any;
    availableScopes: string[];
    selectedScope: string;
    onScopeChange: (scope: string) => void;
    loading?: boolean;
}

export default function FacetViewerRouter({
    facetType,
    entityId,
    data,
    availableScopes,
    selectedScope,
    onScopeChange,
    loading = false,
}: FacetViewerRouterProps) {
    // Route to appropriate facet component based on facet type
    const renderFacetView = (toggleButton: ReactNode) => {
        switch (facetType.toLowerCase()) {
            case 'descriptive':
                return <DescriptiveFacetView data={data} toggleButton={toggleButton} />;
            case 'structural':
                return <StructuralFacetView data={data} toggleButton={toggleButton} />;
            case 'relation':
                return <RelationFacetView data={data} toggleButton={toggleButton} />;
            case 'concept':
                return <ConceptFacetView data={data} toggleButton={toggleButton} />;
            case 'value-mapping':
                return <ValueMappingFacetView data={data} toggleButton={toggleButton} />;
            default:
                // Fallback to JSON view for unknown facet types
                return <JsonFacetView data={data} toggleButton={toggleButton} />;
        }
    };

    // For unknown facet types, show JSON view directly without BaseFacetViewer wrapper
    if (facetType.toLowerCase() !== 'descriptive' &&
        facetType.toLowerCase() !== 'structural' &&
        facetType.toLowerCase() !== 'relation' &&
        facetType.toLowerCase() !== 'concept' &&
        facetType.toLowerCase() !== 'value-mapping') {
        return (
            <BaseFacetViewer
                facetType={facetType}
                entityId={entityId}
                data={data}
                availableScopes={availableScopes}
                selectedScope={selectedScope}
                onScopeChange={onScopeChange}
                loading={loading}
            >
                {({ toggleButton }) => <JsonFacetView data={data} toggleButton={toggleButton} />}
            </BaseFacetViewer>
        );
    }

    return (
        <BaseFacetViewer
            facetType={facetType}
            entityId={entityId}
            data={data}
            availableScopes={availableScopes}
            selectedScope={selectedScope}
            onScopeChange={onScopeChange}
            loading={loading}
        >
            {({ toggleButton }) => renderFacetView(toggleButton)}
        </BaseFacetViewer>
    );
}

