import { MantineProvider, ColorSchemeScript, Box } from '@mantine/core';
import { Notifications } from '@mantine/notifications';
import { Routes, Route, Navigate } from 'react-router';
import { theme } from './theme/theme';
import { ChatProvider } from './context/ChatContext';
import { AppHeader } from './components/layout/AppHeader';
import { AppShell } from './components/layout/AppShell';
import { DataModelLayout } from './components/data-model/DataModelLayout';
import { ContextLayout } from './components/context/ContextLayout';
import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';

function ChatView() {
  return (
    <ChatProvider>
      <AppShell />
    </ChatProvider>
  );
}

function App() {
  return (
    <>
      <ColorSchemeScript defaultColorScheme="auto" />
      <MantineProvider theme={theme} defaultColorScheme="auto">
        <Notifications position="top-right" />
        <Box style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
          <AppHeader />
          <Box style={{ flex: 1, overflow: 'hidden' }}>
            <Routes>
              <Route path="/chat/*" element={<ChatView />} />
              <Route path="/data-model/:schema?/:table?/:attribute?" element={<DataModelLayout />} />
              <Route path="/context/:conceptId?" element={<ContextLayout />} />
              <Route index element={<Navigate to="/chat" replace />} />
              <Route path="*" element={<Navigate to="/chat" replace />} />
            </Routes>
          </Box>
        </Box>
      </MantineProvider>
    </>
  );
}

export default App;
