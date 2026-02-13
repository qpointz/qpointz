import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MantineProvider } from '@mantine/core';
import { MessageInput } from '../chat/MessageInput';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';

// Mock feature service so FeatureFlagProvider doesn't call real backend
vi.mock('../../services/api', () => ({
  featureService: {
    async getFlags() {
      const { defaultFeatureFlags } = await import('../../features/defaults');
      return { ...defaultFeatureFlags };
    },
  },
}));

function renderInput(props: { onSend?: (msg: string) => void; disabled?: boolean } = {}) {
  const onSend = props.onSend ?? vi.fn();
  return {
    onSend,
    ...render(
      <MantineProvider>
        <FeatureFlagProvider>
          <MessageInput onSend={onSend} disabled={props.disabled} />
        </FeatureFlagProvider>
      </MantineProvider>,
    ),
  };
}

describe('MessageInput', () => {
  it('should render a textarea with placeholder', () => {
    renderInput();
    expect(screen.getByPlaceholderText('Type your message...')).toBeInTheDocument();
  });

  it('should render the send button', () => {
    renderInput();
    expect(screen.getByLabelText('Send message')).toBeInTheDocument();
  });

  it('should render attach and dictate buttons (feature-flagged)', () => {
    renderInput();
    expect(screen.getByLabelText('Attach file')).toBeInTheDocument();
    expect(screen.getByLabelText('Dictate')).toBeInTheDocument();
  });

  it('should disable send button when textarea is empty', () => {
    renderInput();
    const sendBtn = screen.getByLabelText('Send message');
    expect(sendBtn).toHaveAttribute('disabled');
  });

  it('should call onSend when the send button is clicked with text', async () => {
    const onSend = vi.fn();
    renderInput({ onSend });

    const textarea = screen.getByPlaceholderText('Type your message...');
    await userEvent.type(textarea, 'Hello world');

    const sendBtn = screen.getByLabelText('Send message');
    await userEvent.click(sendBtn);

    expect(onSend).toHaveBeenCalledWith('Hello world');
  });

  it('should send message on Enter key press', async () => {
    const onSend = vi.fn();
    renderInput({ onSend });

    const textarea = screen.getByPlaceholderText('Type your message...');
    await userEvent.type(textarea, 'Test message');
    fireEvent.keyDown(textarea, { key: 'Enter', code: 'Enter' });

    expect(onSend).toHaveBeenCalledWith('Test message');
  });

  it('should NOT send message on Shift+Enter (allows new line)', async () => {
    const onSend = vi.fn();
    renderInput({ onSend });

    const textarea = screen.getByPlaceholderText('Type your message...');
    await userEvent.type(textarea, 'Line one');
    fireEvent.keyDown(textarea, { key: 'Enter', code: 'Enter', shiftKey: true });

    expect(onSend).not.toHaveBeenCalled();
  });

  it('should clear the textarea after sending', async () => {
    const onSend = vi.fn();
    renderInput({ onSend });

    const textarea = screen.getByPlaceholderText('Type your message...') as HTMLTextAreaElement;
    await userEvent.type(textarea, 'Message to send');
    fireEvent.keyDown(textarea, { key: 'Enter', code: 'Enter' });

    expect(textarea.value).toBe('');
  });

  it('should not send empty or whitespace-only messages', async () => {
    const onSend = vi.fn();
    renderInput({ onSend });

    const textarea = screen.getByPlaceholderText('Type your message...');
    await userEvent.type(textarea, '   ');
    fireEvent.keyDown(textarea, { key: 'Enter', code: 'Enter' });

    expect(onSend).not.toHaveBeenCalled();
  });

  it('should be disabled when disabled prop is true', () => {
    renderInput({ disabled: true });
    const textarea = screen.getByPlaceholderText('Type your message...');
    expect(textarea).toBeDisabled();
  });
});
