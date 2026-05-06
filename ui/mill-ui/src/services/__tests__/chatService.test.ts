import { describe, it, expect, vi, afterEach } from 'vitest';
import { chatService, realChatService } from '../chatService';

describe('chatService', () => {
  describe('createChat', () => {
    it('should return chatId and chatName', async () => {
      const result = await chatService.createChat();
      expect(result.chatId).toBeDefined();
      expect(typeof result.chatId).toBe('string');
      expect(result.chatName).toBeDefined();
      expect(typeof result.chatName).toBe('string');
    });

    it('should return unique chatIds on successive calls', async () => {
      const r1 = await chatService.createChat();
      const r2 = await chatService.createChat();
      expect(r1.chatId).not.toBe(r2.chatId);
    });

    it('should use contextLabel in chatName when provided', async () => {
      const result = await chatService.createChat({
        contextType: 'model',
        contextId: 'sales.customers',
        contextLabel: 'Customers table',
      });
      expect(result.chatName).toContain('Customers table');
    });
  });

  describe('sendMessage', () => {
    it('should yield at least one chunk', async () => {
      const { chatId } = await chatService.createChat();
      const chunks: string[] = [];
      for await (const chunk of chatService.sendMessage(chatId, 'Hello')) {
        chunks.push(chunk);
      }
      expect(chunks.length).toBeGreaterThan(0);
    }, 15000);

    it('chunks should be strings', async () => {
      const { chatId } = await chatService.createChat();
      for await (const chunk of chatService.sendMessage(chatId, 'Hello')) {
        expect(typeof chunk).toBe('string');
      }
    }, 15000);
  });

  describe('listChats', () => {
    it('should return an array', async () => {
      const chats = await chatService.listChats();
      expect(Array.isArray(chats)).toBe(true);
    });

    it('should include general chats (created without context)', async () => {
      // Create a general chat
      const { chatId } = await chatService.createChat();
      const chats = await chatService.listChats();
      expect(chats.some((c) => c.chatId === chatId)).toBe(true);
    });

    it('each chat summary should have chatId, chatName, updatedAt', async () => {
      await chatService.createChat();
      const chats = await chatService.listChats();
      for (const c of chats) {
        expect(c.chatId).toBeDefined();
        expect(c.chatName).toBeDefined();
        expect(c.updatedAt).toBeDefined();
      }
    });
  });

  describe('getChatByContext', () => {
    it('should return null when no chat exists for the context', async () => {
      const result = await chatService.getChatByContext('model', 'nonexistent');
      expect(result).toBeNull();
    });

    it('should return chatId for a context that has a chat', async () => {
      const { chatId } = await chatService.createChat({
        contextType: 'model',
        contextId: 'test-entity',
        contextLabel: 'Test',
      });
      const found = await chatService.getChatByContext('model', 'test-entity');
      expect(found).toBe(chatId);
    });
  });

  describe('listAgentProfiles', () => {
    it('should return at least two profile ids for offline picker tests', async () => {
      const profiles = await chatService.listAgentProfiles();
      expect(profiles.length).toBeGreaterThanOrEqual(2);
      const ids = profiles.map((p) => p.id);
      expect(new Set(ids).size).toBe(ids.length);
      for (const p of profiles) {
        expect(Array.isArray(p.capabilityIds)).toBe(true);
      }
    });
  });

  describe('sendMessage progress', () => {
    it('should emit diagnostic then clear-wait before first text chunk', async () => {
      const { chatId } = await chatService.createChat();
      const events: string[] = [];
      for await (const chunk of chatService.sendMessage(chatId, 'Hello', {
        onProgress: (e) => {
          events.push(e.kind);
        },
      })) {
        if (events.includes('clear-wait')) {
          expect(typeof chunk).toBe('string');
          break;
        }
      }
      expect(events[0]).toBe('diagnostic');
      expect(events).toContain('clear-wait');
    }, 15000);
  });
});

function sseDataLine(obj: Record<string, unknown>): string {
  return `data: ${JSON.stringify(obj)}\n\n`;
}

function streamFromString(s: string): ReadableStream<Uint8Array> {
  return new ReadableStream({
    start(controller) {
      controller.enqueue(new TextEncoder().encode(s));
      controller.close();
    },
  });
}

describe('realChatService (fetch-mocked)', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('should POST createChat with profileId in JSON body when provided', async () => {
    const created = {
      chatId: 'c-test',
      userId: 'u1',
      profileId: 'p1',
      chatName: 'N',
      chatType: 'general',
      isFavorite: false,
      contextType: null,
      contextId: null,
      contextLabel: null,
      contextEntityType: null,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    const fetchSpy = vi.fn().mockResolvedValue(new Response(JSON.stringify(created), { status: 200 }));
    vi.stubGlobal('fetch', fetchSpy);

    await realChatService.createChat({ profileId: 'hello-world' });

    expect(fetchSpy).toHaveBeenCalledTimes(1);
    const tuple = fetchSpy.mock.calls[0];
    expect(tuple !== undefined).toBe(true);
    const init = tuple![1] as RequestInit;
    expect(init.method).toBe('POST');
    expect(init.body).toBe(JSON.stringify({ profileId: 'hello-world' }));
  });

  it('should parse SSE diagnostics, text deltas, and completed without new type strings', async () => {
    const streamBody =
      sseDataLine({ type: 'item.diagnostic', code: 'agent.thinking', message: 'Working…' }) +
      sseDataLine({
        type: 'item.part.updated',
        itemId: 'item-1',
        presentation: 'conversation',
        partType: 'text',
        mode: 'append',
        content: 'Hi',
      }) +
      sseDataLine({
        type: 'item.completed',
        itemId: 'item-1',
        presentation: 'conversation',
        partType: 'text',
        content: null,
      });

    const fetchSpy = vi.fn().mockResolvedValue(
      new Response(streamFromString(streamBody), {
        status: 200,
        headers: { 'Content-Type': 'text/event-stream' },
      }),
    );
    vi.stubGlobal('fetch', fetchSpy);

    const progress: string[] = [];
    const chunks: string[] = [];
    for await (const chunk of realChatService.sendMessage('any', 'x', {
      onProgress: (e) => progress.push(e.kind),
    })) {
      chunks.push(chunk);
    }

    expect(progress).toContain('diagnostic');
    expect(progress).toContain('clear-wait');
    expect(chunks.join('')).toContain('Hi');
  });

  it('should invoke onNonTextPartUpdated for non-V1 part then continue text stream', async () => {
    const structured = {
      type: 'item.part.updated',
      itemId: 'art-1',
      presentation: 'structured',
      partType: 'sql',
      mode: 'append',
      content: 'SELECT 1',
    };
    const textDelta = {
      type: 'item.part.updated',
      itemId: 'item-1',
      presentation: 'conversation',
      partType: 'text',
      mode: 'append',
      content: 'Done.',
    };
    const streamBody =
      sseDataLine(structured) +
      sseDataLine(textDelta) +
      sseDataLine({
        type: 'item.completed',
        itemId: 'item-1',
        presentation: 'conversation',
        partType: 'text',
        content: null,
      });

    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(streamFromString(streamBody), {
          status: 200,
          headers: { 'Content-Type': 'text/event-stream' },
        }),
      ),
    );

    const forwarded: Record<string, unknown>[] = [];
    const chunks: string[] = [];
    for await (const chunk of realChatService.sendMessage('any', 'x', {
      onNonTextPartUpdated: (p) => forwarded.push(p),
    })) {
      chunks.push(chunk);
    }

    expect(forwarded).toHaveLength(1);
    const nonText = forwarded[0];
    expect(nonText).toBeDefined();
    expect(nonText!.partType).toBe('sql');
    expect(chunks.join('')).toBe('Done.');
  });

  it('should not stream mislabeled structured JSON into the V1 text accumulator', async () => {
    const streamBody =
      sseDataLine({
        type: 'item.part.updated',
        itemId: 'item-1',
        mode: 'replace',
        content: JSON.stringify({ sql: 'SELECT 1', dialectId: 'calcite' }),
      }) +
      sseDataLine({
        type: 'item.completed',
        itemId: 'item-1',
        presentation: 'conversation',
        partType: 'text',
        content: null,
      });

    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(streamFromString(streamBody), {
          status: 200,
          headers: { 'Content-Type': 'text/event-stream' },
        }),
      ),
    );

    const forwarded: Record<string, unknown>[] = [];
    const chunks: string[] = [];
    for await (const chunk of realChatService.sendMessage('any', 'x', {
      onNonTextPartUpdated: (p) => forwarded.push(p),
    })) {
      chunks.push(chunk);
    }

    expect(forwarded).toHaveLength(1);
    expect(chunks.join('')).toBe('');
  });

  it('should invoke onItemCompleted with presentation and partType from item.completed', async () => {
    const streamBody =
      sseDataLine({
        type: 'item.part.updated',
        itemId: 'item-1',
        presentation: 'conversation',
        partType: 'text',
        mode: 'append',
        content: 'Hi',
      }) +
      sseDataLine({
        type: 'item.completed',
        itemId: 'item-1',
        presentation: 'structured',
        partType: 'sql',
        content: null,
      });

    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(streamFromString(streamBody), {
          status: 200,
          headers: { 'Content-Type': 'text/event-stream' },
        }),
      ),
    );

    const completed: Array<Record<string, unknown>> = [];
    for await (const _ of realChatService.sendMessage('any', 'x', {
      onItemCompleted: (p) => completed.push(p as unknown as Record<string, unknown>),
    })) {
      // consume
    }

    expect(completed).toHaveLength(1);
    expect(completed[0]!.presentation).toBe('structured');
    expect(completed[0]!.partType).toBe('sql');
  });
});
