import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MantineProvider } from '@mantine/core';
import { SqlDataInlineArtifactStrip } from '../SqlDataInlineArtifactStrip';
import {
  isAnalysisAppliedArtifact,
  setAnalysisAppliedArtifactKey,
} from '../../../queries/analysisHostState';
import type { Message } from '../../../../types/chat';

function renderStrip(message: Message, sqlArtifact: NonNullable<Message['artifacts']>[number]) {
  return render(
    <MantineProvider>
      <SqlDataInlineArtifactStrip
        chatType="inline-analysis"
        message={message}
        conversationId="conv-1"
        chatTitle="Top orders"
        group={{
          kind: 'sql-data-composite',
          sql: sqlArtifact as Extract<typeof sqlArtifact, { kind: 'sql' }>,
        }}
      />
    </MantineProvider>,
  );
}

describe('SqlDataInlineArtifactStrip', () => {
  it('should render pill headline and strip apply icons', () => {
    renderStrip(
      {
        id: 'm1',
        conversationId: 'conv-1',
        role: 'assistant',
        content: 'Here is a proposal',
        timestamp: Date.now(),
      },
      {
        kind: 'sql',
        sql: 'SELECT 1',
        info: {
          title: 'Revenue by segment',
          description: 'Adds revenue_per_customer',
        },
      },
    );

    expect(screen.getByText('Revenue by segment')).toBeInTheDocument();
    expect(screen.getByLabelText('Apply')).toBeInTheDocument();
    expect(screen.getByLabelText('Apply and run')).toBeInTheDocument();
    expect(screen.queryByLabelText('Copy')).not.toBeInTheDocument();
  });

  it('should mark only the applied artifact when SQL text is identical', async () => {
    setAnalysisAppliedArtifactKey(null);

    const firstSql = {
      kind: 'sql' as const,
      sql: 'SELECT 1',
      artifactId: 'sql-turn-a',
      info: { title: 'First proposal' },
    };
    const secondSql = {
      kind: 'sql' as const,
      sql: 'SELECT 1',
      artifactId: 'sql-turn-b',
      info: { title: 'Second proposal' },
    };

    const { unmount: unmountFirst } = renderStrip(
      {
        id: 'm1',
        conversationId: 'conv-1',
        role: 'assistant',
        content: 'First',
        timestamp: Date.now(),
        artifacts: [firstSql],
      },
      firstSql,
    );

    const { unmount: unmountSecond } = renderStrip(
      {
        id: 'm2',
        conversationId: 'conv-1',
        role: 'assistant',
        content: 'Second',
        timestamp: Date.now(),
        artifacts: [secondSql],
      },
      secondSql,
    );

    await userEvent.click(screen.getAllByLabelText('Apply')[0]!);

    expect(isAnalysisAppliedArtifact('sql-turn-a')).toBe(true);
    expect(isAnalysisAppliedArtifact('sql-turn-b')).toBe(false);
    expect(screen.getAllByLabelText('Applied')).toHaveLength(1);

    unmountFirst();
    unmountSecond();
  });
});
