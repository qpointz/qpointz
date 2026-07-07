import { Box, useMantineColorScheme } from '@mantine/core';
import { HiOutlineSparkles } from 'react-icons/hi2';
import { chatAccentCss, chatAccentSoftBg } from './chatChrome';

interface AssistantAvatarProps {
  /** Pixel size of the avatar tile (default 28). */
  size?: number;
}

/**
 * Compact assistant identity marker shown beside thinking state and replies.
 */
export function AssistantAvatar({ size = 28 }: AssistantAvatarProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const iconSize = Math.round(size * 0.5);

  return (
    <Box
      aria-hidden
      style={{
        width: size,
        height: size,
        borderRadius: '50%',
        flexShrink: 0,
        backgroundColor: chatAccentSoftBg(isDark),
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        marginTop: 2,
      }}
    >
      <HiOutlineSparkles size={iconSize} color={chatAccentCss(isDark)} />
    </Box>
  );
}
