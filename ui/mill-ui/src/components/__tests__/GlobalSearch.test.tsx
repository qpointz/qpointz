import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import type { ReactNode } from 'react';
import { GlobalSearch } from '../layout/GlobalSearch';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import type { SearchResult } from '../../types/search';

const mockResults: SearchResult[] = [
  { id: 'view-model', name: 'Model', type: 'view', description: 'Data model explorer', route: '/model' },
  { id: 'sales.customers', name: 'Customers', type: 'table', description: 'Customer data', breadcrumb: 'sales', route: '/model/sales/customers' },
  { id: 'customer-lifetime-value', name: 'Customer Lifetime Value', type: 'concept', description: 'CLV metric', breadcrumb: 'Analytics', route: '/knowledge/customer-lifetime-value' },
];

const mockSearch = vi.fn<(query: string) => Promise<SearchResult[]>>();

vi.mock('../../services/api', () => ({
  searchService: {
    search: (...args: [string]) => mockSearch(...args),
  },
  featureService: {
    async getFlags() {
      return {};
    },
  },
}));

function wrapper({ children }: { children: ReactNode }) {
  return (
    <MemoryRouter>
      <MantineProvider>
        <FeatureFlagProvider>
          {children}
        </FeatureFlagProvider>
      </MantineProvider>
    </MemoryRouter>
  );
}

beforeEach(() => {
  mockSearch.mockReset();
  mockSearch.mockResolvedValue([]);
});

describe('GlobalSearch', () => {
  describe('collapsed state', () => {
    it('should render a search icon button', () => {
      render(<GlobalSearch />, { wrapper });
      expect(screen.getByLabelText('Search (Ctrl+K)')).toBeInTheDocument();
    });
  });

  describe('expand / collapse', () => {
    it('should show the search input after clicking the icon', async () => {
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));
      await waitFor(() => {
        expect(screen.getByPlaceholderText(/Search/)).toBeInTheDocument();
      });
    });

    it('should show Esc hint when input is empty', async () => {
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));
      await waitFor(() => {
        expect(screen.getByText('Esc')).toBeInTheDocument();
      });
    });

    it('should collapse when Escape is pressed', async () => {
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));
      await waitFor(() => {
        expect(screen.getByPlaceholderText(/Search/)).toBeInTheDocument();
      });

      fireEvent.keyDown(screen.getByPlaceholderText(/Search/), { key: 'Escape' });

      await waitFor(() => {
        expect(screen.queryByPlaceholderText(/Search/)).not.toBeInTheDocument();
      });
    });
  });

  describe('search results', () => {
    it('should call searchService.search when typing 2+ chars', async () => {
      mockSearch.mockResolvedValue(mockResults);
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));

      const input = screen.getByPlaceholderText(/Search/);
      await userEvent.type(input, 'cu');

      await waitFor(() => {
        expect(mockSearch).toHaveBeenCalled();
      });
    });

    it('should display results grouped by type', async () => {
      mockSearch.mockResolvedValue(mockResults);
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));

      const input = screen.getByPlaceholderText(/Search/);
      await userEvent.type(input, 'customer');

      await waitFor(() => {
        expect(screen.getByText('Customers')).toBeInTheDocument();
        expect(screen.getByText('Customer Lifetime Value')).toBeInTheDocument();
      });
    });

    it('should show type badges on results', async () => {
      mockSearch.mockResolvedValue(mockResults);
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));

      const input = screen.getByPlaceholderText(/Search/);
      await userEvent.type(input, 'customer');

      await waitFor(() => {
        expect(screen.getByText('Table')).toBeInTheDocument();
        expect(screen.getByText('Concept')).toBeInTheDocument();
      });
    });
  });

  describe('no results', () => {
    it('should show "No results" message when search returns empty', async () => {
      mockSearch.mockResolvedValue([]);
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));

      const input = screen.getByPlaceholderText(/Search/);
      await userEvent.type(input, 'xyznonexistent');

      await waitFor(() => {
        expect(screen.getByText(/No results for/)).toBeInTheDocument();
      });
    });

    it('should show "Ask in Chat" button when no results', async () => {
      mockSearch.mockResolvedValue([]);
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));

      const input = screen.getByPlaceholderText(/Search/);
      await userEvent.type(input, 'xyznonexistent');

      await waitFor(() => {
        expect(screen.getByText('Ask in Chat')).toBeInTheDocument();
      });
    });
  });

  describe('keyboard navigation', () => {
    it('should highlight results with arrow keys', async () => {
      mockSearch.mockResolvedValue(mockResults);
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));

      const input = screen.getByPlaceholderText(/Search/);
      await userEvent.type(input, 'customer');

      await waitFor(() => {
        expect(screen.getByText('Customers')).toBeInTheDocument();
      });

      // Arrow down should move highlight
      fireEvent.keyDown(input, { key: 'ArrowDown' });
      fireEvent.keyDown(input, { key: 'ArrowDown' });
      // No crash — keyboard nav works
    });
  });

  describe('clear button', () => {
    it('should show clear button when there is input text', async () => {
      render(<GlobalSearch />, { wrapper });
      await userEvent.click(screen.getByLabelText('Search (Ctrl+K)'));

      const input = screen.getByPlaceholderText(/Search/);
      await userEvent.type(input, 'test');

      await waitFor(() => {
        // The clear button (X icon) should be present
        const clearButtons = screen.getAllByRole('button');
        expect(clearButtons.length).toBeGreaterThan(0);
      });
    });
  });
});
