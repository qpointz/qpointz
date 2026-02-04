import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';
import '@mantine/code-highlight/styles.css';
import './index.css'
import App from './App'
import { localStorageColorSchemeManager, MantineProvider } from '@mantine/core';
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
const colorSchemeManager = localStorageColorSchemeManager({ key: 'mill-grinder-color-scheme' });

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter basename="/app" >
      <MantineProvider theme={theme} defaultColorScheme="light" colorSchemeManager={colorSchemeManager}>
          <Notifications />
          <CodeHighlightAdapterProvider adapter={shikiAdapter}>
            <App />
          </CodeHighlightAdapterProvider>
      </MantineProvider>
    </BrowserRouter>
  </StrictMode>,
)
