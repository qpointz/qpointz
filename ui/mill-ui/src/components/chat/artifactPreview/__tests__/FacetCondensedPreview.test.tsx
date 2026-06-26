import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MantineProvider } from '@mantine/core';
import type { Message } from '../../../../types/chat';
import type { FacetTypeManifest } from '../../../../types/facetTypes';
import { FacetCondensedPreview } from '../FacetCondensedPreview';
import type { ArtifactRenderGroup } from '../types';

const navigateMock = vi.fn();

vi.mock('react-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router')>();
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock('../FacetJsonReadOnlyPanel', () => ({
  FacetJsonReadOnlyPanel: ({ json }: { json: string }) => (
    <div data-testid="facet-json-panel">{json}</div>
  ),
}));

vi.mock('../../../../features/FeatureFlagContext', () => ({
  useFeatureFlags: () => ({
    modelStructuralFacet: false,
    chatSqlExecute: false,
    chatAgentPicker: false,
    viewModel: true,
  }),
}));

const getMock = vi.fn();
const acceptMock = vi.fn();
const rejectMock = vi.fn();

vi.mock('../../../../services/api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../../../services/api')>();
  return {
    ...actual,
    facetTypeService: {
      ...actual.facetTypeService,
      list: vi.fn().mockResolvedValue([]),
      get: (...args: unknown[]) => getMock(...args),
    },
    chatService: {
      ...actual.chatService,
      acceptArtifact: (...args: unknown[]) => acceptMock(...args),
      rejectArtifact: (...args: unknown[]) => rejectMock(...args),
    },
  };
});

const descriptiveManifest: FacetTypeManifest = {
  typeKey: 'urn:mill/metadata/facet-type:descriptive',
  title: 'Descriptive',
  description: 'Descriptive facet',
  category: 'descriptive',
  enabled: true,
  mandatory: false,
  payload: {
    type: 'OBJECT',
    title: 'Descriptive',
    description: '',
    fields: [
      {
        name: 'description',
        stereotype: null,
        schema: { type: 'STRING', title: 'Description', description: '' },
      },
    ],
  },
};

function renderPreview(
  message: Message,
  facetStatus = 'active',
  artifactId = 'art-1',
) {
  const group: ArtifactRenderGroup = {
    kind: 'facet-proposal',
    facet: {
      kind: 'facet-proposal',
      artifactId,
      status: facetStatus,
      facetTypeKey: 'urn:mill/metadata/facet-type:descriptive',
      catalogPath: 'demo.orders',
      metadataEntityId: 'urn:mill/model/table:demo.orders',
      payload: { description: 'Sample facet text' },
    },
  };
  return render(
    <MantineProvider>
      <FacetCondensedPreview
        chatType="general"
        message={message}
        group={group}
        conversationId="chat-1"
      />
    </MantineProvider>,
  );
}

describe('FacetCondensedPreview', () => {
  beforeEach(() => {
    navigateMock.mockReset();
    getMock.mockReset();
    acceptMock.mockReset();
    rejectMock.mockReset();
    getMock.mockResolvedValue(descriptiveManifest);
    acceptMock.mockResolvedValue({
      kind: 'facet-proposal',
      artifactId: 'art-1',
      status: 'active',
    });
    rejectMock.mockResolvedValue(true);
  });

  it('should render Facet and JSON tabs with action toolbar', async () => {
    renderPreview({
      id: 'turn-1',
      conversationId: 'chat-1',
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      artifacts: [
        {
          kind: 'facet-proposal',
          facetTypeKey: 'urn:mill/metadata/facet-type:descriptive',
          catalogPath: 'demo.orders',
          metadataEntityId: 'urn:mill/model/table:demo.orders',
          payload: { description: 'Sample facet text' },
        },
      ],
    });

    expect(screen.getByRole('tab', { name: 'Descriptive' })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'JSON' })).toBeInTheDocument();
    expect(screen.getByText('Assigned to:')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'demo.orders' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Open in model' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Copy JSON' })).toBeInTheDocument();
    expect(screen.queryByText('urn:mill/model/table:demo.orders')).not.toBeInTheDocument();

    await waitFor(() => {
      expect(getMock).toHaveBeenCalledWith('urn:mill/metadata/facet-type:descriptive');
    });
    await waitFor(() => {
      expect(screen.getByText('Description')).toBeInTheDocument();
      expect(screen.getByText('Sample facet text')).toBeInTheDocument();
    });
  });

  it('should show Reject on active facet and Accept on rejected facet', async () => {
    const { unmount } = renderPreview(
      {
        id: 'turn-1',
        conversationId: 'chat-1',
        role: 'assistant',
        content: '',
        timestamp: Date.now(),
        artifacts: [],
      },
      'active',
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Reject' })).toBeInTheDocument();
    });
    expect(screen.queryByRole('button', { name: 'Accept' })).not.toBeInTheDocument();

    unmount();

    renderPreview(
      {
        id: 'turn-1',
        conversationId: 'chat-1',
        role: 'assistant',
        content: '',
        timestamp: Date.now(),
        artifacts: [],
      },
      'rejected',
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Accept' })).toBeInTheDocument();
      expect(screen.getByText('Rejected')).toBeInTheDocument();
    });
    expect(screen.queryByRole('button', { name: 'Reject' })).not.toBeInTheDocument();
  });

  it('should call reject without removing card from thread', async () => {
    const onArtifactsChange = vi.fn();
    const group: ArtifactRenderGroup = {
      kind: 'facet-proposal',
      facet: {
        kind: 'facet-proposal',
        artifactId: 'art-1',
        status: 'active',
        facetTypeKey: 'urn:mill/metadata/facet-type:descriptive',
        catalogPath: 'demo.orders',
        metadataEntityId: 'urn:mill/model/table:demo.orders',
        payload: { description: 'Sample facet text' },
      },
    };
    render(
      <MantineProvider>
        <FacetCondensedPreview
          chatType="general"
          message={{
            id: 'turn-1',
            conversationId: 'chat-1',
            role: 'assistant',
            content: '',
            timestamp: Date.now(),
            artifacts: [group.facet!],
          }}
          group={group}
          conversationId="chat-1"
          onArtifactsChange={onArtifactsChange}
        />
      </MantineProvider>,
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Reject' })).toBeInTheDocument();
    });

    await userEvent.click(screen.getByRole('button', { name: 'Reject' }));

    await waitFor(() => {
      expect(rejectMock).toHaveBeenCalledWith('chat-1', 'art-1');
    });
    expect(onArtifactsChange).toHaveBeenCalled();
    const calls = onArtifactsChange.mock.calls;
    const next = calls[calls.length - 1]?.[0] as typeof group.facet[];
    expect(next).toHaveLength(1);
    expect(next[0]?.status).toBe('rejected');
  });

  it('should navigate to model view when Open in model is clicked', async () => {
    renderPreview({
      id: 'turn-1',
      conversationId: 'chat-1',
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      artifacts: [],
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Open in model' })).toBeInTheDocument();
    });

    await userEvent.click(screen.getByRole('button', { name: 'Open in model' }));
    expect(navigateMock).toHaveBeenCalledWith('/model/demo/orders');
  });

  it('should show wire JSON on JSON tab', async () => {
    renderPreview({
      id: 'turn-1',
      conversationId: 'chat-1',
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      artifacts: [],
    });

    await waitFor(() => {
      expect(getMock).toHaveBeenCalled();
    });

    screen.getByRole('tab', { name: 'JSON' }).click();
    const panel = await screen.findByTestId('facet-json-panel');
    expect(panel.textContent).toContain('"facetTypeKey"');
    expect(panel.textContent).toContain('"metadataEntityId"');
    expect(panel.textContent).toContain('"payload"');
  });

  it('should fall back to JSON body when descriptor load fails', async () => {
    getMock.mockRejectedValueOnce(new Error('not found'));
    renderPreview({
      id: 'turn-1',
      conversationId: 'chat-1',
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      artifacts: [],
    });

    await waitFor(() => {
      expect(screen.getByText(/Sample facet text/)).toBeInTheDocument();
    });
  });
});
