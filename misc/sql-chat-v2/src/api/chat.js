export async function sendChatQuery(query) {
  const response = await fetch('/data-bot/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query })
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || 'Chat request failed');
  }

  return await response.json();
}
