import type {
  AgentProfileResponseWire,
  ChatDetailResponseWire,
  ChatResponseWire,
  ChatSendOptions,
  ChatService,
  ChatSummary,
  CreateChatParams,
} from '../types/chat';
import { resolveGeneralChatAgentProfileId } from '../features/chatPreferences';
import { isV1MainConversationTextPart } from '../types/chatTransport';
import { sleep, streamResponse } from '../utils/streamUtils';

const CHATS_PREFIX = '/api/v1/ai/chats';
const PROFILES_PREFIX = '/api/v1/ai/profiles';

const FETCH_AI_JSON: RequestInit = {
  credentials: 'include',
  headers: { Accept: 'application/json' },
};

const FETCH_AI_SSE: RequestInit = {
  credentials: 'include',
  headers: {
    Accept: 'text/event-stream',
    'Content-Type': 'application/json',
  },
};

// --- Mock response pools (unchanged corpus) ---

const generalResponses = [
  `I'd be happy to help you with that! Here's a quick example:

\`\`\`typescript
function greet(name: string): string {
  return \`Hello, \${name}!\`;
}

console.log(greet("World"));
\`\`\`

This function takes a name as input and returns a greeting string. Let me know if you need any modifications!`,

  `That's a great question! Let me break it down for you:

1. **First**, you'll want to understand the core concept
2. **Second**, practice with small examples
3. **Third**, build something real

Would you like me to elaborate on any of these points?`,

  `Here's the solution you're looking for:

\`\`\`python
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

# Generate first 10 Fibonacci numbers
for i in range(10):
    print(fibonacci(i))
\`\`\`

This recursive implementation is elegant but not the most efficient for large numbers. Want me to show you an optimized version?`,

  `I can explain that concept! 

**Key Points:**
- The main idea is to break complex problems into smaller pieces
- Each piece should be independently testable
- Combine the pieces to form the complete solution

Here's a practical example in JavaScript:

\`\`\`javascript
// Instead of one big function
const processData = (data) => {
  const validated = validate(data);
  const transformed = transform(validated);
  const formatted = format(transformed);
  return formatted;
};
\`\`\`

This pattern makes your code more maintainable and easier to debug.`,

  `Absolutely! Here's what I recommend:

| Approach | Pros | Cons |
|----------|------|------|
| Option A | Fast, Simple | Limited flexibility |
| Option B | Flexible | More complex |
| Option C | Best of both | Requires setup |

Based on your use case, I'd suggest starting with **Option B** as it gives you room to grow.`,
];

const modelResponses = [
  `Looking at this column's metadata, here are some observations:

- **Data distribution**: Values appear well-distributed with no significant skew
- **Null rate**: Less than 0.5% null values across the dataset
- **Cardinality**: High cardinality suggests this column is a good candidate for indexing

Would you like me to suggest any constraints or relationships?`,

  `Based on the schema structure, I'd recommend the following improvements:

1. **Add a foreign key** relationship to the reference table
2. **Consider adding a check constraint** for data validation
3. **Index this column** if it's frequently used in WHERE clauses

\`\`\`sql
ALTER TABLE customers
  ADD CONSTRAINT chk_segment
  CHECK (segment IN ('Enterprise', 'SMB', 'Startup'));
\`\`\``,

  `This column's data profile shows:

| Metric | Value |
|--------|-------|
| Distinct values | 1,247 |
| Null % | 0.3% |
| Min length | 3 |
| Max length | 64 |
| Most common | "Enterprise" (34%) |

The data looks clean overall. Consider adding a **display name** and **business description** to improve discoverability.`,

  `The relationship pattern here suggests a **one-to-many** cardinality. Each parent record maps to approximately 4.2 child records on average.

**Referential integrity**: 100% -- no orphaned records found.

You might want to document:
- The **business meaning** of this relationship
- **Cascade behavior** on delete
- Whether this is a **hard or soft** dependency`,
];

const knowledgeResponses = [
  `This business concept is well-defined. Here's how it connects to the data model:

- **Primary source**: Derived from the \`orders\` and \`customers\` tables
- **Calculation frequency**: Typically refreshed daily
- **Dependencies**: Requires \`total_amount\` and \`order_date\` fields

Would you like me to suggest related concepts that could extend this definition?`,

  `I can help refine this concept. Consider these improvements:

1. **Add temporal boundaries** -- specify the time window (e.g., "last 12 months")
2. **Clarify edge cases** -- what happens with cancelled orders?
3. **Add synonyms** -- other teams may call this differently

\`\`\`sql
-- Suggested refined definition
SELECT customer_id,
       SUM(total_amount) AS lifetime_value
FROM orders
WHERE status != 'cancelled'
  AND order_date >= DATEADD(month, -12, CURRENT_DATE)
GROUP BY customer_id
\`\`\``,

  `Related concepts you might want to capture:

| Concept | Relationship | Status |
|---------|-------------|--------|
| Customer Segments | Parent grouping | Documented |
| Churn Risk Score | Derived metric | Missing |
| Average Order Value | Component metric | Documented |
| Purchase Frequency | Component metric | Missing |

I'd suggest starting with **Churn Risk Score** as it directly extends this concept.`,

  `The current SQL definition looks correct, but I notice a few opportunities:

- **Performance**: Consider materializing this as a view or summary table
- **Consistency**: The \`source\` tag should be updated to "INFERRED" since it's calculated
- **Documentation**: Adding the **business owner** and **update frequency** would help downstream consumers

Would you like me to draft an improved version?`,
];

const analysisResponses = [
  `Looking at your query, here are a few optimization suggestions:

1. **Add an index** on the JOIN columns if not already indexed
2. **Filter early** — move WHERE conditions closer to the base tables
3. **Avoid SELECT \\*** — specify only the columns you need

\`\`\`sql
-- Consider adding this index
CREATE INDEX idx_orders_customer_id
  ON sales.orders (customer_id);
\`\`\`

This should improve execution time significantly for large datasets.`,

  `The results look interesting! Here's what stands out:

- **Top segment**: Enterprise customers generate the highest per-order revenue
- **Distribution**: The data shows a classic Pareto pattern — ~20% of customers drive ~80% of revenue
- **Outliers**: Check for any unusually high values that might skew averages

Would you like me to suggest a follow-up query to dig deeper into any of these patterns?`,

  `I can help you extend this query. Here's a version with additional insights:

\`\`\`sql
SELECT
  c.segment,
  COUNT(DISTINCT c.customer_id) AS customers,
  COUNT(o.order_id) AS orders,
  SUM(o.total_amount) AS revenue,
  AVG(o.total_amount) AS avg_order,
  SUM(o.total_amount) / COUNT(DISTINCT c.customer_id) AS revenue_per_customer
FROM sales.customers c
JOIN sales.orders o ON c.customer_id = o.customer_id
GROUP BY c.segment
ORDER BY revenue DESC
\`\`\`

The added \`revenue_per_customer\` metric helps compare segment efficiency.`,

  `Here's a breakdown of what this query does:

| Clause | Purpose |
|--------|---------|
| **SELECT** | Aggregates revenue and order counts |
| **JOIN** | Links customers to their orders |
| **GROUP BY** | Groups results by the chosen dimension |
| **ORDER BY** | Sorts by the primary metric descending |
| **LIMIT** | Caps output for performance |

The query is well-structured. Consider adding a **HAVING** clause if you want to filter groups (e.g., only segments with > 100 orders).`,
];

// --- Mock state ---

const chatContextMap = new Map<string, string>();
const contextToChatMap = new Map<string, string>();
const generalChatList: ChatSummary[] = [];
/** Full wire rows for mocks that back `getChatDetail` / PATCH / DELETE parity. */
const mockWireById = new Map<string, ChatResponseWire>();

function resolveProfileForMockCreate(params?: CreateChatParams): string {
  return params?.profileId ?? resolveGeneralChatAgentProfileId();
}

function pickRandom(pool: string[]): string {
  return pool[Math.floor(Math.random() * pool.length)] ?? '';
}

function getResponsePool(chatId: string): string[] {
  const ctx = chatContextMap.get(chatId);
  if (ctx === 'model') return modelResponses;
  if (ctx === 'analysis') return analysisResponses;
  if (ctx === 'knowledge') return knowledgeResponses;
  return generalResponses;
}

/**
 * Vitest (`MODE=test`) stays on mocks; browsers default to REST unless `VITE_CHAT_API=mock`.
 * Exported for contexts that must diverge persistence (REST vs mock) without importing private toggles.
 */
export function isRestChatBackendActive(): boolean {
  if (import.meta.env.MODE === 'test') {
    return false;
  }
  return import.meta.env.VITE_CHAT_API?.toLowerCase() !== 'mock';
}

async function ensureOk(response: Response, label: string): Promise<void> {
  if (response.ok) return;
  const body = await response.text().catch(() => '');
  throw new Error(`${label} failed (${response.status}): ${body}`.trim());
}

function summarizeFromWire(chat: ChatResponseWire): ChatSummary {
  return {
    chatId: chat.chatId,
    chatName: chat.chatName,
    updatedAt: Date.parse(chat.updatedAt),
  };
}

function buildMockWire(chatId: string, params: CreateChatParams | undefined, chatName: string): ChatResponseWire {
  const nowIso = new Date().toISOString();
  const contextual = Boolean(params?.contextType && params?.contextId);
  return {
    chatId,
    userId: 'mock-user',
    profileId: resolveProfileForMockCreate(params),
    chatName,
    chatType: contextual ? 'contextual' : 'general',
    isFavorite: false,
    contextType: params?.contextType ?? null,
    contextId: params?.contextId ?? null,
    contextLabel: params?.contextLabel ?? null,
    contextEntityType: params?.contextEntityType ?? null,
    createdAt: nowIso,
    updatedAt: nowIso,
  };
}

function buildCreateBody(params?: CreateChatParams): Record<string, unknown> | undefined {
  const body: Record<string, unknown> = {};
  const profile = params?.profileId ?? resolveGeneralChatAgentProfileId();
  body.profileId = profile;
  if (params?.contextType) {
    body.contextType = params.contextType;
  }
  if (params?.contextId) {
    body.contextId = params.contextId;
  }
  if (params?.contextLabel !== undefined) {
    body.contextLabel = params.contextLabel;
  }
  if (params?.contextEntityType !== undefined) {
    body.contextEntityType = params.contextEntityType;
  }
  return Object.keys(body).length ? body : undefined;
}

async function readJson<T>(response: Response): Promise<T> {
  return (await response.json()) as T;
}

/**
 * Streams `data:` JSON payloads from an SSE byte stream (Spring `ServerSentEvent` framing).
 *
 * Forward-tolerant: blocks without `data:` are skipped; malformed JSON throws (transport fault).
 */
async function* sseDataRecords(body: ReadableStream<Uint8Array>): AsyncGenerator<Record<string, unknown>> {
  const reader = body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  try {
    for (;;) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      const parts = buffer.split('\n\n');
      buffer = parts.pop() ?? '';
      for (const block of parts) {
        const lines = block.split('\n');
        const dataParts: string[] = [];
        for (const line of lines) {
          if (line.startsWith('data:')) {
            dataParts.push(line.slice(5).trimStart());
          }
        }
        if (dataParts.length === 0) continue;
        const jsonText = dataParts.join('\n');
        yield JSON.parse(jsonText) as Record<string, unknown>;
      }
    }
  } finally {
    reader.releaseLock();
  }
}

async function* consumeChatSse(
  response: Response,
  options: ChatSendOptions | undefined,
): AsyncGenerator<string, void, unknown> {
  const body = response.body;
  if (!body) {
    throw new Error('Chat SSE response has empty body');
  }

  let hadTextDeltaForPrimary = false;
  const accumulatedByItem = new Map<string, string>();
  let primaryItemId: string | null = null;
  let lastToolEmitMs = 0;

  const emitToolLine = (line: string) => {
    const now = Date.now();
    if (now - lastToolEmitMs < 60) return;
    lastToolEmitMs = now;
    options?.onProgress?.({ kind: 'tool', line });
  };

  const clearWait = () => {
    options?.onProgress?.({ kind: 'clear-wait' });
  };

  for await (const evt of sseDataRecords(body)) {
    const eventType = typeof evt.type === 'string' ? evt.type : '';

    switch (eventType) {
      case 'item.created':
        break;
      case 'item.diagnostic': {
        options?.onProgress?.({
          kind: 'diagnostic',
          code: typeof evt.code === 'string' ? evt.code : '',
          message: typeof evt.message === 'string' ? evt.message : '',
        });
        break;
      }
      case 'item.tool.call': {
        const name =
          typeof evt.toolName === 'string' ? evt.toolName : '?';
        emitToolLine(`Tool: ${name}`);
        break;
      }
      case 'item.tool.result': {
        const name =
          typeof evt.toolName === 'string' ? evt.toolName : '?';
        emitToolLine(`Tool done: ${name}`);
        break;
      }
      case 'item.part.updated': {
        if (!isV1MainConversationTextPart(evt)) {
          options?.onNonTextPartUpdated?.(evt);
          break;
        }

        const itemId =
          typeof evt.itemId === 'string' ? evt.itemId : '';
        const mode = typeof evt.mode === 'string' ? evt.mode : 'append';
        const content =
          typeof evt.content === 'string' ? evt.content : '';

        if (!primaryItemId) {
          primaryItemId = itemId;
        }

        const prior = accumulatedByItem.get(itemId) ?? '';
        accumulatedByItem.set(
          itemId,
          mode === 'replace' ? content : prior + content,
        );

        if (!hadTextDeltaForPrimary && itemId === primaryItemId && content.length > 0) {
          hadTextDeltaForPrimary = true;
          clearWait();
        }

        if (itemId === primaryItemId && content.length > 0) {
          yield content;
        }

        break;
      }
      case 'item.completed': {
        clearWait();
        const itemId = typeof evt.itemId === 'string' ? evt.itemId : '';
        const record = evt as Record<string, unknown>;
        const presentation =
          typeof record.presentation === 'string' ? record.presentation : 'conversation';
        const partType =
          typeof record.partType === 'string'
            ? record.partType
            : typeof record.part_type === 'string'
              ? record.part_type
              : 'text';

        let contentFull: string | null = null;
        if ('content' in evt && evt.content != null) {
          contentFull = String(evt.content);
        }

        options?.onItemCompleted?.({
          itemId,
          presentation,
          partType,
          content: contentFull,
        });

        if (!hadTextDeltaForPrimary && contentFull !== null && (!primaryItemId || itemId === primaryItemId)) {
          yield contentFull;
        }
        break;
      }
      case 'item.failed': {
        clearWait();
        const code = typeof evt.code === 'string' ? evt.code : 'error';
        const reason = typeof evt.reason === 'string' ? evt.reason : '';
        yield `\n\n**Error** (${code}): ${reason}`;
        break;
      }
      default:
        break;
    }
  }
}

// --- Implementations ---

const mockChatService: ChatService = {
  async createChat(params) {
    await sleep(100);
    const chatId = crypto.randomUUID();
    const chatName = params?.contextLabel ? `Chat: ${params.contextLabel}` : 'New Chat';
    const wire = buildMockWire(chatId, params, chatName);
    mockWireById.set(chatId, wire);

    if (params?.contextType && params?.contextId) {
      chatContextMap.set(chatId, params.contextType);
      contextToChatMap.set(`${params.contextType}:${params.contextId}`, chatId);
    } else {
      generalChatList.unshift(summarizeFromWire(wire));
    }

    return { chatId, chatName };
  },

  async *sendMessage(chatId, _message, options) {
    options?.onProgress?.({
      kind: 'diagnostic',
      code: 'mock.preparing',
      message: 'Preparing reply…',
    });
    const pool = getResponsePool(chatId);
    const response = pickRandom(pool);
    if (!response) return;
    let firstChunk = true;
    for await (const chunk of streamResponse(response)) {
      if (firstChunk) {
        firstChunk = false;
        options?.onProgress?.({ kind: 'clear-wait' });
      }
      yield chunk;
    }
  },

  async listChats() {
    await sleep(50);
    return [...generalChatList];
  },

  async getChatDetail(chatId) {
    await sleep(30);
    const wire = mockWireById.get(chatId);
    if (!wire) {
      throw new Error(`Mock chat not found: ${chatId}`);
    }
    return { chat: wire, messages: [] };
  },

  async deleteChat(chatId) {
    await sleep(30);
    const wire = mockWireById.get(chatId);
    mockWireById.delete(chatId);
    chatContextMap.delete(chatId);
    if (wire?.contextType && wire.contextId) {
      contextToChatMap.delete(`${wire.contextType}:${wire.contextId}`);
    }
    const idx = generalChatList.findIndex((c) => c.chatId === chatId);
    if (idx >= 0) {
      generalChatList.splice(idx, 1);
    }
  },

  async renameChat(chatId, chatName) {
    await sleep(30);
    const wire = mockWireById.get(chatId);
    if (!wire) {
      throw new Error(`Mock chat not found: ${chatId}`);
    }
    const next: ChatResponseWire = {
      ...wire,
      chatName,
      updatedAt: new Date().toISOString(),
    };
    mockWireById.set(chatId, next);
    const summaryIdx = generalChatList.findIndex((c) => c.chatId === chatId);
    if (summaryIdx >= 0) {
      generalChatList[summaryIdx] = summarizeFromWire(next);
    }
    return next;
  },

  async listAgentProfiles() {
    await sleep(20);
    return [
      {
        id: 'data-analysis',
        capabilityIds: ['sql.query'],
      },
      {
        id: 'hello-world',
        capabilityIds: ['conversation.general'],
      },
      {
        id: 'schema-exploration',
        capabilityIds: ['metadata.schema-read', 'sql.query'],
      },
    ];
  },

  async getChatByContext(contextType, contextId) {
    await sleep(50);
    return contextToChatMap.get(`${contextType}:${contextId}`) ?? null;
  },
};

const realChatService: ChatService = {
  async createChat(params) {
    const payload = buildCreateBody(params);
    const res = await fetch(CHATS_PREFIX, {
      ...FETCH_AI_JSON,
      method: 'POST',
      headers: {
        ...FETCH_AI_JSON.headers,
        ...(payload ? { 'Content-Type': 'application/json' } : {}),
      },
      body: payload ? JSON.stringify(payload) : undefined,
    });
    await ensureOk(res, 'POST /api/v1/ai/chats');
    const chat = await readJson<ChatResponseWire>(res);
    return { chatId: chat.chatId, chatName: chat.chatName };
  },

  async *sendMessage(chatId, message, options) {
    const res = await fetch(`${CHATS_PREFIX}/${encodeURIComponent(chatId)}/messages`, {
      ...FETCH_AI_SSE,
      method: 'POST',
      body: JSON.stringify({ message }),
    });
    await ensureOk(res, 'POST SSE /api/v1/ai/chats/.../messages');
    yield* consumeChatSse(res, options);
  },

  async listChats() {
    const res = await fetch(CHATS_PREFIX, {
      ...FETCH_AI_JSON,
      method: 'GET',
    });
    await ensureOk(res, 'GET /api/v1/ai/chats');
    const list = await readJson<ChatResponseWire[]>(res);
    return list.map((chat) => ({
      chatId: chat.chatId,
      chatName: chat.chatName,
      updatedAt: Date.parse(chat.updatedAt),
    }));
  },

  async getChatDetail(chatId) {
    const res = await fetch(`${CHATS_PREFIX}/${encodeURIComponent(chatId)}`, {
      ...FETCH_AI_JSON,
      method: 'GET',
    });
    await ensureOk(res, `GET /api/v1/ai/chats/${chatId}`);
    return readJson<ChatDetailResponseWire>(res);
  },

  async deleteChat(chatId) {
    const res = await fetch(`${CHATS_PREFIX}/${encodeURIComponent(chatId)}`, {
      credentials: 'include',
      method: 'DELETE',
    });
    await ensureOk(res, `DELETE /api/v1/ai/chats/${chatId}`);
  },

  async renameChat(chatId, chatName) {
    const res = await fetch(`${CHATS_PREFIX}/${encodeURIComponent(chatId)}`, {
      credentials: 'include',
      method: 'PATCH',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ chatName }),
    });
    await ensureOk(res, `PATCH /api/v1/ai/chats/${chatId}`);
    return readJson<ChatResponseWire>(res);
  },

  async listAgentProfiles() {
    const res = await fetch(PROFILES_PREFIX, {
      ...FETCH_AI_JSON,
      method: 'GET',
    });
    await ensureOk(res, 'GET /api/v1/ai/profiles');
    return readJson<AgentProfileResponseWire[]>(res);
  },

  async getChatByContext(contextType, contextId) {
    const path = `${CHATS_PREFIX}/context-types/${encodeURIComponent(contextType)}/contexts/${encodeURIComponent(contextId)}`;
    const res = await fetch(path, {
      ...FETCH_AI_JSON,
      method: 'GET',
    });
    if (res.status === 404) {
      return null;
    }
    await ensureOk(res, path);
    const chat = await readJson<ChatResponseWire>(res);
    return chat.chatId;
  },
};

export const chatService: ChatService = isRestChatBackendActive() ? realChatService : mockChatService;

/** Explicit mock handle for tests that need to reference the in-memory implementation. */
export { mockChatService, realChatService };
