import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App'
import { MantineProvider } from '@mantine/core';
import { BrowserRouter } from "react-router";
import { CodeHighlightAdapterProvider, createShikiAdapter } from "@mantine/code-highlight";
import { createHighlighterCore, createOnigurumaEngine } from "shiki";
import { Notifications } from "@mantine/notifications";
import { theme } from './theme';

// Re-export theme for backward compatibility
export { theme } from './theme';

async function loadShiki() {
    const highlighter = await createHighlighterCore({
        langs: [
            import('@shikijs/langs/sql'),
            import('@shikijs/langs/json')
        ],
        themes: [
            import('@shikijs/themes/github-light')
        ],
        engine: createOnigurumaEngine(import('shiki/wasm')),
    });
    return highlighter;
}

const shikiAdapter = createShikiAdapter(loadShiki);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter basename="/app" >
      <MantineProvider theme={theme} defaultColorScheme="light">
          <Notifications />
          <CodeHighlightAdapterProvider adapter={shikiAdapter}>
            <App />
          </CodeHighlightAdapterProvider>
      </MantineProvider>
    </BrowserRouter>
  </StrictMode>,
)
