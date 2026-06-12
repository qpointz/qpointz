import { Box, Group, Text, useMantineColorScheme, type BoxProps } from '@mantine/core';
import type { ReactNode } from 'react';
import {
  CONTENT_PANE_ICON_TILE_SIZE,
  contentPaneHeaderShellStyle,
} from './layoutChrome';

export type ContentPaneHeaderProps = BoxProps & {
  /** Leading icon component (react-icons). */
  icon: React.ComponentType<{ size: number; color?: string }>;
  title?: string;
  subtitle?: string;
  /** Optional badge or label beside the title (entity type, profile, etc.). */
  titleAddon?: ReactNode;
  /** Replaces the default title / subtitle block (e.g. analysis rename field). */
  titleContent?: ReactNode;
  actions?: ReactNode;
};

/**
 * Shared content header for explorer main panes (model entity, analysis query, general chat).
 * Fixed height via {@link CONTENT_PANE_HEADER_HEIGHT} in layoutChrome.
 */
export function ContentPaneHeader({
  icon: Icon,
  title,
  subtitle,
  titleAddon,
  titleContent,
  actions,
  style,
  ...rest
}: ContentPaneHeaderProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const accentColor = isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)';
  const iconTileBg = isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)';

  return (
    <Box style={{ ...contentPaneHeaderShellStyle(isDark), ...style }} {...rest}>
      <Group gap="md" justify="space-between" wrap="nowrap" style={{ width: '100%', minWidth: 0 }}>
        <Group gap="md" wrap="nowrap" style={{ minWidth: 0, flex: 1 }}>
          <Box
            style={{
              width: CONTENT_PANE_ICON_TILE_SIZE,
              height: CONTENT_PANE_ICON_TILE_SIZE,
              borderRadius: 8,
              backgroundColor: iconTileBg,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <Icon size={20} color={accentColor} />
          </Box>
          <Box style={{ minWidth: 0, flex: 1 }}>
            {titleContent ?? (
              <>
                <Group gap="xs" wrap="nowrap" style={{ minWidth: 0 }}>
                  {title ? (
                    <Text size="lg" fw={600} c={isDark ? 'gray.1' : 'gray.8'} truncate>
                      {title}
                    </Text>
                  ) : null}
                  {titleAddon}
                </Group>
                {subtitle ? (
                  <Text size="sm" c="dimmed" ff="monospace" truncate title={subtitle}>
                    {subtitle}
                  </Text>
                ) : null}
              </>
            )}
          </Box>
        </Group>
        {actions ? (
          <Group gap={4} wrap="nowrap" style={{ flexShrink: 0 }}>
            {actions}
          </Group>
        ) : null}
      </Group>
    </Box>
  );
}
