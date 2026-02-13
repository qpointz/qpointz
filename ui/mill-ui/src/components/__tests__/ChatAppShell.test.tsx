import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import { AppShell } from '../layout/AppShell';
import { ChatProvider } from '../../context/ChatContext';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import { defaultFeatureFlags } from '../../features/defaults';

// Mock both services needed by AppShell
vi.mock('../../services/api', () => ({
  chatService: {
    async createChat() {
      return { chatId: crypto.randomUUID(), chatName: 'New Chat' };
    },
    async *sendMessage() {
      yield 'Mock response';
    },
  },
  featureService: {
    async getFlags() {
      return { ...defaultFeatureFlags };
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

function renderAppShell() {
  return render(
    <MemoryRouter>
      <MantineProvider>
        <FeatureFlagProvider>
          <ChatProvider>
            <AppShell />
          </ChatProvider>
        </FeatureFlagProvider>
      </MantineProvider>
    </MemoryRouter>,
  );
}

describe('Chat AppShell', () => {
  it('should render the "Conversations" sidebar title', () => {
    renderAppShell();
    expect(screen.getByText('Conversations')).toBeInTheDocument();
  });

  it('should start with no conversations', () => {
    renderAppShell();
    // The badge showing conversation count should display "0"
    expect(screen.getByText('0')).toBeInTheDocument();
  });

  it('should display the "New Chat" title in the chat area when no conversation exists', () => {
    renderAppShell();
    // The chat area header falls back to "New Chat" when there's no active conversation
    expect(screen.getByText('New Chat')).toBeInTheDocument();
  });

  it('should render the chat area with the welcome message', () => {
    renderAppShell();
    expect(screen.getByText('How can I help you today?')).toBeInTheDocument();
  });

  it('should render the message input', () => {
    renderAppShell();
    expect(screen.getByPlaceholderText('Type your message...')).toBeInTheDocument();
  });
});
