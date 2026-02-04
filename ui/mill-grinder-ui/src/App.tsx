import { AppShell } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { Navigate, Route, Routes, useLocation, useParams } from "react-router";
import NotFound from "./component/NotFound";
import ChatLayout from "./component/chat/ChatLayout.tsx";
import MetadataLayout from "./component/data-model/MetadataLayout.tsx";
import ContextLayout from "./component/context/ContextLayout.tsx";
import { AppHeader } from "./component/layout/AppHeader";
import { AppSidebar } from "./component/layout/AppSidebar";
import { ChatSidebarContent } from "./component/chat/ChatSidebarContent";
import { MetadataSidebarContent } from "./component/data-model/MetadataSidebarContent";
import { ContextSidebarContent } from "./component/context/ContextSidebarContent";
import { ChatProvider } from "./component/chat/ChatProvider";

function ExploreRedirect() {
    const params = useParams<{ schema?: string; table?: string; attribute?: string }>();
    let newPath = "/data-model";
    if (params.schema) {
        newPath += `/${params.schema}`;
        if (params.table) {
            newPath += `/${params.table}`;
            if (params.attribute) {
                newPath += `/${params.attribute}`;
            }
        }
    }
    return <Navigate to={newPath} replace />;
}

function SidebarContent() {
    const location = useLocation();
    
    if (location.pathname.startsWith('/chat')) {
        return <ChatSidebarContent />;
    }
    if (location.pathname.startsWith('/data-model')) {
        return <MetadataSidebarContent />;
    }
    if (location.pathname.startsWith('/context')) {
        return <ContextSidebarContent />;
    }
    return null;
}

function App() {
    const [navbarOpened, { toggle: toggleNavbar }] = useDisclosure(true);
    const location = useLocation();

    // Single, shared ChatProvider for both sidebar + main chat view.
    // This fixes "new chat doesn't appear until refresh" (caused by multiple providers).
    const activeChatId = (() => {
        const m = location.pathname.match(/^\/chat(?:\/([^/]+))?/);
        return m?.[1] ? decodeURIComponent(m[1]) : undefined;
    })();

    return (
        <ChatProvider chatId={activeChatId}>
            <AppShell
                h="100vh"
                header={{ height: 56 }}
                navbar={{
                    width: 320,
                    breakpoint: 'sm',
                    collapsed: { mobile: !navbarOpened, desktop: !navbarOpened },
                }}
                padding="md"
            >
                <AppShell.Header>
                    <AppHeader navbarOpened={navbarOpened} onToggleNavbar={toggleNavbar} />
                </AppShell.Header>

                <AppShell.Navbar p="xs">
                    <AppSidebar>
                        <SidebarContent />
                    </AppSidebar>
                </AppShell.Navbar>

                <AppShell.Main style={{ height: '100%', overflow: 'hidden' }}>
                    <Routes>
                        <Route path="/overview/*" element={<NotFound />} />
                        <Route path="/data/*" element={<NotFound />} />
                        <Route path="/data-model/:schema?/:table?/:attribute?" element={<MetadataLayout />} />
                        <Route path="/context/:contextId?" element={<ContextLayout />} />
                        {/* Redirect old concepts URLs to context */}
                        <Route path="/concepts/:conceptId?" element={<Navigate to="/context" replace />} />
                        <Route path="/explore" element={<Navigate to="/data-model" replace />} />
                        <Route path="/explore/data/:schema?/:table?/:attribute?" element={<ExploreRedirect />} />
                        <Route path="/explore/:schema/:table/:attribute?" element={<ExploreRedirect />} />
                        <Route path="/chat/*" element={<ChatLayout />} />
                        <Route path="/stats/*" element={<NotFound />} />
                        <Route index element={<Navigate to="/chat" replace />} />
                    </Routes>
                </AppShell.Main>
            </AppShell>
        </ChatProvider>
    );
}

export default App
