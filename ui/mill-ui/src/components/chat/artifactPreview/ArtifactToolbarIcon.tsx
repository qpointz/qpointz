import type { IconType } from 'react-icons';
import { ARTIFACT_TOOLBAR_ICON_SIZE } from './artifactToolbar';

/** Heroicons-outline stroke to match ChatArtifactActionBar subtle icons. */
export const ARTIFACT_TOOLBAR_ICON_STROKE = 1.5;

interface ArtifactToolbarIconProps {
  icon: IconType;
}

/** Normalized outline icon for artefact tabs and toolbars. */
export function ArtifactToolbarIcon({ icon: Icon }: ArtifactToolbarIconProps) {
  return (
    <Icon
      size={ARTIFACT_TOOLBAR_ICON_SIZE}
      strokeWidth={ARTIFACT_TOOLBAR_ICON_STROKE}
      aria-hidden
    />
  );
}
