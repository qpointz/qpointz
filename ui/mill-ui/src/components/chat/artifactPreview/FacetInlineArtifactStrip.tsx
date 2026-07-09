import { Group, Loader, Stack } from '@mantine/core';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import type { FacetTypeManifest } from '../../../types/facetTypes';
import { facetTypeService } from '../../../services/api';
import { normalizeFacetTypeKeyForApi } from '../../../utils/urnSlug';
import { useFeatureFlags } from '../../../features/FeatureFlagContext';
import { facetEntityCatalogPath, modelRouteFromCatalogPath } from '../../../utils/metadataEntityDisplay';
import {
  chatScopeSlug,
  formatScopeSearchParam,
  GLOBAL_SCOPE_SLUG,
} from '../../../utils/modelScopeQuery';
import { FacetReadOnlyBody } from '../../data-model/facets/FacetReadOnlyBody';
import { facetBoxBaseTitle } from '../../data-model/facets/facetDisplayUtils';
import { resolveArtifactTreatment } from './chatArtifactTreatments';
import { useFacetProposalLifecycle } from './facetProposalLifecycle';
import { InlineArtifactStripActionBar } from './InlineArtifactStripActionBar';
import { InlineArtifactPillStrip } from './InlineArtifactPillStrip';
import { inlineFacetHeadline } from './inlineArtifactHeadline';
import {
  popoverActionsForInline,
  stripActionsForInline,
} from './inlineArtifactActionPlacement';
import type { ArtifactActionId, ArtifactPreviewContext } from './types';

/** Compact facet proposal strip for inline hosts. */
export function FacetInlineArtifactStrip(props: ArtifactPreviewContext) {
  const { chatType, group, conversationId, message, onArtifactsChange } = props;
  if (group.kind !== 'facet-proposal') {
    return null;
  }

  const artifact = group.facet;
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const treatment = resolveArtifactTreatment(chatType, 'facet-proposal');
  const treatmentActions = (treatment.actions ?? []).filter((id: ArtifactActionId) => {
    if (id === 'open-in-model') return flags.viewModel;
    if (id === 'reject') return true;
    if (id === 'accept') return true;
    return true;
  });
  const stripActions = stripActionsForInline('facet-proposal', treatmentActions).filter((id) => {
    if (id === 'open-in-model') return flags.viewModel;
    return true;
  });
  const popoverActions = popoverActionsForInline('facet-proposal', treatmentActions);

  const lifecycle = useFacetProposalLifecycle({
    artifact,
    conversationId,
    message,
    onArtifactsChange,
  });

  const enabledStripActions = stripActions.filter((id) => {
    if (id === 'reject') return lifecycle.canReject;
    if (id === 'accept') return lifecycle.canAccept;
    return true;
  });

  const [copyCopied, setCopyCopied] = useState(false);
  const [facetTypeTitles, setFacetTypeTitles] = useState<Record<string, string>>({});
  const [descriptor, setDescriptor] = useState<FacetTypeManifest | null>(null);
  const [descriptorLoading, setDescriptorLoading] = useState(true);
  const [descriptorError, setDescriptorError] = useState(false);

  const facetTypeKey = artifact.facetTypeKey ?? '';

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

  const facetTypeTitle = facetBoxBaseTitle(facetTypeKey, facetTypeTitles, descriptor);
  const headline = inlineFacetHeadline(artifact, facetTypeTitle);
  const assignedCatalogPath = useMemo(
    () => facetEntityCatalogPath(artifact.catalogPath, artifact.metadataEntityId),
    [artifact.catalogPath, artifact.metadataEntityId],
  );
  const modelRoute = useMemo(
    () => (assignedCatalogPath ? modelRouteFromCatalogPath(assignedCatalogPath) : '/model'),
    [assignedCatalogPath],
  );

  const handleCopy = useCallback(async () => {
    try {
      await navigator.clipboard.writeText(JSON.stringify(artifact.payload ?? artifact, null, 2));
      setCopyCopied(true);
      window.setTimeout(() => setCopyCopied(false), 1500);
    } catch {
      /* clipboard unavailable */
    }
  }, [artifact]);

  const handleOpenInModel = useCallback(() => {
    if (!assignedCatalogPath || !flags.viewModel) return;
    const scope = formatScopeSearchParam([GLOBAL_SCOPE_SLUG, chatScopeSlug(conversationId)]);
    navigate(`${modelRoute}?scope=${encodeURIComponent(scope)}`);
  }, [assignedCatalogPath, conversationId, flags.viewModel, modelRoute, navigate]);

  return (
    <InlineArtifactPillStrip
      ariaLabel={`Facet proposal ${headline}`}
      typeBadge={{
        kind: 'facet-proposal',
        facetTypeKey,
        facetCategory: descriptor?.category ?? null,
      }}
      headline={headline}
      rejected={lifecycle.isRejected}
      popoverTitle={headline}
      popoverSubtitle={artifact.rationale}
      stripActions={
        <InlineArtifactStripActionBar
          enabledActions={enabledStripActions}
          onCopy={undefined}
          onOpenInModel={enabledStripActions.includes('open-in-model') ? handleOpenInModel : undefined}
          onAccept={enabledStripActions.includes('accept') ? () => void lifecycle.handleAccept() : undefined}
          onReject={enabledStripActions.includes('reject') ? () => void lifecycle.handleReject() : undefined}
          isLifecycleBusy={lifecycle.lifecycleBusy}
        />
      }
      popoverBody={
        <Stack gap="xs">
          {descriptorLoading ? (
            <Group justify="center" py="md">
              <Loader size="sm" />
            </Group>
          ) : (
            <FacetReadOnlyBody
              facetTypeKey={artifact.facetTypeKey}
              payload={artifact.payload}
              descriptor={descriptorError ? null : descriptor}
              structuralFacetEnabled={flags.modelStructuralFacet}
              keyPrefix={`inline-facet-${artifact.metadataEntityId}`}
              jsonFallbackMinHeight={180}
            />
          )}
        </Stack>
      }
      popoverActions={
        popoverActions.length > 0 ? (
          <InlineArtifactStripActionBar
            enabledActions={popoverActions}
            onCopy={handleCopy}
            copyCopied={copyCopied}
          />
        ) : undefined
      }
    />
  );
}
