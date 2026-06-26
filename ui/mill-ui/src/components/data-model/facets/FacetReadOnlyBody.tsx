import { Box, Skeleton, Stack } from '@mantine/core';
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
  /** While facet type manifests are loading, avoid JSON fallback flicker. */
  manifestLoading?: boolean;
  /** Stable prefix for nested field keys (Data Model cards vs chat). */
  keyPrefix?: string;
  /** JSON fallback editor height (Data Model uses 200; chat condensed uses less). */
  jsonFallbackMinHeight?: number;
}

function FacetReadOnlyBodySkeleton({ minHeight = 200 }: { minHeight?: number }) {
  return (
    <Stack gap="sm" aria-busy="true" aria-label="Loading facet content">
      <Skeleton height={10} width="35%" radius="sm" />
      <Skeleton height={10} width="60%" radius="sm" />
      <Skeleton height={10} width="45%" radius="sm" />
      <Skeleton height={minHeight} radius="sm" mt={4} />
    </Stack>
  );
}

/**
 * High-level read-only facet body: structural tailored view, schema-driven fields, or JSON fallback.
 */
export function FacetReadOnlyBody({
  facetTypeKey,
  payload,
  descriptor,
  structuralFacetEnabled = false,
  manifestLoading = false,
  keyPrefix,
  jsonFallbackMinHeight = 200,
}: FacetReadOnlyBodyProps) {
  const prefix = keyPrefix ?? facetTypeKey;
  const structuralView = facetTypeKey.endsWith(':structural') && structuralFacetEnabled;

  if (structuralView) {
    return <StructuralFacet facet={payload as StructuralFacetData} />;
  }

  if (manifestLoading && !descriptor?.payload) {
    return <FacetReadOnlyBodySkeleton minHeight={jsonFallbackMinHeight} />;
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
