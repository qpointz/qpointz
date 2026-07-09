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
import { useState, useCallback, useRef, useEffect, type Ref } from 'react';
import { useParams } from 'react-router';
import { SqlCodeEditor, type SqlCodeEditorHandle } from './SqlCodeEditor';
import {
  HiOutlinePlay,
  HiOutlineCommandLine,
  HiOutlineBookmark,
  HiOutlinePencil,
  HiOutlineArrowUturnLeft,
  HiOutlineArrowUturnRight,
} from 'react-icons/hi2';
import { InlineChatButton } from '../common/InlineChatButton';
import { ContentPaneHeader } from '../layout/ContentPaneHeader';
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
  /** Remount key to reset editor undo history (e.g. after Save). */
  editorEpoch?: number;
  /** Optional ref to the underlying SQL editor imperative API. */
  editorRef?: Ref<SqlCodeEditorHandle>;
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
  editorEpoch = 0,
  editorRef,
}: QueryEditorProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const params = useParams<{ queryId?: string }>();
  const inlineChatContextId = queryId ?? params.queryId ?? '__analysis__';
  const [isEditingName, setIsEditingName] = useState(false);
  const [draftName, setDraftName] = useState(queryName ?? '');
  const nameInputRef = useRef<HTMLInputElement>(null);
  const localEditorRef = useRef<SqlCodeEditorHandle>(null);
  const [canUndo, setCanUndo] = useState(false);
  const [canRedo, setCanRedo] = useState(false);

  const refreshHistoryState = useCallback(() => {
    setCanUndo(localEditorRef.current?.canUndo() ?? false);
    setCanRedo(localEditorRef.current?.canRedo() ?? false);
  }, []);

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
      <ContentPaneHeader
        icon={HiOutlineCommandLine}
        titleContent={
          <>
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
                <Text size="lg" fw={600} c={isDark ? 'gray.1' : 'gray.8'} truncate>
                  {queryName || 'SQL Editor'}
                </Text>
                {queryId && onQueryNameChange ? (
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
                ) : null}
              </Group>
            )}
            {queryDescription ? (
              <Text size="sm" c="dimmed" truncate>
                {queryDescription}
              </Text>
            ) : null}
          </>
        }
        actions={
          <>
            <Tooltip label="Undo" withArrow>
              <ActionIcon
                size="sm"
                variant="subtle"
                color="gray"
                disabled={!canUndo}
                onClick={() => {
                  localEditorRef.current?.undo();
                  refreshHistoryState();
                }}
                aria-label="Undo"
              >
                <HiOutlineArrowUturnLeft size={14} />
              </ActionIcon>
            </Tooltip>
            <Tooltip label="Redo" withArrow>
              <ActionIcon
                size="sm"
                variant="subtle"
                color="gray"
                disabled={!canRedo}
                onClick={() => {
                  localEditorRef.current?.redo();
                  refreshHistoryState();
                }}
                aria-label="Redo"
              >
                <HiOutlineArrowUturnRight size={14} />
              </ActionIcon>
            </Tooltip>
            {onSave ? (
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
            ) : null}
            {flags.analysisExecuteQuery ? (
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
            ) : null}
            <InlineChatButton
              contextType="analysis"
              contextId={inlineChatContextId}
              contextLabel={queryName ?? 'Query Playground'}
              contextEntityType="Query"
            />
          </>
        }
      />

      <Box style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
        <SqlCodeEditor
          key={editorEpoch}
          ref={(instance) => {
            localEditorRef.current = instance;
            if (typeof editorRef === 'function') {
              editorRef(instance);
            } else if (editorRef) {
              editorRef.current = instance;
            }
          }}
          value={sql}
          onChange={(next) => {
            onChange(next);
            window.requestAnimationFrame(refreshHistoryState);
          }}
          onExecute={handleEditorExecute}
          executeEnabled={flags.analysisExecuteQuery}
        />
      </Box>
    </Box>
  );
}
