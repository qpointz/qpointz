import { useState } from 'react';
import {
  ActionIcon,
  Tooltip,
  Badge,
  Box,
  Text,
  Group,
  Popover,
  ScrollArea,
  useMantineColorScheme,
} from '@mantine/core';
import {
  HiOutlineLink,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiOutlineLightBulb,
  HiOutlineBeaker,
  HiOutlineArrowTopRightOnSquare,
} from 'react-icons/hi2';
import { useNavigate } from 'react-router';
import { useRelatedContent } from '../../context/RelatedContentContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { RelatedModelTree } from './RelatedModelTree';
import type { RelatedContentRef, RelatedContentType } from '../../types/relatedContent';

interface RelatedContentButtonProps {
  contextType: string;
  contextId: string;
  contextLabel: string;
  contextEntityType?: string;
}

/** Icon for each related content type */
function RefIcon({ type, entityType, size, color }: {
  type: RelatedContentType;
  entityType?: string;
  size: number;
  color: string;
}) {
  if (type === 'model') {
    if (entityType === 'ATTRIBUTE') return <HiOutlineViewColumns size={size} color={color} style={{ flexShrink: 0 }} />;
    return <HiOutlineTableCells size={size} color={color} style={{ flexShrink: 0 }} />;
  }
  if (type === 'concept') return <HiOutlineLightBulb size={size} color={color} style={{ flexShrink: 0 }} />;
  if (type === 'analysis') return <HiOutlineBeaker size={size} color={color} style={{ flexShrink: 0 }} />;
  return <HiOutlineLink size={size} color={color} style={{ flexShrink: 0 }} />;
}

/** Color for each related content type badge */
const typeColors: Record<RelatedContentType, string> = {
  model: 'teal',
  concept: 'grape',
  analysis: 'orange',
};

const typeLabels: Record<RelatedContentType, string> = {
  model: 'Model',
  concept: 'Concept',
  analysis: 'Analysis',
};

/** Navigate to the right view for a related ref */
function getNavigationPath(ref: RelatedContentRef): string {
  if (ref.type === 'model') {
    // Model IDs are dot-separated: schema.table.attribute → /model/schema/table/attribute
    return '/model/' + ref.id.replace(/\./g, '/');
  }
  if (ref.type === 'concept') {
    return `/knowledge/${ref.id}`;
  }
  if (ref.type === 'analysis') {
    return `/analysis/${ref.id}`;
  }
  return '/';
}

export function RelatedContentButton({
  contextType,
  contextId,
  contextLabel,
  contextEntityType,
}: RelatedContentButtonProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const { refs } = useRelatedContent(contextType, contextId);
  const [popoverOpen, setPopoverOpen] = useState(false);

  // Global kill-switch
  if (!flags.relatedContentEnabled) return null;

  // Context-type level flags
  if (contextType === 'model') {
    if (!flags.relatedContentModelContext) return null;
    if (contextEntityType === 'SCHEMA' && !flags.relatedContentModelSchema) return null;
    if (contextEntityType === 'TABLE' && !flags.relatedContentModelTable) return null;
    if (contextEntityType === 'ATTRIBUTE' && !flags.relatedContentModelColumn) return null;
  }
  if (contextType === 'knowledge' && !flags.relatedContentKnowledgeContext) return null;
  if (contextType === 'analysis' && !flags.relatedContentAnalysisContext) return null;

  const hasRelated = refs.length > 0;

  if (!hasRelated) return null;

  // Split refs: model refs go into tree, rest stay flat
  const grouped: Record<string, RelatedContentRef[]> = {};
  for (const ref of refs) {
    if (!grouped[ref.type]) grouped[ref.type] = [];
    grouped[ref.type]!.push(ref);
  }
  const nonModelRefs = refs.filter((r) => r.type !== 'model');

  const tooltip = `${refs.length} related item${refs.length > 1 ? 's' : ''} for ${contextLabel}`;

  return (
    <Popover
      opened={popoverOpen}
      onChange={setPopoverOpen}
      position="bottom-end"
      width={280}
      shadow="md"
      withArrow
      arrowSize={8}
    >
      <Popover.Target>
        <Tooltip label={tooltip} withArrow disabled={popoverOpen}>
          <Box style={{ position: 'relative', display: 'inline-flex' }}>
            <ActionIcon
              variant="subtle"
              color={isDark ? 'indigo.4' : 'indigo.6'}
              size="lg"
              onClick={() => setPopoverOpen((o) => !o)}
            >
              <HiOutlineLink size={20} />
            </ActionIcon>
            {/* Related content count badge */}
            <Badge
              size="xs"
              variant="filled"
              color="indigo"
              circle
              style={{
                position: 'absolute',
                bottom: -2,
                right: -2,
                minWidth: 16,
                height: 16,
                padding: 0,
                fontSize: '10px',
                pointerEvents: 'none',
                zIndex: 1,
              }}
            >
              {refs.length}
            </Badge>
          </Box>
        </Tooltip>
      </Popover.Target>

      <Popover.Dropdown
        p={0}
        style={{
          backgroundColor: 'var(--mantine-color-body)',
          border: `1px solid ${isDark ? 'var(--mantine-color-gray-6)' : 'var(--mantine-color-gray-3)'}`,
        }}
      >
        <Box px="xs" pt={6} pb={4}>
          <Group gap={4} mb={6}>
            <HiOutlineLink
              size={12}
              color={isDark ? 'var(--mantine-color-indigo-4)' : 'var(--mantine-color-indigo-6)'}
            />
            <Text size="xs" c="dimmed" tt="uppercase" fw={600} style={{ fontSize: '10px', letterSpacing: '0.5px' }}>
              Related ({refs.length})
            </Text>
          </Group>
          <ScrollArea.Autosize mah={260} type="scroll" scrollbarSize={6}>
            {/* Model refs → tree (schema > table > column) */}
            {grouped['model'] && grouped['model'].length > 0 && (
              <RelatedModelTree
                refs={grouped['model']}
                isDark={isDark}
                iconColor={isDark ? 'var(--mantine-color-indigo-4)' : 'var(--mantine-color-indigo-6)'}
                onNavigate={(fullId) => {
                  setPopoverOpen(false);
                  navigate('/model/' + fullId.replace(/\./g, '/'));
                }}
              />
            )}
            {/* Non-model refs → flat list */}
            {nonModelRefs.map((ref) => (
              <Box
                key={ref.id}
                px="xs"
                py={4}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                  borderRadius: 4,
                  cursor: 'pointer',
                  transition: 'background-color 150ms ease',
                }}
                onClick={() => {
                  setPopoverOpen(false);
                  navigate(getNavigationPath(ref));
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = isDark
                    ? 'var(--mantine-color-dark-6)'
                    : 'var(--mantine-color-gray-1)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = 'transparent';
                }}
              >
                <RefIcon
                  type={ref.type}
                  entityType={ref.entityType}
                  size={14}
                  color={isDark ? 'var(--mantine-color-indigo-4)' : 'var(--mantine-color-indigo-6)'}
                />
                <Text
                  size="xs"
                  c={isDark ? 'gray.2' : 'gray.7'}
                  truncate
                  style={{ flex: 1 }}
                >
                  {ref.title}
                </Text>
                <Badge
                  size="xs"
                  variant="light"
                  color={typeColors[ref.type] || 'gray'}
                  style={{ flexShrink: 0 }}
                >
                  {typeLabels[ref.type] || ref.type}
                </Badge>
                <HiOutlineArrowTopRightOnSquare
                  size={12}
                  color={isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-4)'}
                  style={{ flexShrink: 0 }}
                />
              </Box>
            ))}
          </ScrollArea.Autosize>
        </Box>
      </Popover.Dropdown>
    </Popover>
  );
}
