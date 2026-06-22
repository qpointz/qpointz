import { CodeHighlight } from '@mantine/code-highlight';
import { Box } from '@mantine/core';

interface FacetJsonReadOnlyPanelProps {
  /** Pretty-printed JSON for the wire facet-proposal object. */
  json: string;
  /** Bounded height for in-chat condensed view. */
  maxHeight?: number;
}

/** JSON tab panel for facet condensed preview (mirrors {@link SqlReadOnlyPanel} height bounds). */
export function FacetJsonReadOnlyPanel({ json, maxHeight = 220 }: FacetJsonReadOnlyPanelProps) {
  return (
    <Box style={{ maxHeight, overflow: 'auto', borderRadius: 6 }}>
      <CodeHighlight code={json} language="json" withCopyButton />
    </Box>
  );
}
