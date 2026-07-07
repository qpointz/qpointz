import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MessageList } from '../chat/MessageList';
import type { Message } from '../../types/chat';

function renderList(props: { messages?: Message[]; isLoading?: boolean; thinkingMessage?: string | null } = {}) {
  return render(
    <MantineProvider>
      <MessageList
        messages={props.messages ?? []}
        isLoading={props.isLoading ?? false}
        thinkingMessage={props.thinkingMessage ?? null}
      />
    </MantineProvider>,
  );
}

function makeMsg(overrides: Partial<Message> & { id: string; role: Message['role'] }): Message {
  return {
    conversationId: 'conv-1',
    content: 'Test content',
    timestamp: Date.now(),
    ...overrides,
  };
}

describe('MessageList', () => {
  describe('empty state', () => {
    it('should show welcome message when there are no messages', () => {
      renderList();
      expect(screen.getByText('How can I help you today?')).toBeInTheDocument();
    });

    it('should show helper text about starting a conversation', () => {
      renderList();
      expect(screen.getByText(/Ask questions about your data/)).toBeInTheDocument();
    });
  });

  describe('with messages', () => {
    it('should render user messages', () => {
      const messages: Message[] = [
        makeMsg({ id: '1', role: 'user', content: 'Hello AI!' }),
      ];
      renderList({ messages });
      expect(screen.getByText('Hello AI!')).toBeInTheDocument();
    });

    it('should render assistant messages', () => {
      const messages: Message[] = [
        makeMsg({ id: '1', role: 'assistant', content: 'Hello human!' }),
      ];
      renderList({ messages });
      expect(screen.getByText('Hello human!')).toBeInTheDocument();
    });

    it('should render multiple messages in order', () => {
      const messages: Message[] = [
        makeMsg({ id: '1', role: 'user', content: 'First message' }),
        makeMsg({ id: '2', role: 'assistant', content: 'Second message' }),
        makeMsg({ id: '3', role: 'user', content: 'Third message' }),
      ];
      renderList({ messages });
      expect(screen.getByText('First message')).toBeInTheDocument();
      expect(screen.getByText('Second message')).toBeInTheDocument();
      expect(screen.getByText('Third message')).toBeInTheDocument();
    });

    it('should not show the empty state when messages exist', () => {
      const messages: Message[] = [
        makeMsg({ id: '1', role: 'user', content: 'Hi' }),
      ];
      renderList({ messages });
      expect(screen.queryByText('How can I help you today?')).not.toBeInTheDocument();
    });
  });

  describe('assistant feedback', () => {
    it('should show in-thread feedback when loading and last assistant message is empty', () => {
      const messages: Message[] = [
        makeMsg({ id: '1', role: 'user', content: 'Hello' }),
        makeMsg({ id: '2', role: 'assistant', content: '' }),
      ];
      const { container } = renderList({ messages, isLoading: true, thinkingMessage: 'Running validate_sql…' });
      const dots = container.querySelectorAll('.typing-dot');
      expect(dots.length).toBe(3);
      expect(screen.getByText('Running validate_sql…')).toBeInTheDocument();
    });

    it('should NOT show feedback when not loading', () => {
      const messages: Message[] = [
        makeMsg({ id: '1', role: 'user', content: 'Hello' }),
        makeMsg({ id: '2', role: 'assistant', content: '' }),
      ];
      const { container } = renderList({ messages, isLoading: false });
      const dots = container.querySelectorAll('.typing-dot');
      expect(dots.length).toBe(0);
    });

    it('should NOT show feedback when last message has content', () => {
      const messages: Message[] = [
        makeMsg({ id: '1', role: 'user', content: 'Hello' }),
        makeMsg({ id: '2', role: 'assistant', content: 'Responding...' }),
      ];
      const { container } = renderList({ messages, isLoading: true });
      const dots = container.querySelectorAll('.typing-dot');
      expect(dots.length).toBe(0);
    });
  });
});
