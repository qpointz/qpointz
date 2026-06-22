import { Badge, Card, Group, Loader, Stack, Tabs, Text } from '@mantine/core';
import { useEffect, useMemo, useState } from 'react';
import {
  HiOutlineArrowsRightLeft,
  HiOutlineCube,
  HiOutlineDocumentText,
} from 'react-icons/hi2';
import type { FacetTypeManifest } from '../../../types/facetTypes';
import { facetTypeService } from '../../../services/api';
import { normalizeFacetTypeKeyForApi } from '../../../utils/urnSlug';
import { FacetReadOnlyBody } from '../../data-model/facets/FacetReadOnlyBody';
import { facetBoxBaseTitle, facetCondensedTabLabel } from '../../data-model/facets/facetDisplayUtils';
import { useFeatureFlags } from '../../../features/FeatureFlagContext';
import { ChatArtifactActionBar } from './ChatArtifactActionBar';
import { ChatArtifactCard } from './ChatArtifactCard';
import { FacetJsonReadOnlyPanel } from './FacetJsonReadOnlyPanel';
import type { ArtifactPreviewContext } from './types';

function facetHeaderIcon(facetTypeKey: string) {
  if (facetTypeKey.endsWith(':descriptive')) return <HiOutlineDocumentText size={16} />;
  if (facetTypeKey.endsWith(':structural')) return <HiOutlineCube size={16} />;
  if (facetTypeKey.endsWith(':relation')) return <HiOutlineArrowsRightLeft size={16} />;
  return null;
}

/** Condensed facet-proposal preview — tabbed Facet + JSON shell (general and inline metadata chat). */
export function FacetCondensedPreview(props: ArtifactPreviewContext) {
  const { group } = props;
  const artifact = group.facet;
  const flags = useFeatureFlags();
  const [activeTab, setActiveTab] = useState<string | null>('facet');
  const [descriptor, setDescriptor] = useState<FacetTypeManifest | null>(null);
  const [facetTypeTitles, setFacetTypeTitles] = useState<Record<string, string>>({});
  const [descriptorLoading, setDescriptorLoading] = useState(true);
  const [descriptorError, setDescriptorError] = useState(false);

  const facetTypeKey = artifact?.facetTypeKey ?? '';

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
            <ChatArtifactActionBar enabledActions={[]} reserveLayout />
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
                <Badge size="xs" variant="light" color="grape">
                  Proposed
                </Badge>
              </Group>
              <Text size="xs" c="dimmed" ff="monospace" mb={6}>
                {artifact.metadataEntityId}
              </Text>
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
      </Stack>
    </ChatArtifactCard>
  );
}
