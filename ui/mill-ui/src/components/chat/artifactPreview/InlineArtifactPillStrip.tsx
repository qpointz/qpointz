import { Box, Group, Popover, Text } from '@mantine/core';
import { useState, type ReactNode } from 'react';
import { HiOutlineArrowsPointingOut } from 'react-icons/hi2';
import { InlineArtifactTypeBadge } from './InlineArtifactTypeBadge';
import type { InlineArtifactTypeBadgeInput } from './inlineArtifactTypeBadgeModel';
import classes from './InlineArtifactStrip.module.css';

export interface InlineArtifactPillStripProps {
  ariaLabel: string;
  typeBadge: InlineArtifactTypeBadgeInput;
  headline: string;
  applied?: boolean;
  rejected?: boolean;
  stripActions: ReactNode;
  popoverBody: ReactNode;
  popoverActions?: ReactNode;
  popoverTitle?: string;
  popoverSubtitle?: string;
}

/** Compact single-line pill row with popover preview for inline artifact strips. */
export function InlineArtifactPillStrip({
  ariaLabel,
  typeBadge,
  headline,
  applied = false,
  rejected = false,
  stripActions,
  popoverBody,
  popoverActions,
  popoverTitle,
  popoverSubtitle,
}: InlineArtifactPillStripProps) {
  const [previewOpen, setPreviewOpen] = useState(false);

  const togglePreview = () => setPreviewOpen((open) => !open);

  return (
    <Popover
      opened={previewOpen}
      onChange={setPreviewOpen}
      position="left"
      withArrow
      shadow="md"
      trapFocus={false}
    >
      <Popover.Target>
        <Box
          className={`${classes.pillRoot}${rejected ? ` ${classes.pillRootRejected}` : ''}`}
          aria-label={ariaLabel}
        >
          <Box
            className={`${classes.previewZone}${previewOpen ? ` ${classes.previewZoneExpanded}` : ''}`}
            role="button"
            tabIndex={0}
            aria-expanded={previewOpen}
            aria-label={`${ariaLabel}. Click to preview.`}
            onClick={togglePreview}
            onKeyDown={(event) => {
              if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                togglePreview();
              }
            }}
          >
            <InlineArtifactTypeBadge {...typeBadge} className={classes.typeBadge} />
            <Box
              component="span"
              className={`${classes.headline}${rejected ? ` ${classes.headlineRejected}` : ''}`}
            >
              {headline}
            </Box>
            <span className={classes.expandHint} aria-hidden>
              <HiOutlineArrowsPointingOut size={10} strokeWidth={1.5} />
            </span>
          </Box>
          <Box className={classes.stripActions} onClick={(event) => event.stopPropagation()}>
            {stripActions}
          </Box>
          {applied ? <Box className={classes.appliedDot} aria-label="Applied" /> : null}
        </Box>
      </Popover.Target>
      <Popover.Dropdown className={classes.previewBody} onClick={(event) => event.stopPropagation()}>
        {popoverTitle ? (
          <Box
            component="span"
            display="block"
            mb={popoverSubtitle ? 4 : 'xs'}
            td={rejected ? 'line-through' : undefined}
            c={rejected ? 'dimmed' : undefined}
          >
            {popoverTitle}
          </Box>
        ) : null}
        {popoverSubtitle ? (
          <Text size="xs" c="dimmed" mb="xs">
            {popoverSubtitle}
          </Text>
        ) : null}
        {popoverBody}
        {popoverActions ? (
          <Group gap={4} justify="flex-end" mt="xs" wrap="nowrap">
            {popoverActions}
          </Group>
        ) : null}
      </Popover.Dropdown>
    </Popover>
  );
}
