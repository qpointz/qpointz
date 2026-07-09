import { ActionIcon, Group, Tooltip, useMantineColorScheme } from '@mantine/core';
import type { MouseEvent } from 'react';
import {
  HiOutlineArrowTopRightOnSquare,
  HiOutlineCheck,
  HiOutlineClipboardDocument,
  HiOutlinePencilSquare,
  HiOutlinePlay,
  HiOutlineXMark,
} from 'react-icons/hi2';
import type { ArtifactActionId } from './types';
import {
  artifactToolbarActionIconProps,
  artifactToolbarIconColor,
} from './artifactToolbar';
import { ArtifactToolbarIcon } from './ArtifactToolbarIcon';

export interface InlineArtifactStripActionBarProps {
  enabledActions: ArtifactActionId[];
  onApply?: () => void;
  onApplyAndRun?: () => void;
  onCopy?: () => void;
  onOpenInModel?: () => void;
  onAccept?: () => void;
  onReject?: () => void;
  copyCopied?: boolean;
  disableApplyAndRun?: boolean;
  applied?: boolean;
  isLifecycleBusy?: boolean;
}

/** Icon-only actions for inline artifact pill strips and popover toolbars. */
export function InlineArtifactStripActionBar({
  enabledActions,
  onApply,
  onApplyAndRun,
  onCopy,
  onOpenInModel,
  onAccept,
  onReject,
  copyCopied = false,
  disableApplyAndRun = false,
  applied = false,
  isLifecycleBusy = false,
}: InlineArtifactStripActionBarProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const idleColor = artifactToolbarIconColor(isDark);
  const actionIconProps = artifactToolbarActionIconProps;
  const show = (id: ArtifactActionId) => enabledActions.includes(id);

  const preventFocusScroll = (event: MouseEvent) => {
    event.preventDefault();
  };

  return (
    <Group gap={2} wrap="nowrap" justify="flex-end" onClick={(event) => event.stopPropagation()}>
      {show('apply') && onApply && !applied ? (
        <Tooltip label="Apply to editor" withArrow>
          <ActionIcon
            {...actionIconProps}
            color={idleColor}
            onClick={onApply}
            onMouseDown={preventFocusScroll}
            aria-label="Apply"
          >
            <ArtifactToolbarIcon icon={HiOutlinePencilSquare} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('apply-and-run') && onApplyAndRun ? (
        <Tooltip label="Apply and run" withArrow>
          <ActionIcon
            {...actionIconProps}
            color="teal"
            onClick={onApplyAndRun}
            onMouseDown={preventFocusScroll}
            disabled={disableApplyAndRun}
            aria-label="Apply and run"
          >
            <ArtifactToolbarIcon icon={HiOutlinePlay} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('reject') && onReject ? (
        <Tooltip label="Reject facet" withArrow>
          <ActionIcon
            {...actionIconProps}
            color="red"
            onClick={onReject}
            onMouseDown={preventFocusScroll}
            loading={isLifecycleBusy}
            aria-label="Reject"
          >
            <ArtifactToolbarIcon icon={HiOutlineXMark} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('accept') && onAccept ? (
        <Tooltip label="Accept facet" withArrow>
          <ActionIcon
            {...actionIconProps}
            color="teal"
            onClick={onAccept}
            onMouseDown={preventFocusScroll}
            loading={isLifecycleBusy}
            aria-label="Accept"
          >
            <ArtifactToolbarIcon icon={HiOutlineCheck} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('open-in-model') && onOpenInModel ? (
        <Tooltip label="Open in model" withArrow>
          <ActionIcon
            {...actionIconProps}
            color={idleColor}
            onClick={onOpenInModel}
            onMouseDown={preventFocusScroll}
            aria-label="Open in model"
          >
            <ArtifactToolbarIcon icon={HiOutlineArrowTopRightOnSquare} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('copy') && onCopy ? (
        <Tooltip label={copyCopied ? 'Copied!' : 'Copy'} withArrow>
          <ActionIcon
            {...actionIconProps}
            color={copyCopied ? 'teal' : idleColor}
            onClick={onCopy}
            onMouseDown={preventFocusScroll}
            aria-label="Copy"
          >
            <ArtifactToolbarIcon icon={HiOutlineClipboardDocument} />
          </ActionIcon>
        </Tooltip>
      ) : null}
    </Group>
  );
}
