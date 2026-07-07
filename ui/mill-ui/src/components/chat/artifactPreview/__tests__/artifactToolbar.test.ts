import { describe, it, expect } from 'vitest';
import {
  ARTIFACT_TOOLBAR_ICON_SIZE,
  artifactToolbarIconColor,
  artifactToolbarActiveColor,
  artifactToolbarIconCss,
  artifactToolbarActiveIconCss,
  artifactToolbarActionIconProps,
  buildArtifactTabsStyles,
} from '../artifactToolbar';

describe('artifactToolbar', () => {
  it('should expose shared icon size and action icon defaults', () => {
    expect(ARTIFACT_TOOLBAR_ICON_SIZE).toBe(14);
    expect(artifactToolbarActionIconProps).toEqual({ variant: 'subtle', size: 'sm' });
  });

  it('should use theme-aware idle and active colors', () => {
    expect(artifactToolbarIconColor(false)).toBe('gray.5');
    expect(artifactToolbarIconColor(true)).toBe('gray.4');
    expect(artifactToolbarActiveColor(false)).toBe('teal');
    expect(artifactToolbarActiveColor(true)).toBe('cyan');
    expect(artifactToolbarIconCss(false)).toContain('gray-5');
    expect(artifactToolbarActiveIconCss(true)).toContain('cyan');
  });

  it('should build minimal tab layout styles (chrome lives in CSS module)', () => {
    const styles = buildArtifactTabsStyles();

    expect(styles.root?.display).toBe('block');
    expect(styles.panel?.paddingTop).toBe(0);
    expect(styles.tab).toBeUndefined();
  });
});
