import type { ChatService } from '../types/chat';

const mockResponses = [
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

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export const mockChatService: ChatService = {
  async *sendMessage(_conversationId: string, _message: string) {
    // Simulate network delay
    await sleep(500);

    // Pick a random response
    const response = mockResponses[Math.floor(Math.random() * mockResponses.length)];
    if (!response) return;

    // Stream the response character by character with slight delays
    const words = response.split(' ');
    for (let i = 0; i < words.length; i++) {
      yield words[i] + (i < words.length - 1 ? ' ' : '');
      await sleep(30 + Math.random() * 50); // Random delay between 30-80ms per word
    }
  },
};
