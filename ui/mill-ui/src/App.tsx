import { MantineProvider, ColorSchemeScript, Box } from '@mantine/core';
import { Notifications } from '@mantine/notifications';
import { Routes, Route, Navigate, useLocation } from 'react-router';
import { useEffect, useMemo, useState, useCallback, createContext, useContext } from 'react';
import { buildTheme } from './theme/theme';
import { ColorThemeProvider, useColorTheme } from './theme/ThemeContext';
import { FeatureFlagProvider, useFeatureFlags } from './features/FeatureFlagContext';
import { ChatProvider } from './context/ChatContext';
import { InlineChatProvider } from './context/InlineChatContext';
import { ChatReferencesProvider } from './context/ChatReferencesContext';
import { RelatedContentProvider } from './context/RelatedContentContext';
import { AppHeader } from './components/layout/AppHeader';
import { AppShell } from './components/layout/AppShell';
import { OverviewDashboard } from './components/overview/OverviewDashboard';
import { DataModelLayout } from './components/data-model/DataModelLayout';
import { ContextLayout } from './components/context/ContextLayout';
import { QueryPlayground } from './components/queries/QueryPlayground';
import { AdminLayout } from './components/admin/AdminLayout';
import { ProfileLayout } from './components/profile/ProfileLayout';
import { ConnectLayout } from './components/connect/ConnectLayout';
import { InlineChatDrawer } from './components/inline-chat/InlineChatDrawer';
import { LoginPage } from './components/auth/LoginPage';
import { NotFoundPage } from './components/common/NotFoundPage';
import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';

/* ── Lightweight auth context (mock) ─────────────────────────────── */
interface AuthContextValue {
  isAuthenticated: boolean;
  login: () => void;
  logout: () => void;
}
const AuthContext = createContext<AuthContextValue>({
  isAuthenticated: false,
  login: () => {},
  logout: () => {},
});
export const useAuth = () => useContext(AuthContext);

function ChatView() {
  return (
    <ChatProvider>
      <AppShell />
    </ChatProvider>
  );
}

/** TODO: will come from the backend */
export const APP_NAME = 'MILL UI';

/** Maps route prefixes to page labels for document.title */
const routeTitles: [string, string][] = [
  ['/home', 'Home'],
  ['/model', 'Model'],
  ['/knowledge', 'Knowledge'],
  ['/analysis', 'Analysis'],
  ['/chat', 'Chat'],
  ['/connect', 'Connect'],
  ['/admin', 'Admin'],
  ['/profile', 'Profile'],
];

/** Inner shell that reads the selected color theme and builds the Mantine theme */
function ThemedApp() {
  const { lightTheme, darkTheme } = useColorTheme();
  const flags = useFeatureFlags();
  const { isAuthenticated, login } = useAuth();
  const location = useLocation();

  const { theme: mantineTheme, resolver: cssVariablesResolver } = useMemo(
    () => buildTheme(lightTheme, darkTheme),
    [lightTheme, darkTheme],
  );

  // Update browser tab title on route change
  useEffect(() => {
    const match = routeTitles.find(([prefix]) => location.pathname.startsWith(prefix));
    const pageLabel = match ? match[1] : null;
    document.title = pageLabel ? `${pageLabel} — ${APP_NAME}` : APP_NAME;
  }, [location.pathname]);

  // Determine the default route (first enabled view)
  const defaultRoute = flags.viewHome ? '/home'
    : flags.viewModel ? '/model'
    : flags.viewKnowledge ? '/knowledge'
    : flags.viewAnalysis ? '/analysis'
    : flags.viewChat ? '/chat'
    : '/home';

  return (
    <MantineProvider theme={mantineTheme} cssVariablesResolver={cssVariablesResolver} defaultColorScheme="auto">
      {!isAuthenticated ? (
        /* Full-screen login — no header, no navigation */
        <LoginPage onLogin={login} />
      ) : (
        <InlineChatProvider>
        <ChatReferencesProvider>
        <RelatedContentProvider>
          <Notifications position="top-right" />
          <Box style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
            <AppHeader />
            <Box style={{ flex: 1, overflow: 'hidden', display: 'flex' }}>
              <Box style={{ flex: 1, overflow: 'hidden' }}>
                <Routes>
                  {flags.viewHome && <Route path="/home" element={<OverviewDashboard />} />}
                  {flags.viewModel && <Route path="/model/:schema?/:table?/:attribute?" element={<DataModelLayout />} />}
                  {flags.viewKnowledge && <Route path="/knowledge/:conceptId?" element={<ContextLayout />} />}
                  {flags.viewAnalysis && <Route path="/analysis/:queryId?" element={<QueryPlayground />} />}
                  {flags.viewChat && <Route path="/chat/*" element={<ChatView />} />}
                  {flags.viewConnect && <Route path="/connect/:section?" element={<ConnectLayout />} />}
                  {flags.viewAdmin && <Route path="/admin/:section?" element={<AdminLayout />} />}
                  {flags.viewProfile && <Route path="/profile/:section?" element={<ProfileLayout />} />}
                  <Route index element={<Navigate to={defaultRoute} replace />} />
                  <Route path="*" element={<NotFoundPage />} />
                </Routes>
              </Box>
              {flags.inlineChatEnabled && <InlineChatDrawer />}
            </Box>
          </Box>
        </RelatedContentProvider>
        </ChatReferencesProvider>
        </InlineChatProvider>
      )}
    </MantineProvider>
  );
}

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(true);
  const login = useCallback(() => setIsAuthenticated(true), []);
  const logout = useCallback(() => setIsAuthenticated(false), []);

  const authValue = useMemo(
    () => ({ isAuthenticated, login, logout }),
    [isAuthenticated, login, logout],
  );

  return (
    <>
      <ColorSchemeScript defaultColorScheme="auto" />
      <AuthContext.Provider value={authValue}>
        <ColorThemeProvider>
          <FeatureFlagProvider>
            <ThemedApp />
          </FeatureFlagProvider>
        </ColorThemeProvider>
      </AuthContext.Provider>
    </>
  );
}

export default App;
