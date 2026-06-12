import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import type { Message } from '../../../../types/chat';
import { SqlDataCondensedPreview } from '../SqlDataCondensedPreview';
import type { ArtifactRenderGroup } from '../types';

const runMock = vi.fn(async () => ({
  columns: [{ name: 'c1', type: 'INTEGER' }],
  rows: [{ c1: 1 }],
  rowCount: 1,
  executionTimeMs: 1,
  page: {
    executionId: 'exec-1',
    pageIndex: 0,
    pageSize: 50,
    epoch: 0,
    pageRowCount: 1,
    hasNext: false,
    hasPrevious: false,
    totalResult: 1,
  },
}));

vi.mock('../../../data/QueryDataView', () => ({
  QueryDataView: () => <div data-testid="query-data-view" />,
}));

vi.mock('../SqlReadOnlyPanel', () => ({
  SqlReadOnlyPanel: () => <div data-testid="sql-read-only" />,
}));

vi.mock('../useChatArtifactRun', () => ({
  useChatArtifactRun: () => ({ run: runMock, closePriorSession: vi.fn() }),
}));

vi.mock('../useLazyArtifactData', () => ({
  useLazyArtifactData: () => ({
    containerRef: { current: null },
    result: null,
    error: null,
    isLoading: false,
    fetchPage: vi.fn(),
    reset: vi.fn(),
    setResult: vi.fn(),
    setError: vi.fn(),
  }),
}));

vi.mock('../useOpenInAnalysis', () => ({
  useOpenInAnalysis: () => vi.fn(),
}));

vi.mock('../../expand/useChatExpand', () => ({
  useChatExpand: () => ({ openExpand: vi.fn(), runAllTick: 0, notifyRunAllComplete: vi.fn() }),
}));

vi.mock('../../../../features/FeatureFlagContext', () => ({
  useFeatureFlags: () => ({ chatSqlExecute: true, chatAgentPicker: false }),
}));

function renderPreview(message: Message) {
  const group: ArtifactRenderGroup = {
    kind: 'sql-data-composite',
    sql: { kind: 'sql', sql: 'SELECT 1', dialectId: 'CALCITE' },
  };
  return render(
    <MantineProvider>
      <SqlDataCondensedPreview
        chatType="general"
        message={message}
        group={group}
        conversationId="chat-1"
      />
    </MantineProvider>,
  );
}

describe('SqlDataCondensedPreview', () => {
  beforeEach(() => {
    runMock.mockClear();
  });

  it('should auto-run SQL for live assistant turns', async () => {
    renderPreview({
      id: 'live-1',
      conversationId: 'chat-1',
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      restReplay: false,
      artifacts: [{ kind: 'sql', sql: 'SELECT 1', dialectId: 'CALCITE' }],
    });

    await waitFor(() => {
      expect(runMock).toHaveBeenCalledTimes(1);
    });
  });

  it('should not auto-run SQL for REST replayed turns', async () => {
    renderPreview({
      id: 'turn-1',
      conversationId: 'chat-1',
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      restReplay: true,
      artifacts: [{ kind: 'sql', sql: 'SELECT 1', dialectId: 'CALCITE' }],
    });

    expect(screen.getByRole('tab', { name: 'SQL' })).toBeInTheDocument();
    await waitFor(() => {
      expect(runMock).not.toHaveBeenCalled();
    });
  });

  it('should not render Generated SQL header row', () => {
    renderPreview({
      id: 'turn-1',
      conversationId: 'chat-1',
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      restReplay: true,
      artifacts: [{ kind: 'sql', sql: 'SELECT 1', dialectId: 'CALCITE' }],
    });

    expect(screen.queryByText(/Generated SQL/i)).toBeNull();
    expect(screen.queryByText(/CALCITE/i)).toBeNull();
  });
});
