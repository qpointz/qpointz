import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import type { Message } from '../../../../types/chat';
import type { FacetTypeManifest } from '../../../../types/facetTypes';
import { FacetCondensedPreview } from '../FacetCondensedPreview';
import type { ArtifactRenderGroup } from '../types';

vi.mock('../FacetJsonReadOnlyPanel', () => ({
  FacetJsonReadOnlyPanel: ({ json }: { json: string }) => (
    <div data-testid="facet-json-panel">{json}</div>
  ),
}));

vi.mock('../../../../features/FeatureFlagContext', () => ({
  useFeatureFlags: () => ({ modelStructuralFacet: false, chatSqlExecute: false, chatAgentPicker: false }),
}));

const getMock = vi.fn();

vi.mock('../../../../services/api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../../../services/api')>();
  return {
    ...actual,
    facetTypeService: {
      ...actual.facetTypeService,
      list: vi.fn().mockResolvedValue([]),
      get: (...args: unknown[]) => getMock(...args),
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

function renderPreview(message: Message) {
  const group: ArtifactRenderGroup = {
    kind: 'facet-proposal',
    facet: {
      kind: 'facet-proposal',
      facetTypeKey: 'urn:mill/metadata/facet-type:descriptive',
      metadataEntityId: 'urn:mill:metadata:entity:table:demo',
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
    getMock.mockReset();
    getMock.mockResolvedValue(descriptiveManifest);
  });

  it('should render Facet and JSON tabs with reserved action bar layout', async () => {
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
          metadataEntityId: 'urn:mill:metadata:entity:table:demo',
          payload: { description: 'Sample facet text' },
        },
      ],
    });

    expect(screen.getByRole('tab', { name: 'Facet:Descriptive' })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'JSON' })).toBeInTheDocument();
    expect(screen.getByText('urn:mill:metadata:entity:table:demo')).toBeInTheDocument();
    expect(screen.getByText('Proposed')).toBeInTheDocument();

    await waitFor(() => {
      expect(getMock).toHaveBeenCalledWith('urn:mill/metadata/facet-type:descriptive');
    });
    await waitFor(() => {
      expect(screen.getByText('Description')).toBeInTheDocument();
      expect(screen.getByText('Sample facet text')).toBeInTheDocument();
    });
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
