import { describe, it, expect, vi, afterEach } from 'vitest';
import { buildSendMessageBody, chatService, realChatService } from '../chatService';

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

  describe('analysis copilot mock', () => {
    it('should use analysis-copilot profile for analysis contextual create', async () => {
      const { chatId } = await chatService.createChat({
        contextType: 'analysis',
        contextId: '__analysis__',
        contextLabel: 'Current query',
      });
      const detail = await chatService.getChatDetail(chatId);
      expect(detail.chat.profileId).toBe('analysis-copilot');
    });

    it('should synthesize sql artifact for rewrite prompts', async () => {
      const { chatId } = await chatService.createChat({
        contextType: 'analysis',
        contextId: '__analysis__',
        contextLabel: 'Current query',
      });
      const artifacts: unknown[] = [];
      for await (const _chunk of chatService.sendMessage(chatId, 'rewrite this query', {
        onNonTextPartUpdated: (evt) => artifacts.push(evt),
      })) {
        /* consume stream */
      }
      expect(artifacts.length).toBeGreaterThan(0);
    }, 15000);

    it('should return prose-only advice for optimize prompts without sql artifact', async () => {
      const { chatId } = await chatService.createChat({
        contextType: 'analysis',
        contextId: '__analysis__',
        contextLabel: 'Current query',
      });
      const artifacts: unknown[] = [];
      const chunks: string[] = [];
      for await (const chunk of chatService.sendMessage(chatId, 'optimize this query', {
        onNonTextPartUpdated: (evt) => artifacts.push(evt),
      })) {
        chunks.push(chunk);
      }
      expect(chunks.join('').length).toBeGreaterThan(0);
      expect(artifacts).toHaveLength(0);
    }, 15000);
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

describe('buildSendMessageBody', () => {
  it('should send message only when no context is provided', () => {
    expect(buildSendMessageBody('hello')).toEqual({ message: 'hello' });
  });

  it('should include context.values and optional version on the wire', () => {
    expect(
      buildSendMessageBody('optimize', {
        context: {
          values: {
            'sql.current': 'SELECT 1',
            'custom.unknown.key': 42,
          },
          version: 2,
        },
      }),
    ).toEqual({
      message: 'optimize',
      context: {
        values: {
          'sql.current': 'SELECT 1',
          'custom.unknown.key': 42,
        },
        version: 2,
      },
    });
  });

  it('should omit context when values map is empty', () => {
    expect(buildSendMessageBody('hello', { context: { values: {} } })).toEqual({ message: 'hello' });
  });
});

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

  it('should POST createChat with data-analysis when profileId omitted', async () => {
    const created = {
      chatId: 'c-test',
      userId: 'u1',
      profileId: 'data-analysis',
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

    await realChatService.createChat();

    const init = fetchSpy.mock.calls[0]![1] as RequestInit;
    expect(init.body).toBe(JSON.stringify({ profileId: 'data-analysis' }));
  });

  it('should PATCH updateChatProfile with profileId in JSON body', async () => {
    const updated = {
      chatId: 'c-test',
      userId: 'u1',
      profileId: 'schema-exploration',
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
    const fetchSpy = vi.fn().mockResolvedValue(new Response(JSON.stringify(updated), { status: 200 }));
    vi.stubGlobal('fetch', fetchSpy);

    const result = await realChatService.updateChatProfile('c-test', 'schema-exploration');

    expect(result.profileId).toBe('schema-exploration');
    expect(fetchSpy).toHaveBeenCalledTimes(1);
    const [url, init] = fetchSpy.mock.calls[0]!;
    expect(String(url)).toContain('/api/v1/ai/chats/c-test');
    expect((init as RequestInit).method).toBe('PATCH');
    expect((init as RequestInit).body).toBe(JSON.stringify({ profileId: 'schema-exploration' }));
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

  it('should POST sendMessage with optional context.values in JSON body', async () => {
    const streamBody = sseDataLine({
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

    for await (const _chunk of realChatService.sendMessage('chat-1', 'rewrite', {
      context: {
        values: {
          'sql.current': 'SELECT * FROM orders',
          'future.key': true,
        },
      },
    })) {
      // consume
    }

    expect(fetchSpy).toHaveBeenCalledTimes(1);
    const [, init] = fetchSpy.mock.calls[0]!;
    expect((init as RequestInit).body).toBe(
      JSON.stringify({
        message: 'rewrite',
        context: {
          values: {
            'sql.current': 'SELECT * FROM orders',
            'future.key': true,
          },
        },
      }),
    );
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

  it('should forward N structured parts and multi item.completed hint', async () => {
    const facet1 = JSON.stringify({
      facetTypeKey: 'descriptive',
      metadataEntityId: 'sales.customers',
      serializedPayload: { summary: 'VIP' },
    });
    const facet2 = JSON.stringify({
      facetTypeKey: 'descriptive',
      metadataEntityId: 'sales.orders',
      serializedPayload: { summary: 'Orders' },
    });
    const streamBody =
      sseDataLine({
        type: 'item.part.updated',
        itemId: 'item-1',
        presentation: 'structured',
        partType: 'facet-proposal',
        mode: 'replace',
        content: facet1,
      }) +
      sseDataLine({
        type: 'item.part.updated',
        itemId: 'item-1',
        presentation: 'structured',
        partType: 'facet-proposal',
        mode: 'append',
        content: facet2,
      }) +
      sseDataLine({
        type: 'item.completed',
        itemId: 'item-1',
        presentation: 'structured',
        partType: 'multi',
        structuredPartCount: 2,
        partTypes: ['facet-proposal', 'facet-proposal'],
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

    const structuredParts: Record<string, unknown>[] = [];
    const completed: Array<Record<string, unknown>> = [];
    for await (const _ of realChatService.sendMessage('any', 'x', {
      onNonTextPartUpdated: (p) => structuredParts.push(p),
      onItemCompleted: (p) => completed.push(p as unknown as Record<string, unknown>),
    })) {
      // consume
    }

    expect(structuredParts).toHaveLength(2);
    expect(structuredParts[0]!.mode).toBe('replace');
    expect(structuredParts[1]!.mode).toBe('append');
    expect(completed).toHaveLength(1);
    expect(completed[0]!.partType).toBe('multi');
    expect(completed[0]!.structuredPartCount).toBe(2);
    expect(completed[0]!.partTypes).toEqual(['facet-proposal', 'facet-proposal']);
  });
});
