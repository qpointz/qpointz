import { Box } from '@mantine/core';
import type { FacetTypeManifest } from '../../../types/facetTypes';
import type { StructuralFacet as StructuralFacetData } from '../../../types/schema';
import { SyntaxCodeEditor } from '../../common/SyntaxCodeEditor';
import { FacetPayloadReadOnly } from './FacetPayloadReadOnly';
import { StructuralFacet } from './StructuralFacet';

export interface FacetReadOnlyBodyProps {
  facetTypeKey: string;
  payload: unknown;
  descriptor: FacetTypeManifest | null;
  /** When true and facet type is structural, render {@link StructuralFacet}. */
  structuralFacetEnabled?: boolean;
  /** Stable prefix for nested field keys (Data Model cards vs chat). */
  keyPrefix?: string;
  /** JSON fallback editor height (Data Model uses 200; chat condensed uses less). */
  jsonFallbackMinHeight?: number;
}

/**
 * High-level read-only facet body: structural tailored view, schema-driven fields, or JSON fallback.
 */
export function FacetReadOnlyBody({
  facetTypeKey,
  payload,
  descriptor,
  structuralFacetEnabled = false,
  keyPrefix,
  jsonFallbackMinHeight = 200,
}: FacetReadOnlyBodyProps) {
  const prefix = keyPrefix ?? facetTypeKey;

  if (facetTypeKey.endsWith(':structural') && structuralFacetEnabled) {
    return <StructuralFacet facet={payload as StructuralFacetData} />;
  }

  if (descriptor?.payload) {
    return (
      <Box>
        <FacetPayloadReadOnly schema={descriptor.payload} value={payload} keyPrefix={prefix} />
      </Box>
    );
  }

  return (
    <SyntaxCodeEditor
      value={JSON.stringify(payload ?? {}, null, 2)}
      language="json"
      minHeight={jsonFallbackMinHeight}
      readOnly
    />
  );
}
