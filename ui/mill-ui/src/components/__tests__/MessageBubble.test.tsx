import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MessageBubble } from '../chat/MessageBubble';
import type { Message } from '../../types/chat';

function renderBubble(msg: Partial<Message> & { role: Message['role'] }) {
  const message: Message = {
    id: 'msg-1',
    conversationId: 'conv-1',
    content: msg.content ?? '',
    role: msg.role,
    timestamp: Date.now(),
    ...msg,
  };
  return render(
    <MantineProvider>
      <MessageBubble message={message} />
    </MantineProvider>,
  );
}

describe('MessageBubble', () => {
  describe('user messages', () => {
    it('should render user message content as plain text', () => {
      renderBubble({ role: 'user', content: 'Hello world' });
      expect(screen.getByText('Hello world')).toBeInTheDocument();
    });

    it('should preserve whitespace in user messages', () => {
      renderBubble({ role: 'user', content: 'Line 1\nLine 2' });
      const el = screen.getByText(/Line 1/);
      expect(el).toHaveStyle({ whiteSpace: 'pre-wrap' });
    });
  });

  describe('assistant messages', () => {
    it('should render plain text in assistant messages', () => {
      renderBubble({ role: 'assistant', content: 'I can help with that!' });
      expect(screen.getByText('I can help with that!')).toBeInTheDocument();
    });

    it('should render markdown bold text', () => {
      renderBubble({ role: 'assistant', content: 'This is **bold** text' });
      const bold = screen.getByText('bold');
      expect(bold.tagName.toLowerCase()).toBe('strong');
    });

    it('should render markdown lists', () => {
      const { container } = renderBubble({
        role: 'assistant',
        content: '- Item A\n- Item B\n- Item C',
      });
      const listItems = container.querySelectorAll('li');
      expect(listItems.length).toBe(3);
    });

    it('should render markdown headings', () => {
      renderBubble({ role: 'assistant', content: '## Section Title' });
      expect(screen.getByText('Section Title')).toBeInTheDocument();
    });

    it('should render inline code', () => {
      renderBubble({ role: 'assistant', content: 'Use `console.log()` for debugging' });
      const code = screen.getByText('console.log()');
      expect(code.tagName.toLowerCase()).toBe('code');
    });

    it('should render ordered lists', () => {
      const { container } = renderBubble({
        role: 'assistant',
        content: '1. First\n2. Second\n3. Third',
      });
      const listItems = container.querySelectorAll('li');
      expect(listItems.length).toBe(3);
      const ol = container.querySelector('ol');
      expect(ol).not.toBeNull();
    });
  });

  describe('role distinction', () => {
    it('should render user messages inside a plain Text (not markdown)', () => {
      const { container } = renderBubble({ role: 'user', content: '**not bold**' });
      // User messages use Text with pre-wrap, not ReactMarkdown
      const el = screen.getByText('**not bold**');
      expect(el).toBeInTheDocument();
      // Should NOT be parsed as bold
      expect(container.querySelector('strong')).toBeNull();
    });

    it('should render assistant messages through markdown processor', () => {
      const { container } = renderBubble({ role: 'assistant', content: '**bold text**' });
      const strong = container.querySelector('strong');
      expect(strong).not.toBeNull();
      expect(strong!.textContent).toBe('bold text');
    });
  });
});
