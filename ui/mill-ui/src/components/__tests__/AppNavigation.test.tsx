import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { MantineProvider } from '@mantine/core';
import { defaultFeatureFlags } from '../../features/defaults';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import { ColorThemeProvider } from '../../theme/ThemeContext';
import { InlineChatProvider } from '../../context/InlineChatContext';
import { ChatReferencesProvider } from '../../context/ChatReferencesContext';
import { RelatedContentProvider } from '../../context/RelatedContentContext';
import { ChatProvider } from '../../context/ChatContext';
import { ChatRouteSync } from '../chat/ChatRouteSync';
import { AppHeader } from '../layout/AppHeader';
import { AppShell } from '../layout/AppShell';

const { testTree, testEntity } = vi.hoisted(() => ({
  testTree: [
    {
      id: 'model-entity',
      type: 'MODEL' as const,
      name: 'Model',
      children: [],
    },
  ],
  testEntity: {
    id: 'model-entity',
    entityType: 'MODEL' as const,
    name: 'Model',
    schemaName: '',
    tableName: '',
    columnName: '',
    columns: [],
  },
}));

vi.mock('../../services/api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../services/api')>();
  return {
    ...actual,
    schemaService: {
      ...actual.schemaService,
      getTree: vi.fn(() => Promise.resolve(testTree)),
      getEntityById: vi.fn(() => Promise.resolve(testEntity)),
      getEntityFacets: vi.fn(() => Promise.resolve({})),
    },
    chatService: {
      async createChat() {
        return { chatId: 'chat-1', chatName: 'Chat' };
      },
      async listChats() {
        return [];
      },
      async listAgentProfiles() {
        return [];
      },
      async *sendMessage() {
        yield 'ok';
      },
    },
    featureService: {
      async getFlags() {
        return { ...defaultFeatureFlags };
      },
    },
    searchService: {
      async search() {
        return [];
      },
    },
    chatReferencesService: {
      getConversationsForContext: vi.fn(() => Promise.resolve([])),
    },
  };
});

vi.mock('../../App', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../App')>();
  return {
    ...actual,
    useAuth: () => ({
      user: { userId: 'u1', email: 'u@test', displayName: 'User', groups: [], securityEnabled: false },
      loading: false,
      isAuthenticated: true,
      securityEnabled: false,
      login: async () => {},
      logout: async () => {},
      updateProfile: async () => {},
      register: async () => {},
    }),
  };
});

function ChatLayout() {
  return (
    <>
      <ChatRouteSync />
      <AppShell />
    </>
  );
}

function ChatView() {
  return (
    <ChatProvider>
      <Routes>
        <Route index element={<ChatLayout />} />
        <Route path=":conversationId" element={<ChatLayout />} />
      </Routes>
    </ChatProvider>
  );
}

beforeEach(() => {
  localStorage.clear();
});

async function renderAppNav(initialPath = '/model/model-entity') {
  const { DataModelLayout } = await import('../data-model/DataModelLayout');

  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <MantineProvider>
        <ColorThemeProvider>
        <FeatureFlagProvider>
          <InlineChatProvider>
            <ChatReferencesProvider>
              <RelatedContentProvider>
                <AppHeader />
                <Routes>
                  <Route
                    path="/*"
                    element={
                      <Routes>
                        <Route path="/model/:schema?/:table?/:attribute?" element={<DataModelLayout />} />
                        <Route path="/chat/*" element={<ChatView />} />
                      </Routes>
                    }
                  />
                </Routes>
              </RelatedContentProvider>
            </ChatReferencesProvider>
          </InlineChatProvider>
        </FeatureFlagProvider>
        </ColorThemeProvider>
      </MantineProvider>
    </MemoryRouter>,
  );
}

describe('App navigation from model to chat', () => {
  it('should switch from model explorer to chat when header Chat is clicked', async () => {
    const user = userEvent.setup();
    await renderAppNav();

    await waitFor(() => {
      expect(screen.getByText('Model')).toBeInTheDocument();
    });

    const chatNav = screen.getByRole('button', { name: /^Chat$/i });
    await user.click(chatNav);

    await waitFor(() => {
      expect(screen.getByText('Conversations')).toBeInTheDocument();
    });
    expect(screen.queryByText('Schema Browser')).not.toBeInTheDocument();
  }, 15_000);
});
