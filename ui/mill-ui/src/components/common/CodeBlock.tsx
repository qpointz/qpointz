import { CodeHighlight } from '@mantine/code-highlight';
import { Box } from '@mantine/core';
import '@mantine/code-highlight/styles.css';

interface CodeBlockProps {
  code: string;
  language?: string;
}

export function CodeBlock({ code, language = 'text' }: CodeBlockProps) {
  return (
    <Box my="xs">
      <CodeHighlight
        code={code}
        language={language}
        withCopyButton
        copyLabel="Copy code"
        copiedLabel="Copied!"
      />
    </Box>
  );
}
