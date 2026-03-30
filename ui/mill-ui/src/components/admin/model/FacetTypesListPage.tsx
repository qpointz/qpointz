import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActionIcon,
  Badge,
  Box,
  Button,
  Card,
  Group,
  Modal,
  MultiSelect,
  Stack,
  Switch,
  Table,
  Text,
  TextInput,
  Tooltip,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import {
  HiOutlineClipboardDocument,
  HiOutlinePencilSquare,
  HiOutlinePlus,
  HiOutlineQueueList,
  HiOutlineSquares2X2,
  HiOutlineTrash,
} from 'react-icons/hi2';
import { useNavigate } from 'react-router';
import { facetTypeService } from '../../../services/api';
import type { FacetTypeManifest } from '../../../types/facetTypes';
import { ApplicableToPills } from './ApplicableToPills';
import { KNOWN_TARGETS } from './knownTargets';
import { facetTypeLocalDisplayKey } from '../../../utils/urnSlug';

type ViewMode = 'list' | 'cards';

function normalizeCategory(raw?: string): string {
  const value = (raw ?? '').trim();
  return value ? value.toLowerCase() : 'general';
}

function formatCategoryLabel(category: string): string {
  const spaced = category.replace(/-/g, ' ');
  return spaced.length === 0 ? 'General' : `${spaced.charAt(0).toUpperCase()}${spaced.slice(1)}`;
}

/** Shared layout for Category + Applicable-to filters (equal flex width, single-line pill row). */
const facetTypesFilterMultiSelectStyles = {
  wrapper: { maxHeight: 'var(--input-height)' },
  input: {
    maxHeight: 'var(--input-height)',
    minHeight: 'var(--input-height)',
    overflow: 'hidden',
    alignItems: 'center',
  },
  pillsList: {
    flexWrap: 'nowrap',
    overflowX: 'auto',
    overflowY: 'hidden',
    alignItems: 'center',
    flex: '1 1 auto',
    minWidth: 0,
    maxHeight: 'calc(var(--input-height) - 2px)',
  },
} as const;

function matchesQuery(item: FacetTypeManifest, query: string): boolean {
  if (!query) return true;
  const q = query.toLowerCase();
  const fullKey = (item.typeKey ?? '').toLowerCase();
  const shortKey = facetTypeLocalDisplayKey(item.typeKey).toLowerCase();
  const title = (item.title ?? '').toLowerCase();
  const desc = (item.description ?? '').toLowerCase();
  return fullKey.includes(q) || shortKey.includes(q) || title.includes(q) || desc.includes(q);
}

interface FacetTypesListPageProps {
  readOnly: boolean;
}

export function FacetTypesListPage({ readOnly }: FacetTypesListPageProps) {
  const navigate = useNavigate();
  const [items, setItems] = useState<FacetTypeManifest[]>([]);
  const [query, setQuery] = useState('');
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [enabledOnly, setEnabledOnly] = useState(false);
  const [targetFilter, setTargetFilter] = useState<string[]>([]);
  const [categoryFilter, setCategoryFilter] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  /** Set when user chooses Delete; confirmed in modal before API call. */
  const [deleteTarget, setDeleteTarget] = useState<{ typeKey: string; title: string } | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const data = await facetTypeService.list({ enabledOnly });
      setItems(data);
    } catch (e) {
      notifications.show({
        color: 'red',
        title: 'Failed to load facet types',
        message: e instanceof Error ? e.message : 'Unknown error',
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [enabledOnly]);

  const categoryOptions = useMemo(() => {
    const categories = Array.from(new Set(items.map((i) => normalizeCategory(i.category))));
    categories.sort((a, b) => {
      if (a === 'general') return -1;
      if (b === 'general') return 1;
      return a.localeCompare(b);
    });
    return categories.map((c) => ({ value: c, label: formatCategoryLabel(c) }));
  }, [items]);

  const filtered = useMemo(() => {
    return items.filter((i) => {
      if (!matchesQuery(i, query)) return false;
      const category = normalizeCategory(i.category);
      if (categoryFilter.length > 0 && !categoryFilter.includes(category)) return false;
      if (targetFilter.length === 0) return true;
      const applicable = i.applicableTo ?? [];
      /** Empty list means the type applies to any entity; keep visible for every applicable-to filter. */
      if (applicable.length === 0) return true;
      return targetFilter.some((target) => applicable.includes(target));
    });
  }, [items, query, targetFilter, categoryFilter]);

  const openDeleteConfirm = (item: FacetTypeManifest) => {
    const typeKey = item.typeKey?.trim() ?? '';
    if (!typeKey) {
      notifications.show({ color: 'red', title: 'Cannot delete', message: 'Missing facet type key.' });
      return;
    }
    setDeleteTarget({ typeKey, title: item.title?.trim() ? item.title : '—' });
  };

  const performDelete = async (typeKey: string) => {
    if (!typeKey.trim()) {
      notifications.show({ color: 'red', title: 'Cannot delete', message: 'Missing facet type key.' });
      return;
    }
    try {
      await facetTypeService.delete(typeKey);
      notifications.show({ color: 'green', title: 'Deleted', message: typeKey });
      await load();
    } catch (e) {
      notifications.show({
        color: 'red',
        title: 'Delete failed',
        message: e instanceof Error ? e.message : 'Unknown error',
      });
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    const { typeKey } = deleteTarget;
    setDeleteTarget(null);
    await performDelete(typeKey);
  };

  const toRouteKey = (typeKey: string | undefined | null) => {
    const k = typeof typeKey === 'string' ? typeKey : '';
    const prefix = 'urn:mill/metadata/facet-type:';
    return k.startsWith(prefix) ? k.slice(prefix.length) : k;
  };
  const editPath = (typeKey: string | undefined | null) => {
    const route = toRouteKey(typeKey).trim();
    if (!route) {
      return null;
    }
    return `/admin/model/facet-types/${encodeURIComponent(route)}/edit`;
  };

  const copyFullUrn = useCallback(async (urn: string) => {
    try {
      await navigator.clipboard.writeText(urn);
      notifications.show({ color: 'green', title: 'Copied', message: 'Full facet-type URN copied.' });
    } catch (e) {
      notifications.show({
        color: 'red',
        title: 'Copy failed',
        message: e instanceof Error ? e.message : 'Clipboard is unavailable',
      });
    }
  }, []);

  return (
    <Stack p="md" gap="md">
      <Group justify="space-between">
        <Text fw={700} size="lg">Facet Types</Text>
        <Tooltip label={readOnly ? 'Read-only mode enabled by feature flag' : 'Create facet type'}>
          <Button
            leftSection={<HiOutlinePlus size={16} />}
            disabled={readOnly}
            onClick={() => navigate('/admin/model/facet-types/new')}
          >
            Create
          </Button>
        </Tooltip>
      </Group>

      <Stack gap="sm">
        <Group wrap="nowrap" align="flex-end">
          <TextInput
            placeholder="Search by title, description, or URN"
            value={query}
            onChange={(e) => setQuery(e.currentTarget.value)}
            style={{ flex: 1, minWidth: 0 }}
          />
          <Group gap={4} wrap="nowrap" style={{ flexShrink: 0 }}>
            <Tooltip label="List view" withArrow>
              <ActionIcon
                variant={viewMode === 'list' ? 'filled' : 'default'}
                size="lg"
                aria-label="List view"
                aria-pressed={viewMode === 'list'}
                onClick={() => setViewMode('list')}
              >
                <HiOutlineQueueList size={20} />
              </ActionIcon>
            </Tooltip>
            <Tooltip label="Card view" withArrow>
              <ActionIcon
                variant={viewMode === 'cards' ? 'filled' : 'default'}
                size="lg"
                aria-label="Card view"
                aria-pressed={viewMode === 'cards'}
                onClick={() => setViewMode('cards')}
              >
                <HiOutlineSquares2X2 size={20} />
              </ActionIcon>
            </Tooltip>
          </Group>
        </Group>
        <Group
          align="flex-end"
          wrap="nowrap"
          gap="sm"
          style={{ minWidth: 0, overflowX: 'auto', overflowY: 'hidden' }}
        >
          <MultiSelect
            placeholder="Category (multi)"
            value={categoryFilter}
            onChange={setCategoryFilter}
            data={categoryOptions}
            searchable
            clearable
            style={{ flex: '1 1 0', minWidth: 160 }}
            styles={facetTypesFilterMultiSelectStyles}
          />
          <MultiSelect
            placeholder="Applicable to (multi)"
            value={targetFilter}
            onChange={setTargetFilter}
            data={KNOWN_TARGETS.map((t) => ({ value: t.urn, label: t.label }))}
            searchable
            clearable
            style={{ flex: '1 1 0', minWidth: 160 }}
            styles={facetTypesFilterMultiSelectStyles}
          />
          <Switch
            checked={enabledOnly}
            onChange={(e) => setEnabledOnly(e.currentTarget.checked)}
            label="Enabled only"
            style={{ flexShrink: 0 }}
          />
        </Group>
      </Stack>

      {viewMode === 'list' ? (
        <Table striped withTableBorder highlightOnHover>
          <Table.Thead>
            <Table.Tr>
              <Table.Th>Title</Table.Th>
              <Table.Th>Type Key</Table.Th>
              <Table.Th>Enabled</Table.Th>
              <Table.Th>Mandatory</Table.Th>
              <Table.Th>Cardinality</Table.Th>
              <Table.Th>Category</Table.Th>
              <Table.Th>Applicable To</Table.Th>
              <Table.Th>Actions</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {filtered.map((item, rowIdx) => (
              <Table.Tr key={item.typeKey?.trim() ? item.typeKey : `row-${rowIdx}`}>
                <Table.Td>{item.title ?? '—'}</Table.Td>
                <Table.Td>
                  <Text size="xs" ff="monospace">
                    {item.typeKey?.trim() ? facetTypeLocalDisplayKey(item.typeKey) : '—'}
                  </Text>
                </Table.Td>
                <Table.Td><Badge color={item.enabled ? 'green' : 'gray'}>{String(item.enabled)}</Badge></Table.Td>
                <Table.Td><Badge color={item.mandatory ? 'orange' : 'gray'}>{String(item.mandatory)}</Badge></Table.Td>
                <Table.Td>
                  <Badge color={(item.targetCardinality ?? 'SINGLE') === 'MULTIPLE' ? 'blue' : 'violet'}>
                    {(item.targetCardinality ?? 'SINGLE').toLowerCase()}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Badge variant="light" color="gray">
                    {formatCategoryLabel(normalizeCategory(item.category))}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <ApplicableToPills values={item.applicableTo ?? []} />
                </Table.Td>
                <Table.Td>
                  <Group gap="xs">
                    <ActionIcon
                      variant="light"
                      aria-label="Edit facet type"
                      disabled={!item.typeKey?.trim()}
                      onClick={() => {
                        const path = editPath(item.typeKey);
                        if (path) {
                          void navigate(path);
                        } else {
                          notifications.show({
                            color: 'red',
                            title: 'Cannot edit',
                            message: 'This facet type has no URN / type key in the API response.',
                          });
                        }
                      }}
                    >
                      <HiOutlinePencilSquare size={16} />
                    </ActionIcon>
                    <Tooltip label={readOnly ? 'Read-only mode enabled' : 'Delete'}>
                      <ActionIcon
                        variant="light"
                        color="red"
                        disabled={readOnly || !item.typeKey?.trim()}
                        onClick={() => openDeleteConfirm(item)}
                      >
                        <HiOutlineTrash size={16} />
                      </ActionIcon>
                    </Tooltip>
                  </Group>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      ) : (
        <Group align="stretch">
          {filtered.map((item, rowIdx) => (
            <FacetTypeCardUrn
              key={item.typeKey?.trim() ? item.typeKey : `card-${rowIdx}`}
              fullUrn={item.typeKey?.trim() ? item.typeKey! : ''}
              title={item.title ?? '—'}
              description={item.description ?? ''}
              onCopyUrn={() => void copyFullUrn(item.typeKey ?? '')}
              readOnly={readOnly}
              item={item}
              editPath={editPath}
              navigate={navigate}
              onRequestDelete={openDeleteConfirm}
            />
          ))}
        </Group>
      )}

      {!loading && filtered.length === 0 && <Box><Text c="dimmed">No facet types found.</Text></Box>}

      <Modal
        opened={deleteTarget != null}
        onClose={() => setDeleteTarget(null)}
        title="Delete facet type?"
        centered
      >
        <Stack gap="sm">
          <Text size="sm">
            Permanently delete <Text span fw={600}>{deleteTarget?.title}</Text>? This cannot be undone.
          </Text>
          {deleteTarget?.typeKey ? (
            <Text size="xs" c="dimmed" ff="monospace" style={{ wordBreak: 'break-all' }}>
              {deleteTarget.typeKey}
            </Text>
          ) : null}
          <Group justify="flex-end" gap="sm" mt="xs">
            <Button variant="default" onClick={() => setDeleteTarget(null)}>
              Cancel
            </Button>
            <Button color="red" onClick={() => void confirmDelete()}>
              Delete
            </Button>
          </Group>
        </Stack>
      </Modal>
    </Stack>
  );
}

interface FacetTypeCardUrnProps {
  fullUrn: string;
  title: string;
  description: string;
  onCopyUrn: () => void;
  readOnly: boolean;
  item: FacetTypeManifest;
  editPath: (typeKey: string | undefined | null) => string | null;
  navigate: ReturnType<typeof useNavigate>;
  onRequestDelete: (item: FacetTypeManifest) => void;
}

function FacetTypeCardUrn({
  fullUrn,
  title,
  description,
  onCopyUrn,
  readOnly,
  item,
  editPath,
  navigate,
  onRequestDelete,
}: FacetTypeCardUrnProps) {
  const [urnHover, setUrnHover] = useState(false);
  return (
    <Card withBorder radius="md" style={{ width: 360 }}>
      <Stack gap="xs">
        <Text fw={600}>{title}</Text>
        {fullUrn ? (
          <Group
            gap={6}
            align="flex-start"
            wrap="nowrap"
            onMouseEnter={() => setUrnHover(true)}
            onMouseLeave={() => setUrnHover(false)}
          >
            <Text size="xs" c="dimmed" style={{ wordBreak: 'break-all', flex: 1 }}>
              {fullUrn}
            </Text>
            <Tooltip label="Copy URN" withArrow disabled={!urnHover}>
              <ActionIcon
                variant="subtle"
                size="sm"
                aria-label="Copy facet type URN"
                onClick={onCopyUrn}
                style={{ flexShrink: 0, opacity: urnHover ? 1 : 0, transition: 'opacity 120ms ease' }}
              >
                <HiOutlineClipboardDocument size={14} />
              </ActionIcon>
            </Tooltip>
          </Group>
        ) : (
          <Text size="xs" c="dimmed">
            —
          </Text>
        )}
        <Text size="sm">{description}</Text>
        <Group gap="xs">
          <Badge color={item.enabled ? 'green' : 'gray'}>enabled: {String(item.enabled)}</Badge>
          <Badge color={item.mandatory ? 'orange' : 'gray'}>mandatory: {String(item.mandatory)}</Badge>
          <Badge color={(item.targetCardinality ?? 'SINGLE') === 'MULTIPLE' ? 'blue' : 'violet'}>
            cardinality: {(item.targetCardinality ?? 'SINGLE').toLowerCase()}
          </Badge>
          <Badge variant="light" color="gray">
            category: {formatCategoryLabel(normalizeCategory(item.category))}
          </Badge>
        </Group>
        <ApplicableToPills values={item.applicableTo ?? []} />
        <Group>
          <Button
            size="xs"
            variant="light"
            disabled={!item.typeKey?.trim()}
            onClick={() => {
              const path = editPath(item.typeKey);
              if (path) {
                void navigate(path);
              } else {
                notifications.show({
                  color: 'red',
                  title: 'Cannot edit',
                  message: 'This facet type has no URN / type key in the API response.',
                });
              }
            }}
          >
            Edit
          </Button>
          <Tooltip label={readOnly ? 'Read-only mode enabled' : 'Delete'}>
            <Button
              size="xs"
              color="red"
              variant="light"
              disabled={readOnly || !item.typeKey?.trim()}
              onClick={() => onRequestDelete(item)}
            >
              Delete
            </Button>
          </Tooltip>
        </Group>
      </Stack>
    </Card>
  );
}
