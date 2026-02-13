import { Box, Group, Button, Textarea, Tooltip, ActionIcon, Text, useMantineColorScheme } from '@mantine/core';
import { useState, useCallback } from 'react';
import {
  HiOutlinePlay,
  HiOutlineSparkles,
  HiOutlineClipboardDocument,
  HiOutlineTrash,
  HiOutlineCommandLine,
} from 'react-icons/hi2';
import { format as formatSQL } from 'sql-formatter';
import { InlineChatButton } from '../common/InlineChatButton';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

interface QueryEditorProps {
  sql: string;
  onChange: (sql: string) => void;
  onExecute: () => void;
  isExecuting: boolean;
  queryId?: string | null;
  queryName?: string | null;
  queryDescription?: string | null;
}

export function QueryEditor({ sql, onChange, onExecute, isExecuting, queryId, queryName, queryDescription }: QueryEditorProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const [copied, setCopied] = useState(false);

  const handleFormat = useCallback(() => {
    try {
      const formatted = formatSQL(sql, {
        language: 'sql',
        tabWidth: 2,
        keywordCase: 'upper',
        linesBetweenQueries: 2,
      });
      onChange(formatted);
    } catch {
      // If formatting fails, leave as-is
    }
  }, [sql, onChange]);

  const handleCopy = useCallback(async () => {
    try {
      await navigator.clipboard.writeText(sql);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Clipboard API not available
    }
  }, [sql]);

  const handleClear = useCallback(() => {
    onChange('');
  }, [onChange]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    // Ctrl/Cmd + Enter to execute (only when execute flag is enabled)
    if (flags.analysisExecuteQuery && (e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      if (sql.trim() && !isExecuting) {
        onExecute();
      }
    }
  };

  const borderColor = 'var(--mantine-color-default-border)';

  return (
    <Box
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        minHeight: 0,
      }}
    >
      {/* Subheading — same style as Model / Knowledge headers */}
      <Box
        px="md"
        py="sm"
        style={{
          borderBottom: `1px solid ${borderColor}`,
          background: isDark
            ? 'linear-gradient(135deg, var(--mantine-color-dark-8) 0%, var(--mantine-color-dark-7) 100%)'
            : 'linear-gradient(135deg, var(--mantine-color-teal-0) 0%, white 100%)',
          flexShrink: 0,
        }}
      >
        <Group justify="space-between" wrap="nowrap">
          <Group gap="sm" wrap="nowrap">
            <Box
              style={{
                width: 32,
                height: 32,
                borderRadius: 8,
                backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <HiOutlineCommandLine
                size={16}
                color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
              />
            </Box>
            <Box style={{ minWidth: 0 }}>
              <Text size="md" fw={600} c={isDark ? 'gray.1' : 'gray.8'} truncate>
                {queryName || 'SQL Editor'}
              </Text>
              {queryDescription && (
                <Text size="xs" c="dimmed" truncate>
                  {queryDescription}
                </Text>
              )}
            </Box>
          </Group>
          <InlineChatButton
            contextType="analysis"
            contextId={queryId ?? '__analysis__'}
            contextLabel={queryName ?? 'Query Playground'}
            contextEntityType="Query"
          />
        </Group>
      </Box>

      {/* Editor area */}
      <Box style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
        <Textarea
          value={sql}
          onChange={(e) => onChange(e.currentTarget.value)}
          onKeyDown={handleKeyDown}
          placeholder="Write your SQL query here..."
          styles={{
            root: { height: '100%' },
            wrapper: { height: '100%' },
            input: {
              height: '100%',
              fontFamily: 'ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, "Liberation Mono", monospace',
              fontSize: 13,
              lineHeight: 1.6,
              border: 'none',
              borderRadius: 0,
              resize: 'none',
              backgroundColor: 'var(--mantine-color-body)',
              color: 'var(--mantine-color-text)',
              padding: 12,
            },
          }}
        />
      </Box>

      {/* Toolbar + Execute bar */}
      <Group
        justify="space-between"
        px="sm"
        py={6}
        style={{
          borderTop: `1px solid ${borderColor}`,
          backgroundColor: isDark ? 'var(--mantine-color-dark-8)' : 'var(--mantine-color-gray-0)',
          flexShrink: 0,
        }}
      >
        <Group gap={4}>
          {flags.analysisFormatSql && (
            <Tooltip label="Format SQL" withArrow>
              <ActionIcon
                variant="subtle"
                size="sm"
                color={isDark ? 'gray.4' : 'gray.6'}
                onClick={handleFormat}
                disabled={!sql.trim()}
              >
                <HiOutlineSparkles size={14} />
              </ActionIcon>
            </Tooltip>
          )}
          {flags.analysisCopySql && (
            <Tooltip label={copied ? 'Copied!' : 'Copy SQL'} withArrow>
              <ActionIcon
                variant="subtle"
                size="sm"
                color={copied ? 'teal' : isDark ? 'gray.4' : 'gray.6'}
                onClick={handleCopy}
                disabled={!sql.trim()}
              >
                <HiOutlineClipboardDocument size={14} />
              </ActionIcon>
            </Tooltip>
          )}
          {flags.analysisClearSql && (
            <Tooltip label="Clear" withArrow>
              <ActionIcon
                variant="subtle"
                size="sm"
                color={isDark ? 'gray.4' : 'gray.6'}
                onClick={handleClear}
                disabled={!sql.trim()}
              >
                <HiOutlineTrash size={14} />
              </ActionIcon>
            </Tooltip>
          )}
          <Text size="xs" c="dimmed" ml="xs">
            Ctrl+Enter to run
          </Text>
        </Group>
        {flags.analysisExecuteQuery && (
          <Button
            size="xs"
            leftSection={<HiOutlinePlay size={14} />}
            color={isDark ? 'cyan' : 'teal'}
            onClick={onExecute}
            loading={isExecuting}
            disabled={!sql.trim()}
          >
            Run Query
          </Button>
        )}
      </Group>
    </Box>
  );
}
