import {
  Box,
  Group,
  Button,
  ActionIcon,
  Text,
  TextInput,
  Tooltip,
  useMantineColorScheme,
} from '@mantine/core';
import { useState, useCallback, useRef, useEffect } from 'react';
import { SqlCodeEditor } from './SqlCodeEditor';
import {
  HiOutlinePlay,
  HiOutlineCommandLine,
  HiOutlineBookmark,
  HiOutlinePencil,
} from 'react-icons/hi2';
import { InlineChatButton } from '../common/InlineChatButton';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { resolveSqlToExecute } from './resolveSqlToExecute';

interface QueryEditorProps {
  sql: string;
  onChange: (sql: string) => void;
  /** When `sqlFragment` is set, only that selection is executed (Ctrl/Cmd+Enter in the editor). */
  onExecute: (sqlFragment?: string) => void;
  onSave?: () => void;
  onQueryNameChange?: (name: string) => void;
  isExecuting: boolean;
  isSaving?: boolean;
  isDirty?: boolean;
  queryId?: string | null;
  queryName?: string | null;
  queryDescription?: string | null;
}

export function QueryEditor({
  sql,
  onChange,
  onExecute,
  onSave,
  onQueryNameChange,
  isExecuting,
  isSaving = false,
  isDirty = false,
  queryId,
  queryName,
  queryDescription,
}: QueryEditorProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const [isEditingName, setIsEditingName] = useState(false);
  const [draftName, setDraftName] = useState(queryName ?? '');
  const nameInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!isEditingName) {
      setDraftName(queryName ?? '');
    }
  }, [queryName, isEditingName]);

  useEffect(() => {
    if (isEditingName) {
      nameInputRef.current?.focus();
      nameInputRef.current?.select();
    }
  }, [isEditingName]);

  const commitNameEdit = useCallback(() => {
    const trimmed = draftName.trim();
    if (trimmed && trimmed !== queryName) {
      onQueryNameChange?.(trimmed);
    } else {
      setDraftName(queryName ?? '');
    }
    setIsEditingName(false);
  }, [draftName, onQueryNameChange, queryName]);

  const cancelNameEdit = useCallback(() => {
    setDraftName(queryName ?? '');
    setIsEditingName(false);
  }, [queryName]);

  const handleEditorExecute = useCallback((sqlFragment?: string) => {
    const sqlToRun = resolveSqlToExecute(sql, sqlFragment);
    if (sqlToRun && !isExecuting) {
      onExecute(sqlFragment?.trim() ? sqlFragment : undefined);
    }
  }, [sql, isExecuting, onExecute]);

  const borderColor = 'var(--mantine-color-default-border)';
  const accentColor = isDark ? 'cyan' : 'teal';

  return (
    <Box
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        minHeight: 0,
      }}
    >
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
          <Group gap="sm" wrap="nowrap" style={{ minWidth: 0, flex: 1 }}>
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
            <Box style={{ minWidth: 0, flex: 1 }}>
              {isEditingName && queryId ? (
                <TextInput
                  ref={nameInputRef}
                  size="xs"
                  value={draftName}
                  onChange={(event) => setDraftName(event.currentTarget.value)}
                  onBlur={commitNameEdit}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter') {
                      event.preventDefault();
                      commitNameEdit();
                    } else if (event.key === 'Escape') {
                      event.preventDefault();
                      cancelNameEdit();
                    }
                  }}
                  styles={{ input: { fontWeight: 600 } }}
                />
              ) : (
                <Group gap={4} wrap="nowrap" style={{ minWidth: 0 }}>
                  <Text size="md" fw={600} c={isDark ? 'gray.1' : 'gray.8'} truncate>
                    {queryName || 'SQL Editor'}
                  </Text>
                  {queryId && onQueryNameChange && (
                    <Tooltip label="Rename query" withArrow>
                      <ActionIcon
                        variant="subtle"
                        size="xs"
                        color="gray"
                        onClick={() => setIsEditingName(true)}
                        aria-label="Rename query"
                      >
                        <HiOutlinePencil size={12} />
                      </ActionIcon>
                    </Tooltip>
                  )}
                </Group>
              )}
              {queryDescription && (
                <Text size="xs" c="dimmed" truncate>
                  {queryDescription}
                </Text>
              )}
            </Box>
          </Group>
          <Group gap={4} wrap="nowrap">
            {onSave && (
              <Button
                size="xs"
                variant="light"
                leftSection={<HiOutlineBookmark size={14} />}
                color={accentColor}
                onClick={onSave}
                loading={isSaving}
                disabled={isSaving || (queryId ? !isDirty : !sql.trim())}
              >
                Save
              </Button>
            )}
            {flags.analysisExecuteQuery && (
              <Button
                size="xs"
                leftSection={<HiOutlinePlay size={14} />}
                color={accentColor}
                onClick={() => onExecute()}
                loading={isExecuting}
                disabled={!sql.trim()}
              >
                Run Query
              </Button>
            )}
            <InlineChatButton
              contextType="analysis"
              contextId={queryId ?? '__analysis__'}
              contextLabel={queryName ?? 'Query Playground'}
              contextEntityType="Query"
            />
          </Group>
        </Group>
      </Box>

      <Box style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
        <SqlCodeEditor
          value={sql}
          onChange={onChange}
          onExecute={handleEditorExecute}
          executeEnabled={flags.analysisExecuteQuery}
        />
      </Box>
    </Box>
  );
}
