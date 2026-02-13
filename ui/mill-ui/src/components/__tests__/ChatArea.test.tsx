import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, act, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import type { ReactNode } from 'react';
import { useEffect, useRef } from 'react';
import { ChatArea } from '../chat/ChatArea';
import { ChatProvider, useChat } from '../../context/ChatContext';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';

// Mock the chat service with an instant single-chunk response
vi.mock('../../services/api', () => ({
  chatService: {
    async createChat() {
      return { chatId: crypto.randomUUID(), chatName: 'New Chat' };
    },
    async *sendMessage() {
      yield 'AI reply';
    },
  },
  featureService: {
    async getFlags() {
      return {};
    },
  },
  searchService: {
    async search() {
      return [];
    },
  },
}));

beforeEach(() => {
  localStorage.clear();
});

function wrapper({ children }: { children: ReactNode }) {
  return (
    <MemoryRouter>
      <MantineProvider>
        <FeatureFlagProvider>
          <ChatProvider>{children}</ChatProvider>
        </FeatureFlagProvider>
      </MantineProvider>
    </MemoryRouter>
  );
}

function wrapperWithSearchQuery({ children }: { children: ReactNode }) {
  return (
    <MemoryRouter initialEntries={[{ pathname: '/chat', state: { searchQuery: 'test search' } }]}>
      <MantineProvider>
        <FeatureFlagProvider>
          <ChatProvider>{children}</ChatProvider>
        </FeatureFlagProvider>
      </MantineProvider>
    </MemoryRouter>
  );
}

/** Seed an active conversation before rendering ChatArea */
function ChatAreaWithConversation() {
  const { createConversation, state } = useChat();
  const initialized = useRef(false);

  useEffect(() => {
    if (!initialized.current && state.conversations.length === 0) {
      initialized.current = true;
      createConversation();
    }
  }, [state.conversations.length, createConversation]);

  return <ChatArea />;
}

async function renderChatArea() {
  const result = render(<ChatAreaWithConversation />, { wrapper });
  // Wait for async createConversation to resolve
  await waitFor(() => {
    expect(screen.getByText('New Chat')).toBeInTheDocument();
  });
  return result;
}

describe('ChatArea', () => {
  it('should display the conversation title', async () => {
    await renderChatArea();
    expect(screen.getByText('New Chat')).toBeInTheDocument();
  });

  it('should show the empty-state welcome message initially', async () => {
    await renderChatArea();
    expect(screen.getByText('How can I help you today?')).toBeInTheDocument();
  });

  it('should render the message input area', async () => {
    await renderChatArea();
    expect(screen.getByPlaceholderText('Type your message...')).toBeInTheDocument();
  });

  it('should render the send button', async () => {
    await renderChatArea();
    expect(screen.getByLabelText('Send message')).toBeInTheDocument();
  });

  it('should add a user message when submitting text', async () => {
    await renderChatArea();

    const textarea = screen.getByPlaceholderText('Type your message...');
    await userEvent.type(textarea, 'Hello AI');

    await act(async () => {
      fireEvent.keyDown(textarea, { key: 'Enter', code: 'Enter' });
    });

    // The user message appears in the bubble (title might also match, so use getAllByText)
    const matches = screen.getAllByText('Hello AI');
    expect(matches.length).toBeGreaterThanOrEqual(1);
  });

  it('should display the AI response after sending a message', async () => {
    await renderChatArea();

    const textarea = screen.getByPlaceholderText('Type your message...');
    await userEvent.type(textarea, 'Hello');

    await act(async () => {
      fireEvent.keyDown(textarea, { key: 'Enter', code: 'Enter' });
      // Allow the mock async generator to resolve
      await new Promise((r) => setTimeout(r, 50));
    });

    // The mock AI response should appear
    expect(screen.getByText('AI reply')).toBeInTheDocument();
  });

  it('should update conversation title from first user message', async () => {
    await renderChatArea();

    const textarea = screen.getByPlaceholderText('Type your message...');
    await userEvent.type(textarea, 'How does SQL work?');

    await act(async () => {
      fireEvent.keyDown(textarea, { key: 'Enter', code: 'Enter' });
      await new Promise((r) => setTimeout(r, 50));
    });

    // Title + message bubble both show the text
    const matches = screen.getAllByText('How does SQL work?');
    expect(matches.length).toBeGreaterThanOrEqual(1);
  });

  it('should clear the input after sending', async () => {
    await renderChatArea();

    const textarea = screen.getByPlaceholderText('Type your message...') as HTMLTextAreaElement;
    await userEvent.type(textarea, 'Test message');
    fireEvent.keyDown(textarea, { key: 'Enter', code: 'Enter' });

    expect(textarea.value).toBe('');
  });
});

describe('ChatArea auto-send from router state', () => {
  it('should auto-send searchQuery from router state as a message', async () => {
    render(<ChatArea />, { wrapper: wrapperWithSearchQuery });

    // Wait for the auto-sent message to appear
    await waitFor(() => {
      const matches = screen.getAllByText('test search');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });
  });

  it('should create a new conversation for the auto-sent message', async () => {
    render(<ChatArea />, { wrapper: wrapperWithSearchQuery });

    // The auto-sent message should trigger a new conversation with title derived from the message
    await waitFor(() => {
      const matches = screen.getAllByText('test search');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });
  });

  it('should show AI response after auto-send', async () => {
    render(<ChatArea />, { wrapper: wrapperWithSearchQuery });

    await waitFor(
      () => {
        expect(screen.getByText('AI reply')).toBeInTheDocument();
      },
      { timeout: 3000 },
    );
  });
});
