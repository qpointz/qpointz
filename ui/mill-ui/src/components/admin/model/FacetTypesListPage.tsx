import { useEffect, useMemo, useState } from 'react';
import {
  ActionIcon,
  Badge,
  Box,
  Button,
  Card,
  Group,
  MultiSelect,
  SegmentedControl,
  Stack,
  Switch,
  Table,
  Text,
  TextInput,
  Tooltip,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { HiOutlinePencilSquare, HiOutlinePlus, HiOutlineTrash } from 'react-icons/hi2';
import { useNavigate } from 'react-router';
import { facetTypeService } from '../../../services/api';
import type { FacetTypeManifest } from '../../../types/facetTypes';
import { ApplicableToPills } from './ApplicableToPills';
import { KNOWN_TARGETS } from './knownTargets';

type ViewMode = 'list' | 'cards';

function normalizeCategory(raw?: string): string {
  const value = (raw ?? '').trim();
  return value ? value.toLowerCase() : 'general';
}

function formatCategoryLabel(category: string): string {
  const spaced = category.replace(/-/g, ' ');
  return spaced.length === 0 ? 'General' : `${spaced.charAt(0).toUpperCase()}${spaced.slice(1)}`;
}

function matchesQuery(item: FacetTypeManifest, query: string): boolean {
  if (!query) return true;
  const q = query.toLowerCase();
  return (
    item.typeKey.toLowerCase().includes(q) ||
    item.title.toLowerCase().includes(q) ||
    item.description.toLowerCase().includes(q)
  );
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
      return targetFilter.some((target) => applicable.includes(target));
    });
  }, [items, query, targetFilter, categoryFilter]);

  const onDelete = async (typeKey: string) => {
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

  const toRouteKey = (typeKey: string) => {
    const prefix = 'urn:mill/metadata/facet-type:';
    return typeKey.startsWith(prefix) ? typeKey.slice(prefix.length) : typeKey;
  };
  const editPath = (typeKey: string) => `/admin/model/facet-types/${encodeURIComponent(toRouteKey(typeKey))}/edit`;

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

      <Group>
        <TextInput
          placeholder="Search by title, description, or URN"
          value={query}
          onChange={(e) => setQuery(e.currentTarget.value)}
          style={{ flex: 1 }}
        />
        <SegmentedControl
          value={viewMode}
          onChange={(v) => setViewMode(v as ViewMode)}
          data={[
            { label: 'List', value: 'list' },
            { label: 'Cards', value: 'cards' },
          ]}
        />
        <MultiSelect
          placeholder="Applicable to (multi)"
          value={targetFilter}
          onChange={setTargetFilter}
          data={KNOWN_TARGETS.map((t) => ({ value: t.urn, label: t.label }))}
          searchable
          clearable
          style={{ width: 180 }}
        />
        <MultiSelect
          placeholder="Category (multi)"
          value={categoryFilter}
          onChange={setCategoryFilter}
          data={categoryOptions}
          searchable
          clearable
          style={{ width: 180 }}
        />
        <Switch
          checked={enabledOnly}
          onChange={(e) => setEnabledOnly(e.currentTarget.checked)}
          label="Enabled only"
        />
      </Group>

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
            {filtered.map((item) => (
              <Table.Tr key={item.typeKey}>
                <Table.Td>{item.title}</Table.Td>
                <Table.Td><Text size="xs">{item.typeKey}</Text></Table.Td>
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
                    <ActionIcon variant="light" onClick={() => navigate(editPath(item.typeKey))}>
                      <HiOutlinePencilSquare size={16} />
                    </ActionIcon>
                    <Tooltip label={readOnly ? 'Read-only mode enabled' : 'Delete'}>
                      <ActionIcon
                        variant="light"
                        color="red"
                        disabled={readOnly}
                        onClick={() => void onDelete(item.typeKey)}
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
          {filtered.map((item) => (
            <Card key={item.typeKey} withBorder radius="md" style={{ width: 360 }}>
              <Stack gap="xs">
                <Text fw={600}>{item.title}</Text>
                <Text size="xs" c="dimmed">{item.typeKey}</Text>
                <Text size="sm">{item.description}</Text>
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
                  <Button size="xs" variant="light" onClick={() => navigate(editPath(item.typeKey))}>Edit</Button>
                  <Tooltip label={readOnly ? 'Read-only mode enabled' : 'Delete'}>
                    <Button
                      size="xs"
                      color="red"
                      variant="light"
                      disabled={readOnly}
                      onClick={() => void onDelete(item.typeKey)}
                    >
                      Delete
                    </Button>
                  </Tooltip>
                </Group>
              </Stack>
            </Card>
          ))}
        </Group>
      )}

      {!loading && filtered.length === 0 && <Box><Text c="dimmed">No facet types found.</Text></Box>}
    </Stack>
  );
}

