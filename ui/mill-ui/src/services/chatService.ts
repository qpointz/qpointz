import type { ChatService, ChatSummary, CreateChatParams } from '../types/chat';
import { sleep, streamResponse } from '../utils/streamUtils';

// --- Mock response pools ---

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

/** Maps chatId -> contextType for context-aware responses */
const chatContextMap = new Map<string, string>();

/** Maps "contextType:contextId" -> chatId for inline chat lookup */
const contextToChatMap = new Map<string, string>();

/** Tracks general (non-contextual) chats for listChats */
const generalChatList: ChatSummary[] = [];

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

// --- Mock service implementation ---

const mockChatService: ChatService = {
  async createChat(params?: CreateChatParams) {
    await sleep(100);
    const chatId = crypto.randomUUID();
    const chatName = params?.contextLabel ? `Chat: ${params.contextLabel}` : 'New Chat';

    if (params?.contextType && params?.contextId) {
      // Contextual (inline) chat -- store context mapping, NOT in general list
      chatContextMap.set(chatId, params.contextType);
      contextToChatMap.set(`${params.contextType}:${params.contextId}`, chatId);
    } else {
      // General chat -- add to the general list
      generalChatList.unshift({ chatId, chatName, updatedAt: Date.now() });
    }

    return { chatId, chatName };
  },

  async *sendMessage(chatId: string, _message: string) {
    const pool = getResponsePool(chatId);
    const response = pickRandom(pool);
    if (!response) return;
    yield* streamResponse(response);
  },

  async listChats() {
    await sleep(50);
    // Return only general (non-contextual) chats
    return [...generalChatList];
  },

  async getChatByContext(contextType: string, contextId: string) {
    await sleep(50);
    return contextToChatMap.get(`${contextType}:${contextId}`) ?? null;
  },
};

// When real backend is ready, create realChatService and change the export below
export const chatService: ChatService = mockChatService;
