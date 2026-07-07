import { describe, expect, it, vi } from 'vitest';
import type { ComponentProps } from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MantineProvider } from '@mantine/core';
import { QueryDataView } from '../QueryDataView';
import type { QueryResult } from '../../../types/query';

vi.mock('../../../services/api', () => ({
  fetchExportFormats: vi.fn(async () => []),
  downloadSqlExport: vi.fn(),
}));

const sampleResult: QueryResult = {
  columns: [{ name: 'country', type: 'string' }],
  rows: [{ country: 'US' }],
  rowCount: 1,
  executionTimeMs: 12,
  page: {
    executionId: 'exec-1',
    epoch: 0,
    pageIndex: 0,
    pageSize: 50,
    pageRowCount: 1,
    totalResult: 1,
    hasNext: false,
    hasPrevious: false,
  },
};

function renderView(props: Partial<ComponentProps<typeof QueryDataView>> = {}) {
  return render(
    <MantineProvider>
      <QueryDataView
        mode="condensed"
        result={null}
        error={null}
        isExecuting={false}
        {...props}
      />
    </MantineProvider>,
  );
}

describe('QueryDataView', () => {
  it('shouldPreferGridOverStaleError_whenResultAndErrorBothPresent', () => {
    renderView({
      result: sampleResult,
      error: 'Query failed (422): sql_execution_failed',
    });

    expect(screen.getByText('US')).toBeInTheDocument();
    expect(screen.queryByText(/Query failed/)).not.toBeInTheDocument();
  });

  it('shouldShowExecutingBeforeError_whenQueryIsRunning', () => {
    renderView({
      isExecuting: true,
      error: 'Query failed (422): sql_execution_failed',
    });

    expect(screen.getByText('Loading')).toBeInTheDocument();
    expect(screen.queryByText(/Query failed/)).not.toBeInTheDocument();
  });

  it('shouldShowErrorIcon_whenQueryFails', () => {
    renderView({
      error: 'Query failed (422): sql_execution_failed',
    });

    expect(screen.getByText('Error')).toBeInTheDocument();
    expect(screen.getByLabelText('Show error details')).toBeInTheDocument();
    expect(screen.queryByText(/sql_execution_failed/)).not.toBeInTheDocument();
  });

  it('shouldKeepGridVisible_whenPageIsLoading', () => {
    renderView({
      result: sampleResult,
      isPageLoading: true,
    });

    expect(screen.getByText('US')).toBeInTheDocument();
    expect(screen.queryByText('Loading page...')).not.toBeInTheDocument();
  });

  it('shouldShowFullErrorInModal_whenErrorIconClicked', async () => {
    const user = userEvent.setup();
    renderView({
      error: 'Query failed (422): sql_execution_failed',
    });

    await user.click(screen.getByLabelText('Show error details'));
    expect(await screen.findByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(/sql_execution_failed/)).toBeInTheDocument();
  });
});
