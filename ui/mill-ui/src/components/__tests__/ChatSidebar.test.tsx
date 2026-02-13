import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import type { ReactNode } from 'react';
import { useEffect, useRef } from 'react';
import { Sidebar } from '../layout/Sidebar';
import { ChatProvider, useChat } from '../../context/ChatContext';

// Mock the chat service
vi.mock('../../services/api', () => ({
  chatService: {
    async createChat() {
      return { chatId: crypto.randomUUID(), chatName: 'New Chat' };
    },
    async *sendMessage() {
      yield 'Mock response';
    },
  },
}));

beforeEach(() => {
  localStorage.clear();
});

function wrapper({ children }: { children: ReactNode }) {
  return (
    <MantineProvider>
      <ChatProvider>{children}</ChatProvider>
    </MantineProvider>
  );
}

/** Helper component to seed conversations before rendering Sidebar */
function SidebarWithConversations({ count = 0 }: { count?: number }) {
  const { createConversation, state } = useChat();
  const initialized = useRef(false);

  useEffect(() => {
    if (!initialized.current && count > 0) {
      initialized.current = true;
      (async () => {
        for (let i = 0; i < count; i++) {
          await createConversation();
        }
      })();
    }
  }, [count, createConversation]);

  return <Sidebar />;
}

async function renderSidebar(conversationCount = 0) {
  const result = render(
    <SidebarWithConversations count={conversationCount} />,
    { wrapper },
  );
  if (conversationCount > 0) {
    // Wait for the async createConversation calls to resolve
    await waitFor(() => {
      const items = screen.getAllByText('New Chat');
      expect(items.length).toBe(conversationCount);
    });
  }
  return result;
}

describe('Chat Sidebar', () => {
  describe('empty state', () => {
    it('should show "No conversations yet" when there are no conversations', () => {
      render(<SidebarWithConversations count={0} />, { wrapper });
      expect(screen.getByText('No conversations yet')).toBeInTheDocument();
    });

    it('should show "Start a new chat to begin" hint', () => {
      render(<SidebarWithConversations count={0} />, { wrapper });
      expect(screen.getByText('Start a new chat to begin')).toBeInTheDocument();
    });
  });

  describe('with conversations', () => {
    it('should render conversation items', async () => {
      await renderSidebar(2);
      const items = screen.getAllByText('New Chat');
      expect(items.length).toBe(2);
    });

    it('should show message count for each conversation', async () => {
      await renderSidebar(1);
      expect(screen.getByText('0 messages')).toBeInTheDocument();
    });

    it('should show delete icon for each conversation', async () => {
      await renderSidebar(1);
      const buttons = screen.getAllByRole('button');
      expect(buttons.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('delete button', () => {
    it('should render a trash/delete button for each conversation', async () => {
      await renderSidebar(1);
      const buttons = screen.getAllByRole('button');
      const trashBtn = buttons.find((b) => b.querySelector('svg'));
      expect(trashBtn).toBeDefined();
    });

    it('should change trash button appearance when clicked (confirm state)', async () => {
      await renderSidebar(1);
      const buttons = screen.getAllByRole('button');
      const trashBtn = buttons.find((b) => b.querySelector('svg'))!;

      // Before click: button is gray-ish (subtle variant, color="gray")
      const styleBefore = trashBtn.getAttribute('style') ?? '';

      fireEvent.click(trashBtn);

      // After click: button color switches to red (confirmDeleteId is set)
      const styleAfter = trashBtn.getAttribute('style') ?? '';
      expect(styleAfter).toContain('red');
      expect(styleBefore).not.toBe(styleAfter);
    });

    it('should render multiple trash buttons for multiple conversations', async () => {
      await renderSidebar(2);
      const navLinkSections = document.querySelectorAll('[data-position="right"]');
      expect(navLinkSections.length).toBe(2);
    });
  });
});
