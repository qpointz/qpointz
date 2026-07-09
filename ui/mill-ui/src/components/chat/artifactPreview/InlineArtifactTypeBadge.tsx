import { Badge, Tooltip } from '@mantine/core';
import {
  INLINE_ARTIFACT_TYPE_COLORS,
  inlineArtifactTypeLabel,
  resolveInlineArtifactTypeBadge,
  type InlineArtifactTypeBadgeInput,
} from './inlineArtifactTypeBadgeModel';

export interface InlineArtifactTypeBadgeProps extends InlineArtifactTypeBadgeInput {
  className?: string;
}

/** Colored type badge for inline artifact pill strips. */
export function InlineArtifactTypeBadge(props: InlineArtifactTypeBadgeProps) {
  const { className, ...input } = props;
  const code = resolveInlineArtifactTypeBadge(input);
  return (
    <Tooltip label={inlineArtifactTypeLabel(code)} withArrow>
      <Badge
        size="xs"
        variant="filled"
        color={INLINE_ARTIFACT_TYPE_COLORS[code]}
        className={className}
        aria-hidden
      >
        {code}
      </Badge>
    </Tooltip>
  );
}
