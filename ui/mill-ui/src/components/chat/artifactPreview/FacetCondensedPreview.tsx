import { Badge, Card, Group, Loader, Stack, Tabs, Text, Anchor } from '@mantine/core';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import {
  HiOutlineArrowsRightLeft,
  HiOutlineCube,
  HiOutlineDocumentText,
} from 'react-icons/hi2';
import type { FacetTypeManifest } from '../../../types/facetTypes';
import { facetTypeService, chatService } from '../../../services/api';
import { normalizeFacetTypeKeyForApi } from '../../../utils/urnSlug';
import { FacetReadOnlyBody } from '../../data-model/facets/FacetReadOnlyBody';
import { facetBoxBaseTitle, facetCondensedTabLabel } from '../../data-model/facets/facetDisplayUtils';
import { useFeatureFlags } from '../../../features/FeatureFlagContext';
import { facetEntityCatalogPath, modelRouteFromCatalogPath } from '../../../utils/metadataEntityDisplay';
import { ChatArtifactActionBar } from './ChatArtifactActionBar';
import { ChatArtifactCard } from './ChatArtifactCard';
import { resolveArtifactTreatment } from './chatArtifactTreatments';
import { FacetJsonReadOnlyPanel } from './FacetJsonReadOnlyPanel';
import type { ArtifactActionId, ArtifactPreviewContext } from './types';

function facetHeaderIcon(facetTypeKey: string) {
  if (facetTypeKey.endsWith(':descriptive')) return <HiOutlineDocumentText size={16} />;
  if (facetTypeKey.endsWith(':structural')) return <HiOutlineCube size={16} />;
  if (facetTypeKey.endsWith(':relation')) return <HiOutlineArrowsRightLeft size={16} />;
  return null;
}

/** Condensed facet-proposal preview — tabbed Facet + JSON shell (general and inline metadata chat). */
export function FacetCondensedPreview(props: ArtifactPreviewContext) {
  const { chatType, group, conversationId, onArtifactsChange } = props;
  const artifact = group.facet;
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<string | null>('facet');
  const [lifecycleBusy, setLifecycleBusy] = useState(false);
  const [copyCopied, setCopyCopied] = useState(false);
  const [localStatus, setLocalStatus] = useState<string | undefined>(artifact?.status);
  const [descriptor, setDescriptor] = useState<FacetTypeManifest | null>(null);
  const [facetTypeTitles, setFacetTypeTitles] = useState<Record<string, string>>({});
  const [descriptorLoading, setDescriptorLoading] = useState(true);
  const [descriptorError, setDescriptorError] = useState(false);

  const facetTypeKey = artifact?.facetTypeKey ?? '';
  const artifactId = artifact?.artifactId;
  const status = localStatus ?? artifact?.status ?? 'active';
  const isActive = status === 'active' || status === 'pending' || status === 'accepted';
  const isRejected = status === 'rejected' || status === 'declined' || status === 'retracted';

  const treatment = resolveArtifactTreatment(chatType, 'facet-proposal');
  const enabledActions = (treatment.actions ?? []).filter((id: ArtifactActionId) => {
    if (id === 'reject') return isActive && Boolean(artifactId);
    if (id === 'accept') return isRejected && Boolean(artifactId);
    if (id === 'open-in-model') return flags.viewModel;
    return true;
  });

  const assignedCatalogPath = useMemo(
    () => (artifact ? facetEntityCatalogPath(artifact.catalogPath, artifact.metadataEntityId) : ''),
    [artifact],
  );

  const modelRoute = useMemo(
    () => (assignedCatalogPath ? modelRouteFromCatalogPath(assignedCatalogPath) : '/model'),
    [assignedCatalogPath],
  );

  const handleOpenInModel = useCallback(() => {
    if (!assignedCatalogPath || !flags.viewModel) return;
    navigate(modelRoute);
  }, [assignedCatalogPath, flags.viewModel, modelRoute, navigate]);

  useEffect(() => {
    setLocalStatus(artifact?.status);
  }, [artifact?.artifactId, artifact?.status]);

  const updateFacetStatus = (nextStatus: string) => {
    setLocalStatus(nextStatus);
    if (!artifact || !onArtifactsChange) return;
    const prev = props.message.artifacts ?? [];
    onArtifactsChange(
      prev.map((entry) =>
        entry.kind === 'facet-proposal' && entry.artifactId === artifact.artifactId
          ? { ...entry, status: nextStatus }
          : entry,
      ),
    );
  };

  const handleAccept = async () => {
    if (!artifactId) return;
    setLifecycleBusy(true);
    try {
      const updated = await chatService.acceptArtifact(conversationId, artifactId);
      if (updated?.kind === 'facet-proposal') {
        updateFacetStatus(updated.status ?? 'active');
      } else {
        updateFacetStatus('active');
      }
    } finally {
      setLifecycleBusy(false);
    }
  };

  const handleReject = async () => {
    if (!artifactId) return;
    setLifecycleBusy(true);
    try {
      const ok = await chatService.rejectArtifact(conversationId, artifactId);
      if (ok) {
        updateFacetStatus('rejected');
      }
    } finally {
      setLifecycleBusy(false);
    }
  };

  useEffect(() => {
    let cancelled = false;
    void facetTypeService
      .list()
      .then((types) => {
        if (cancelled) return;
        const titles: Record<string, string> = {};
        for (const ft of types) {
          if (ft.title?.trim()) titles[ft.typeKey] = ft.title.trim();
        }
        setFacetTypeTitles(titles);
      })
      .catch(() => {
        if (!cancelled) setFacetTypeTitles({});
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!facetTypeKey.trim()) {
      setDescriptor(null);
      setDescriptorLoading(false);
      setDescriptorError(false);
      return;
    }
    let cancelled = false;
    setDescriptorLoading(true);
    setDescriptorError(false);
    void facetTypeService
      .get(normalizeFacetTypeKeyForApi(facetTypeKey))
      .then((manifest) => {
        if (!cancelled) {
          setDescriptor(manifest);
          setDescriptorError(false);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setDescriptor(null);
          setDescriptorError(true);
        }
      })
      .finally(() => {
        if (!cancelled) setDescriptorLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [facetTypeKey]);

  const facetTabLabel = useMemo(
    () => facetCondensedTabLabel(facetTypeKey, facetTypeTitles, descriptor),
    [facetTypeKey, facetTypeTitles, descriptor],
  );

  const facetCardTitle = useMemo(
    () => facetBoxBaseTitle(facetTypeKey, facetTypeTitles, descriptor),
    [facetTypeKey, facetTypeTitles, descriptor],
  );

  const wireJson = useMemo(
    () =>
      JSON.stringify(
        {
          facetTypeKey: artifact?.facetTypeKey,
          metadataEntityId: artifact?.metadataEntityId,
          payload: artifact?.payload ?? null,
        },
        null,
        2,
      ),
    [artifact],
  );

  const handleCopy = useCallback(async () => {
    if (!wireJson.trim()) return;
    await navigator.clipboard.writeText(wireJson);
    setCopyCopied(true);
    window.setTimeout(() => setCopyCopied(false), 1500);
  }, [wireJson]);

  if (!artifact) return null;

  return (
    <ChatArtifactCard p="xs">
      <Stack gap={4}>
        <Tabs value={activeTab} onChange={setActiveTab}>
          <Group justify="space-between" align="center" wrap="nowrap" gap={4}>
            <Tabs.List style={{ flexWrap: 'nowrap' }}>
              <Tabs.Tab value="facet">{facetTabLabel}</Tabs.Tab>
              <Tabs.Tab value="json">JSON</Tabs.Tab>
            </Tabs.List>
            <ChatArtifactActionBar
              enabledActions={enabledActions}
              onCopy={() => void handleCopy()}
              copyCopied={copyCopied}
              copyTooltip="Copy JSON"
              onOpenInModel={enabledActions.includes('open-in-model') ? handleOpenInModel : undefined}
              onReject={enabledActions.includes('reject') ? () => void handleReject() : undefined}
              onAccept={enabledActions.includes('accept') ? () => void handleAccept() : undefined}
              isLifecycleBusy={lifecycleBusy}
            />
          </Group>
          <Tabs.Panel value="facet" pt={4}>
            <Card withBorder p="xs">
              <Group justify="space-between" mb={6} align="flex-start" wrap="nowrap">
                <Group gap="xs" wrap="wrap">
                  {facetHeaderIcon(facetTypeKey)}
                  <Text fw={600} size="sm">
                    {facetCardTitle}
                  </Text>
                </Group>
                <Badge size="xs" variant="light" color={isRejected ? 'gray' : 'teal'}>
                  {isRejected ? 'Rejected' : 'Active'}
                </Badge>
              </Group>
              {descriptorLoading ? (
                <Group justify="center" py="md">
                  <Loader size="sm" />
                </Group>
              ) : (
                <FacetReadOnlyBody
                  facetTypeKey={facetTypeKey}
                  payload={artifact.payload}
                  descriptor={descriptorError ? null : descriptor}
                  structuralFacetEnabled={flags.modelStructuralFacet}
                  keyPrefix={`facet-proposal-${artifact.metadataEntityId}`}
                  jsonFallbackMinHeight={200}
                />
              )}
            </Card>
          </Tabs.Panel>
          <Tabs.Panel value="json" pt={4}>
            <FacetJsonReadOnlyPanel json={wireJson} maxHeight={220} />
          </Tabs.Panel>
        </Tabs>
        {assignedCatalogPath ? (
          <Text size="xs" c="dimmed">
            Assigned to:{' '}
            {flags.viewModel ? (
              <Anchor
                size="xs"
                component="button"
                type="button"
                onClick={handleOpenInModel}
                style={{ fontWeight: 500 }}
              >
                {assignedCatalogPath}
              </Anchor>
            ) : (
              <Text component="span" size="xs" fw={500} c="dimmed">
                {assignedCatalogPath}
              </Text>
            )}
          </Text>
        ) : null}
      </Stack>
    </ChatArtifactCard>
  );
}
