import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App'
import {createTheme, MantineProvider} from '@mantine/core';
import {BrowserRouter} from "react-router";
import {CodeHighlightAdapterProvider, createShikiAdapter} from "@mantine/code-highlight";
import {createHighlighterCore, createOnigurumaEngine} from "shiki"; // or any theme you want

const theme = createTheme({
    fontFamily: 'Inter, sans-serif',
    //fontFamily: 'Roboto, sans-serif',
    primaryColor : 'primary',

    colors : {
        primary:  [
            "#f1f4fe",
            "#e4e6ed",
            "#c8cad3",
            "#a9adb9",
            "#9094a3",
            "#7f8496",
            "#777c91",
            "#63687c",
            "#595e72",
            "#4a5167"
        ],
        success: ['#e6f9f0', '#ccf2e1', '#99e5c3', '#66d9a4', '#33cc85', '#00bf66', '#00994f', '#007339', '#004d26', '#002613'],
        danger: ['#fdecea', '#f9d3ce', '#f4a89e', '#ef7d6e', '#ea5240', '#e6271a', '#bf1f15', '#991710', '#730f0b', '#4d0806'],
        warning: ['#fff4e5', '#ffe8cc', '#ffd699', '#ffc266', '#ffad33', '#ff9900', '#cc7a00', '#995c00', '#663d00', '#331f00'],
        info: ['#e0f7fa', '#b2ebf2', '#80deea', '#4dd0e1', '#26c6da', '#00bcd4', '#00acc1', '#0097a7', '#00838f', '#006064']
    }
});

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
      <MantineProvider  theme={theme}  >
          <CodeHighlightAdapterProvider adapter={shikiAdapter}>
            <App />
          </CodeHighlightAdapterProvider>
      </MantineProvider>
    </BrowserRouter>
  </StrictMode>,
)
