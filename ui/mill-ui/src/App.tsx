import { MantineProvider, ColorSchemeScript, Box, Loader, Center } from '@mantine/core';
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
import { RegisterPage } from './components/auth/RegisterPage';
import { NotFoundPage } from './components/common/NotFoundPage';
import * as authService from './services/authService';
import type { AuthMeResponse, UserProfilePatch } from './services/authService';
import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';

/* ── Auth context ─────────────────────────────────────────────────── */
interface AuthContextValue {
  user: AuthMeResponse | null;
  loading: boolean;
  isAuthenticated: boolean;
  securityEnabled: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  updateProfile: (patch: UserProfilePatch) => Promise<void>;
  register: (email: string, password: string, displayName?: string) => Promise<void>;
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  loading: true,
  isAuthenticated: false,
  securityEnabled: true,
  login: async () => {},
  logout: async () => {},
  updateProfile: async () => {},
  register: async () => {},
});

export const useAuth = () => useContext(AuthContext);

/* ── RequireAuth ──────────────────────────────────────────────────── */
function RequireAuth({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();
  if (loading) return null;
  if (!isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />;
  return <>{children}</>;
}

/* ── APP_NAME ─────────────────────────────────────────────────────── */
/** Resolved from GET /.well-known/mill on startup; falls back to 'Mill'. */
export let APP_NAME = 'Mill';

async function fetchAppName(): Promise<string> {
  try {
    const res = await fetch('/.well-known/mill');
    if (!res.ok) return 'Mill';
    const data = await res.json() as { name?: string };
    return data.name ?? 'Mill';
  } catch {
    return 'Mill';
  }
}

/* ── Route title map ──────────────────────────────────────────────── */
const routeTitles: [string, string][] = [
  ['/home', 'Home'],
  ['/model', 'Model'],
  ['/knowledge', 'Knowledge'],
  ['/analysis', 'Analysis'],
  ['/chat', 'Chat'],
  ['/connect', 'Connect'],
  ['/admin', 'Admin'],
  ['/profile', 'Profile'],
  ['/login', 'Sign in'],
];

function ChatView() {
  return (
    <ChatProvider>
      <AppShell />
    </ChatProvider>
  );
}

/* ── ThemedApp ────────────────────────────────────────────────────── */
function ThemedApp() {
  const { lightTheme, darkTheme } = useColorTheme();
  const flags = useFeatureFlags();
  const { isAuthenticated, loading, login, register } = useAuth();
  const location = useLocation();

  const { theme: mantineTheme, resolver: cssVariablesResolver } = useMemo(
    () => buildTheme(lightTheme, darkTheme),
    [lightTheme, darkTheme],
  );

  useEffect(() => {
    const match = routeTitles.find(([prefix]) => location.pathname.startsWith(prefix));
    const pageLabel = match ? match[1] : null;
    document.title = pageLabel ? `${pageLabel} — ${APP_NAME}` : APP_NAME;
  }, [location.pathname]);

  const defaultRoute = flags.viewHome ? '/home'
    : flags.viewModel ? '/model'
    : flags.viewKnowledge ? '/knowledge'
    : flags.viewAnalysis ? '/analysis'
    : flags.viewChat ? '/chat'
    : '/home';

  if (loading) {
    return (
      <MantineProvider theme={mantineTheme} cssVariablesResolver={cssVariablesResolver} defaultColorScheme="auto">
        <Center style={{ height: '100vh' }}>
          <Loader size="xl" />
        </Center>
      </MantineProvider>
    );
  }

  return (
    <MantineProvider theme={mantineTheme} cssVariablesResolver={cssVariablesResolver} defaultColorScheme="auto">
      <InlineChatProvider>
      <ChatReferencesProvider>
      <RelatedContentProvider>
        <Notifications position="top-right" />
        <Box style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
          {isAuthenticated && <AppHeader />}
          <Box style={{ flex: 1, overflow: 'hidden', display: 'flex' }}>
            <Box style={{ flex: 1, overflow: 'hidden' }}>
              <Routes>
                <Route
                  path="/login"
                  element={
                    isAuthenticated
                      ? <Navigate to={defaultRoute} replace />
                      : <LoginPage onLogin={login} />
                  }
                />
                <Route
                  path="/register"
                  element={
                    isAuthenticated
                      ? <Navigate to={defaultRoute} replace />
                      : <RegisterPage onRegister={register} />
                  }
                />
                <Route
                  path="/*"
                  element={
                    <RequireAuth>
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
                    </RequireAuth>
                  }
                />
              </Routes>
            </Box>
            {isAuthenticated && flags.inlineChatEnabled && <InlineChatDrawer />}
          </Box>
        </Box>
      </RelatedContentProvider>
      </ChatReferencesProvider>
      </InlineChatProvider>
    </MantineProvider>
  );
}

/* ── App ──────────────────────────────────────────────────────────── */
function App() {
  const [user, setUser] = useState<AuthMeResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [appName, setAppName] = useState('Mill');

  useEffect(() => {
    // Load app name and session user in parallel
    Promise.all([
      fetchAppName(),
      authService.getMe(),
    ]).then(([name, me]) => {
      APP_NAME = name;
      setAppName(name);
      if (me && !me.securityEnabled) {
        // Security off — treat as anonymous authenticated user
        setUser({ userId: 'anonymous', email: null, displayName: null, groups: [], securityEnabled: false });
      } else {
        setUser(me);
      }
    }).catch(() => {
      setUser(null);
    }).finally(() => {
      setLoading(false);
    });
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const me = await authService.login(email, password);
    setUser(me);
  }, []);

  const logout = useCallback(async () => {
    await authService.logout();
    setUser(null);
  }, []);

  const register = useCallback(async (email: string, password: string, displayName?: string) => {
    const me = await authService.register(email, password, displayName);
    setUser(me);
  }, []);

  const updateProfile = useCallback(async (patch: UserProfilePatch) => {
    const updatedProfile = await authService.updateProfile(patch);
    setUser((prev) => {
      if (prev === null) return prev;
      return { ...prev, profile: updatedProfile };
    });
  }, []);

  // Derive securityEnabled from user or default true
  const securityEnabled = user?.securityEnabled ?? true;
  const isAuthenticated = user !== null;

  const authValue = useMemo<AuthContextValue>(
    () => ({ user, loading, isAuthenticated, securityEnabled, login, logout, updateProfile, register }),
    [user, loading, isAuthenticated, securityEnabled, login, logout, updateProfile, register],
  );

  // appName in state so APP_NAME module-level var is kept in sync
  void appName;

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
