import { forwardRef, useImperativeHandle, useState, useEffect, useRef } from "react";
import { Box, Group, Text, ActionIcon, Stack } from "@mantine/core";
import { TbX } from "react-icons/tb";
import { useMantineTheme } from "@mantine/core";
import { RingsLoader } from "./RingsLoader";

export interface StatusMode {
    type: 'clarification' | string; // extensible
    label: string;
    onCancel?: () => void;
}

export interface StatusIndicatorProps {
    mode?: StatusMode;
}

export interface StatusIndicatorRef {
    show: (message: string, durationSeconds?: number) => void;
    hide: () => void;
}

/**
 * Reusable status indicator component that displays:
 * - Mode indicator (e.g., clarification mode) with optional cancel button
 * - Event feedback notifications (via show/hide API) for transient status updates
 * 
 * Both can be displayed independently or simultaneously.
 */
export const StatusIndicator = forwardRef<StatusIndicatorRef, StatusIndicatorProps>(
    ({ mode }, ref) => {
        const theme = useMantineTheme();
        const [eventMessage, setEventMessage] = useState<string | undefined>(undefined);
        const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

        // Expose show/hide API via ref
        useImperativeHandle(ref, () => ({
            show: (message: string, durationSeconds?: number) => {
                setEventMessage(message);
                
                // Clear any existing timeout
                if (timeoutRef.current) {
                    clearTimeout(timeoutRef.current);
                    timeoutRef.current = null;
                }
                
                // Set new timeout if duration provided
                if (durationSeconds !== undefined && durationSeconds > 0) {
                    timeoutRef.current = setTimeout(() => {
                        setEventMessage(undefined);
                        timeoutRef.current = null;
                    }, durationSeconds * 1000);
                }
            },
            hide: () => {
                setEventMessage(undefined);
                if (timeoutRef.current) {
                    clearTimeout(timeoutRef.current);
                    timeoutRef.current = null;
                }
            }
        }), []);

        // Cleanup timeout on unmount
        useEffect(() => {
            return () => {
                if (timeoutRef.current) {
                    clearTimeout(timeoutRef.current);
                }
            };
        }, []);

        // Don't render anything if neither mode nor event is present
        if (!mode && !eventMessage) {
            return null;
        }

        return (
            <Box>
                <Stack gap="xs">
                    {/* Mode Indicator */}
                    {mode && (
                        <Box
                            bg={theme.colors.gray[0]}
                            p={6}
                            style={{
                                borderRadius: 4,
                                border: `1px solid ${theme.colors.gray[2]}`
                            }}
                        >
                            <Group justify="space-between" wrap="nowrap" gap="xs">
                                <Text
                                    size="xs"
                                    c={theme.colors.gray[7]}
                                    style={{ flex: 1 }}
                                    truncate
                                >
                                    clarify: {mode.label}
                                </Text>
                                {mode.onCancel && (
                                    <ActionIcon
                                        size="xs"
                                        variant="subtle"
                                        color="gray"
                                        onClick={mode.onCancel}
                                        style={{ flexShrink: 0 }}
                                    >
                                        <TbX size={12} />
                                    </ActionIcon>
                                )}
                            </Group>
                        </Box>
                    )}

                    {/* Event Feedback */}
                    {eventMessage && (
                        <Group gap="xs" align="center" py={4}>
                            <RingsLoader size={14} />
                            <Text size="xs" c={theme.colors.gray[5]}>
                                {eventMessage}
                            </Text>
                        </Group>
                    )}
                </Stack>
            </Box>
        );
    }
);

StatusIndicator.displayName = "StatusIndicator";
