import type { CSSProperties } from 'react';

/**
 * Shared layout and color tokens for the general chat thread.
 * Keeps message list, bubbles, and composer visually aligned.
 */

/** Max width for centered prose (user bubbles, input, empty state). */
export const CHAT_CONTENT_MAX_WIDTH = 900;

/** Scroll padding so the last message clears the floating composer. */
export const CHAT_BOTTOM_PADDING = 120;

/** Mantine color name for chat accents (teal light / cyan dark). */
export function chatAccentColor(isDark: boolean): 'teal' | 'cyan' {
  return isDark ? 'cyan' : 'teal';
}

export function chatAccentCss(isDark: boolean): string {
  return isDark ? 'var(--mantine-color-cyan-5)' : 'var(--mantine-color-teal-6)';
}

export function chatAccentSoftBg(isDark: boolean): string {
  return isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)';
}

/** Subtle wash behind the scrollable transcript. */
export function chatTranscriptBackground(isDark: boolean): string {
  const base = 'var(--mantine-color-body)';
  const glow = isDark
    ? 'radial-gradient(ellipse 80% 50% at 50% -10%, rgba(34, 184, 207, 0.07) 0%, transparent 60%)'
    : 'radial-gradient(ellipse 80% 50% at 50% -10%, rgba(18, 184, 166, 0.08) 0%, transparent 60%)';
  return `${glow}, ${base}`;
}

export function userBubbleBackground(isDark: boolean): string {
  return isDark
    ? 'linear-gradient(135deg, var(--mantine-color-cyan-7) 0%, var(--mantine-color-cyan-8) 100%)'
    : 'linear-gradient(135deg, var(--mantine-color-teal-6) 0%, var(--mantine-color-teal-7) 100%)';
}

export function composerSurfaceStyle(isDark: boolean, focused: boolean): CSSProperties {
  return {
    backgroundColor: isDark ? 'var(--mantine-color-dark-6)' : 'var(--mantine-color-white)',
    borderRadius: 20,
    border: `1px solid ${
      focused
        ? isDark
          ? 'var(--mantine-color-cyan-7)'
          : 'var(--mantine-color-teal-4)'
        : isDark
          ? 'var(--mantine-color-dark-4)'
          : 'var(--mantine-color-gray-3)'
    }`,
    boxShadow: focused
      ? isDark
        ? '0 0 0 1px rgba(34, 184, 207, 0.25), 0 4px 16px rgba(0, 0, 0, 0.25)'
        : '0 0 0 1px rgba(18, 184, 166, 0.2), 0 4px 16px rgba(0, 0, 0, 0.06)'
      : isDark
        ? '0 2px 8px rgba(0, 0, 0, 0.2)'
        : '0 2px 10px rgba(0, 0, 0, 0.04)',
    transition: 'border-color 150ms ease, box-shadow 150ms ease',
  };
}
