/** Sleep for a given number of milliseconds. */
export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/** Stream a string word-by-word with simulated typing delay. */
export async function* streamResponse(response: string) {
  await sleep(500);
  const words = response.split(' ');
  for (let i = 0; i < words.length; i++) {
    yield words[i] + (i < words.length - 1 ? ' ' : '');
    await sleep(30 + Math.random() * 50);
  }
}
