import { useEffect, useMemo, useState } from 'react';
import {
  ActionIcon,
  Chip,
  Box,
  Button,
  Divider,
  Group,
  Paper,
  ScrollArea,
  Select,
  Stack,
  Switch,
  Text,
  TextInput,
  Tooltip,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { HiOutlineArrowDown, HiOutlineArrowUp, HiOutlineClipboardDocument, HiOutlinePlus, HiOutlineTrash } from 'react-icons/hi2';
import { useNavigate } from 'react-router';
import { facetTypeService } from '../../../services/api';
import type { FacetEnumValue, FacetPayloadSchema, FacetSchemaType, FacetTypeManifest } from '../../../types/facetTypes';
import { ApplicableToPills } from './ApplicableToPills';
import { normalizeTargetValue } from './knownTargets';
import { JsonYamlEditor } from '../../common/JsonYamlEditor';

interface FacetTypeEditPageProps {
  mode: 'create' | 'edit';
  typeKey?: string;
  readOnly: boolean;
}

const schemaTypeOptions: FacetSchemaType[] = ['OBJECT', 'ARRAY', 'STRING', 'NUMBER', 'BOOLEAN', 'ENUM'];
const stringFormatOptions = [
  { value: '', label: 'none' },
  { value: 'date', label: 'date (YYYY-MM-DD)' },
  { value: 'date-time', label: 'date-time (ISO-8601)' },
  { value: 'email', label: 'email' },
  { value: 'uri', label: 'uri' },
];

function defaultSchema(type: FacetSchemaType = 'STRING'): FacetPayloadSchema {
  return {
    type,
    title: `${type} field`,
    description: `${type} description`,
    fields: type === 'OBJECT' ? [] : undefined,
    required: type === 'OBJECT' ? [] : undefined,
    items: type === 'ARRAY' ? defaultSchema('STRING') : undefined,
    values: type === 'ENUM' ? [{ value: 'value', description: 'Describe this enum value' }] : undefined,
  };
}

function normalizeSchemaByType(node: FacetPayloadSchema, type: FacetSchemaType): FacetPayloadSchema {
  if (type === 'ARRAY') {
    return {
      ...node,
      type,
      title: node.title || 'ARRAY field',
      description: node.description || 'ARRAY description',
      items: node.items ?? defaultSchema('STRING'),
      fields: undefined,
      required: undefined,
      values: undefined,
      format: undefined,
    };
  }
  return defaultSchema(type);
}

function ensureObjectShape(schema: FacetPayloadSchema): FacetPayloadSchema {
  if (schema.type !== 'OBJECT') return schema;
  return {
    ...schema,
    fields: schema.fields ?? [],
    required: schema.required ?? [],
    items: undefined,
    values: undefined,
  };
}

type NodePath = number[];

function pathKey(path: NodePath): string {
  return path.join('.');
}

function toSlug(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
}

function getNodeAtPath(root: FacetPayloadSchema, path: NodePath): FacetPayloadSchema | null {
  let node: FacetPayloadSchema = root;
  for (const idx of path) {
    const fields = node.fields ?? [];
    if (idx < 0 || idx >= fields.length) return null;
    node = fields[idx]?.schema ?? null as unknown as FacetPayloadSchema;
    if (!node) return null;
  }
  return node;
}

function updateNodeAtPath(root: FacetPayloadSchema, path: NodePath, updater: (node: FacetPayloadSchema) => FacetPayloadSchema): FacetPayloadSchema {
  if (path.length === 0) return updater(root);
  const idx = path[0] ?? -1;
  const rest = path.slice(1);
  const objectNode = ensureObjectShape(root);
  const fields = [...(objectNode.fields ?? [])];
  const field = fields[idx];
  if (!field) return root;
  fields[idx] = { ...field, schema: updateNodeAtPath(field.schema, rest, updater) };
  return { ...objectNode, fields };
}

function removeFieldAtParentPath(root: FacetPayloadSchema, parentPath: NodePath, fieldIndex: number): FacetPayloadSchema {
  return updateNodeAtPath(root, parentPath, (node) => {
    const objectNode = ensureObjectShape(node);
    const fields = [...(objectNode.fields ?? [])];
    if (fieldIndex < 0 || fieldIndex >= fields.length) return objectNode;
    fields.splice(fieldIndex, 1);
    const required = (objectNode.required ?? []).filter((name) => name !== (fields[fieldIndex]?.name ?? ''));
    return { ...objectNode, fields, required };
  });
}

function moveFieldAtParentPath(root: FacetPayloadSchema, parentPath: NodePath, fieldIndex: number, direction: -1 | 1): FacetPayloadSchema {
  return updateNodeAtPath(root, parentPath, (node) => {
    const objectNode = ensureObjectShape(node);
    const fields = [...(objectNode.fields ?? [])];
    const swapIndex = fieldIndex + direction;
    if (fieldIndex < 0 || fieldIndex >= fields.length) return objectNode;
    if (swapIndex < 0 || swapIndex >= fields.length) return objectNode;
    const tmp = fields[fieldIndex];
    fields[fieldIndex] = fields[swapIndex]!;
    fields[swapIndex] = tmp!;
    return { ...objectNode, fields };
  });
}

function addFieldToObjectPath(root: FacetPayloadSchema, objectPath: NodePath): FacetPayloadSchema {
  return updateNodeAtPath(root, objectPath, (node) => {
    const objectNode = ensureObjectShape(node);
    if (objectNode.type !== 'OBJECT') return objectNode;
    const nextIndex = (objectNode.fields ?? []).length + 1;
    const fields = [...(objectNode.fields ?? []), { name: `field${nextIndex}`, schema: defaultSchema('STRING'), required: true }];
    return { ...objectNode, fields };
  });
}

function renameFieldAtPath(root: FacetPayloadSchema, path: NodePath, newName: string): FacetPayloadSchema {
  if (path.length === 0) return root;
  const parentPath = path.slice(0, -1);
  const fieldIndex = path[path.length - 1] ?? -1;
  return updateNodeAtPath(root, parentPath, (node) => {
    const objectNode = ensureObjectShape(node);
    const fields = [...(objectNode.fields ?? [])];
    const existing = fields[fieldIndex];
    if (!existing) return objectNode;
    fields[fieldIndex] = { ...existing, name: newName };
    return { ...objectNode, fields };
  });
}

function getFieldNameAtPath(root: FacetPayloadSchema, path: NodePath): string | null {
  if (path.length === 0) return null;
  const parent = getNodeAtPath(root, path.slice(0, -1));
  const idx = path[path.length - 1] ?? -1;
  if (!parent?.fields || idx < 0 || idx >= parent.fields.length) return null;
  return parent.fields[idx]?.name ?? null;
}

function getFieldRequiredAtPath(root: FacetPayloadSchema, path: NodePath): boolean {
  if (path.length === 0) return true;
  const parent = getNodeAtPath(root, path.slice(0, -1));
  const idx = path[path.length - 1] ?? -1;
  if (!parent?.fields || idx < 0 || idx >= parent.fields.length) return true;
  return parent.fields[idx]?.required ?? true;
}

function setFieldRequiredAtPath(root: FacetPayloadSchema, path: NodePath, required: boolean): FacetPayloadSchema {
  if (path.length === 0) return root;
  const parentPath = path.slice(0, -1);
  const fieldIndex = path[path.length - 1] ?? -1;
  return updateNodeAtPath(root, parentPath, (node) => {
    const objectNode = ensureObjectShape(node);
    const fields = [...(objectNode.fields ?? [])];
    const existing = fields[fieldIndex];
    if (!existing) return objectNode;
    fields[fieldIndex] = { ...existing, required };
    return { ...objectNode, fields };
  });
}

interface TreeNodeItemProps {
  root: FacetPayloadSchema;
  path: NodePath;
  label: string;
  selectedPath: NodePath;
  onSelect: (path: NodePath) => void;
  onAddChild: (path: NodePath) => void;
  onDelete: (path: NodePath) => void;
  onMoveUp: (path: NodePath) => void;
  onMoveDown: (path: NodePath) => void;
}

function TreeNodeItem({ root, path, label, selectedPath, onSelect, onAddChild, onDelete, onMoveUp, onMoveDown }: TreeNodeItemProps) {
  const node = getNodeAtPath(root, path);
  if (!node) return null;
  const isSelected = pathKey(path) === pathKey(selectedPath);
  const canAdd = node.type === 'OBJECT';
  const isRoot = path.length === 0;
  const parent = path.length > 0 ? getNodeAtPath(root, path.slice(0, -1)) : null;
  const idx = path.length > 0 ? (path[path.length - 1] ?? -1) : -1;
  const siblingCount = parent?.fields?.length ?? 0;
  const canMoveUp = !isRoot && idx > 0;
  const canMoveDown = !isRoot && idx >= 0 && idx < siblingCount - 1;

  return (
    <Stack gap={4}>
      <Group
        justify="space-between"
        onClick={() => onSelect(path)}
        style={{
          padding: '6px 8px',
          borderRadius: 6,
          backgroundColor: isSelected ? 'var(--mantine-color-blue-light)' : 'transparent',
          cursor: 'pointer',
        }}
      >
        <Text size="sm">
          {label} <Text span c="dimmed">({node.type})</Text>
        </Text>
        <Group gap={4}>
          {!isRoot && (
            <>
              <ActionIcon size="sm" variant="subtle" disabled={!canMoveUp} onClick={(e) => { e.stopPropagation(); onMoveUp(path); }}>
                <HiOutlineArrowUp size={14} />
              </ActionIcon>
              <ActionIcon size="sm" variant="subtle" disabled={!canMoveDown} onClick={(e) => { e.stopPropagation(); onMoveDown(path); }}>
                <HiOutlineArrowDown size={14} />
              </ActionIcon>
            </>
          )}
          {canAdd && (
            <ActionIcon size="sm" variant="subtle" onClick={(e) => { e.stopPropagation(); onAddChild(path); }}>
              <HiOutlinePlus size={14} />
            </ActionIcon>
          )}
          {!isRoot && (
            <ActionIcon size="sm" color="red" variant="subtle" onClick={(e) => { e.stopPropagation(); onDelete(path); }}>
              <HiOutlineTrash size={14} />
            </ActionIcon>
          )}
        </Group>
      </Group>
      {node.type === 'OBJECT' && (node.fields ?? []).map((field, idx) => (
        <Box key={`${pathKey(path)}.${idx}`} ml="md">
          <TreeNodeItem
            root={root}
            path={[...path, idx]}
            label={field.name}
            selectedPath={selectedPath}
            onSelect={onSelect}
            onAddChild={onAddChild}
            onDelete={onDelete}
            onMoveUp={onMoveUp}
            onMoveDown={onMoveDown}
          />
        </Box>
      ))}
    </Stack>
  );
}

export function FacetTypeEditPage({ mode, typeKey, readOnly }: FacetTypeEditPageProps) {
  const navigate = useNavigate();
  const [manifest, setManifest] = useState<FacetTypeManifest>({
    typeKey: '',
    title: '',
    description: '',
    category: 'general',
    enabled: true,
    mandatory: false,
    targetCardinality: 'SINGLE',
    applicableTo: [],
    schemaVersion: '1.0',
    payload: defaultSchema('OBJECT'),
  });
  const [expertMode, setExpertMode] = useState(false);
  const [expertDraft, setExpertDraft] = useState<{ valid: boolean; value?: unknown; error?: string } | null>(null);
  const [applicableDraft, setApplicableDraft] = useState('');
  const [enumValueDraft, setEnumValueDraft] = useState('');
  const [enumDescriptionDraft, setEnumDescriptionDraft] = useState('');
  const [selectedPath, setSelectedPath] = useState<NodePath>([]);
  const [showTypeKeyCopy, setShowTypeKeyCopy] = useState(false);

  useEffect(() => {
    if (mode !== 'edit' || !typeKey) return;
    void facetTypeService.get(typeKey).then((data) => {
      setManifest(data);
    }).catch((e) => {
      notifications.show({
        color: 'red',
        title: 'Failed to load facet type',
        message: e instanceof Error ? e.message : 'Unknown error',
      });
    });
  }, [mode, typeKey]);

  const validate = (target: FacetTypeManifest): string[] => {
    const errors: string[] = [];
    if (!target.typeKey.trim()) errors.push('typeKey is required');
    if (!target.title.trim()) errors.push('title is required');
    if (!target.description.trim()) errors.push('description is required');
    const validateUniqueFields = (schema: FacetPayloadSchema, path: string) => {
      if (schema.type === 'OBJECT') {
        const seen = new Set<string>();
        for (const field of schema.fields ?? []) {
          const key = field.name.trim();
          if (!key) continue;
          if (seen.has(key)) {
            errors.push(`${path} has duplicate field key '${key}'`);
          } else {
            seen.add(key);
          }
          validateUniqueFields(field.schema, `${path}.${key}`);
        }
      } else if (schema.type === 'ARRAY' && schema.items) {
        validateUniqueFields(schema.items, `${path}[]`);
      }
    };
    validateUniqueFields(target.payload, 'payload');
    return errors;
  };

  const save = async () => {
    let manifestToSave = manifest;
    if (expertMode) {
      if (!expertDraft?.valid || !expertDraft.value) {
        notifications.show({
          color: 'red',
          title: 'Invalid expert document',
          message: expertDraft?.error ?? 'Fix JSON/YAML content before saving.',
        });
        return;
      }
      manifestToSave = expertDraft.value as FacetTypeManifest;
      setManifest(manifestToSave);
    }
    const errors = validate(manifestToSave);
    if (errors.length > 0) {
      notifications.show({ color: 'red', title: 'Validation failed', message: errors.join('; ') });
      return;
    }
    try {
      if (mode === 'create') {
        await facetTypeService.create(manifestToSave);
      } else if (typeKey) {
        await facetTypeService.update(typeKey, manifestToSave);
      }
      notifications.show({ color: 'green', title: 'Saved', message: manifestToSave.typeKey });
      navigate('/admin/model/facet-types');
    } catch (e) {
      notifications.show({
        color: 'red',
        title: 'Save failed',
        message: e instanceof Error ? e.message : 'Unknown error',
      });
    }
  };

  const selectedNode = useMemo(
    () => (selectedPath.length > 0 ? getNodeAtPath(manifest.payload, selectedPath) : null),
    [manifest.payload, selectedPath]
  );

  const selectedFieldName = useMemo(
    () => getFieldNameAtPath(manifest.payload, selectedPath) ?? '',
    [manifest.payload, selectedPath]
  );
  const selectedFieldRequired = useMemo(
    () => (selectedPath.length > 0 ? getFieldRequiredAtPath(manifest.payload, selectedPath) : true),
    [manifest.payload, selectedPath]
  );

  const addApplicable = (raw: string) => {
    const value = normalizeTargetValue(raw);
    if (!value) return;
    const current = manifest.applicableTo ?? [];
    if (current.includes(value)) return;
    setManifest({ ...manifest, applicableTo: [...current, value] });
  };

  const removeApplicable = (value: string) => {
    const current = manifest.applicableTo ?? [];
    setManifest({ ...manifest, applicableTo: current.filter((v) => v !== value) });
  };

  const addEnumValue = () => {
    if (!selectedNode || selectedNode.type !== 'ENUM') return;
    const value = enumValueDraft.trim();
    const description = enumDescriptionDraft.trim();
    if (!value || !description) return;
    const current = selectedNode.values ?? [];
    if (current.some((v) => v.value === value)) return;
    const values: FacetEnumValue[] = [...current, { value, description }];
    setManifest({
      ...manifest,
      payload: updateNodeAtPath(manifest.payload, selectedPath, (node) => ({ ...node, values })),
    });
    setEnumValueDraft('');
    setEnumDescriptionDraft('');
  };

  const copyTypeKey = async () => {
    try {
      await navigator.clipboard.writeText(manifest.typeKey);
      notifications.show({ color: 'green', title: 'Copied', message: 'typeKey copied to clipboard' });
    } catch (e) {
      notifications.show({
        color: 'red',
        title: 'Copy failed',
        message: e instanceof Error ? e.message : 'Clipboard is unavailable',
      });
    }
  };

  return (
    <Stack p="md" gap="md" style={{ height: '100%', overflowY: 'auto' }}>
      <Group justify="space-between">
        <Text fw={700} size="lg">{mode === 'create' ? 'Create Facet Type' : 'Edit Facet Type'}</Text>
        <Group>
          <Chip
            checked={expertMode}
            onChange={(next) => {
              if (!next) {
                if (!expertDraft?.valid || !expertDraft.value) {
                  notifications.show({
                    color: 'red',
                    title: 'Invalid expert document',
                    message: expertDraft?.error ?? 'Fix JSON/YAML content before leaving expert mode.',
                  });
                  return;
                }
                setManifest(expertDraft.value as FacetTypeManifest);
              }
              setExpertMode(next);
            }}
            size="sm"
            variant="light"
          >
            Expert JSON mode
          </Chip>
          <Button variant="light" onClick={() => navigate('/admin/model/facet-types')}>Back</Button>
          <Tooltip label={readOnly ? 'Read-only mode enabled by feature flag' : 'Save'}>
            <Button onClick={() => void save()} disabled={readOnly}>Save</Button>
          </Tooltip>
        </Group>
      </Group>

      {expertMode ? (
        <JsonYamlEditor
          value={manifest}
          onApply={(next) => {
            setManifest(next as FacetTypeManifest);
            notifications.show({ color: 'green', title: 'Applied', message: 'Manifest updated from expert mode.' });
          }}
          onDraftParsed={setExpertDraft}
          minHeight={360}
        />
      ) : (
        <>
          <Group grow>
            <TextInput
              label="title"
              value={manifest.title}
              onChange={(e) => {
                const title = e.currentTarget.value;
                const next = { ...manifest, title };
                if (mode === 'create' && !manifest.typeKey.trim()) {
                  next.typeKey = toSlug(title);
                }
                setManifest(next);
              }}
            />
            <TextInput
              label="description"
              value={manifest.description}
              onChange={(e) => setManifest({ ...manifest, description: e.currentTarget.value })}
            />
          </Group>
          <Group align="end" wrap="nowrap">
            <TextInput
              label="category"
              value={manifest.category ?? 'general'}
              onChange={(e) => setManifest({ ...manifest, category: e.currentTarget.value || 'general' })}
              style={{ width: 160 }}
            />
            <Select
              label="schemaVersion"
              data={[{ value: '1.0', label: '1.0' }]}
              value={manifest.schemaVersion ?? '1.0'}
              onChange={(value) => setManifest({ ...manifest, schemaVersion: value ?? '1.0' })}
              allowDeselect={false}
              style={{ width: 140 }}
            />
            <Switch
              label="enabled"
              checked={manifest.enabled}
              onChange={(e) => setManifest({ ...manifest, enabled: e.currentTarget.checked })}
            />
            <Switch
              label="mandatory"
              checked={manifest.mandatory}
              onChange={(e) => setManifest({ ...manifest, mandatory: e.currentTarget.checked })}
            />
            <Select
              label="targetCardinality"
              data={[
                { value: 'SINGLE', label: 'single' },
                { value: 'MULTIPLE', label: 'multiple' },
              ]}
              value={manifest.targetCardinality ?? 'SINGLE'}
              onChange={(value) => setManifest({ ...manifest, targetCardinality: (value as 'SINGLE' | 'MULTIPLE' | null) ?? 'SINGLE' })}
              allowDeselect={false}
              style={{ width: 180 }}
            />
          </Group>
          <Stack gap={4}>
            <Text size="sm">typeKey</Text>
            {(readOnly || mode === 'edit') ? (
              <Group
                gap={4}
                align="center"
                px={2}
                py={4}
                style={{ borderRadius: 6, width: 'fit-content' }}
                onMouseEnter={() => setShowTypeKeyCopy(true)}
                onMouseLeave={() => setShowTypeKeyCopy(false)}
              >
                <Text ff="monospace" size="sm">{manifest.typeKey || '-'}</Text>
                <Tooltip label="Copy to clipboard" withArrow disabled={!showTypeKeyCopy}>
                  <ActionIcon
                    variant="subtle"
                    size="sm"
                    onClick={() => void copyTypeKey()}
                    aria-label="Copy type key to clipboard"
                    style={{ opacity: showTypeKeyCopy ? 1 : 0, transition: 'opacity 120ms ease' }}
                  >
                    <HiOutlineClipboardDocument size={14} />
                  </ActionIcon>
                </Tooltip>
              </Group>
            ) : (
              <TextInput
                value={manifest.typeKey}
                onChange={(e) => setManifest({ ...manifest, typeKey: e.currentTarget.value })}
                placeholder="urn or slug"
              />
            )}
          </Stack>
          <Stack gap={4}>
            <Text size="sm">Applicable to (URNs/slugs, empty = any)</Text>
            <ApplicableToPills
              values={manifest.applicableTo ?? []}
              editable
              draftValue={applicableDraft}
              onDraftChange={setApplicableDraft}
              onAdd={addApplicable}
              onRemove={removeApplicable}
            />
          </Stack>

          <Divider label="Payload Schema" />
        <Group align="stretch" grow wrap="nowrap" style={{ minHeight: 520 }}>
          <Paper withBorder p="sm" style={{ flex: '1 1 25%', minWidth: 220 }}>
            <Group justify="space-between" mb="xs">
              <Text fw={600} size="sm">Payload</Text>
              <Tooltip label="Add field">
                <ActionIcon
                  size="sm"
                  variant="subtle"
                  onClick={() => {
                    const current = manifest.payload.fields ?? [];
                    setManifest({ ...manifest, payload: addFieldToObjectPath(manifest.payload, []) });
                    setSelectedPath([current.length]);
                  }}
                  disabled={manifest.payload.type !== 'OBJECT'}
                  aria-label="Add payload field"
                >
                  <HiOutlinePlus size={14} />
                </ActionIcon>
              </Tooltip>
            </Group>
            <ScrollArea h={470}>
              {(manifest.payload.fields ?? []).map((field, idx) => (
                <TreeNodeItem
                  key={`root.${idx}`}
                  root={manifest.payload}
                  path={[idx]}
                  label={field.name}
                  selectedPath={selectedPath}
                  onSelect={setSelectedPath}
                  onAddChild={(path) => {
                    setManifest({ ...manifest, payload: addFieldToObjectPath(manifest.payload, path) });
                  }}
                  onDelete={(path) => {
                    const parent = path.slice(0, -1);
                    const deleteIdx = path[path.length - 1] ?? -1;
                    setManifest({ ...manifest, payload: removeFieldAtParentPath(manifest.payload, parent, deleteIdx) });
                    setSelectedPath(parent);
                  }}
                  onMoveUp={(path) => {
                    const parent = path.slice(0, -1);
                    const moveIdx = path[path.length - 1] ?? -1;
                    setManifest({ ...manifest, payload: moveFieldAtParentPath(manifest.payload, parent, moveIdx, -1) });
                    if (moveIdx > 0) setSelectedPath([...parent, moveIdx - 1]);
                  }}
                  onMoveDown={(path) => {
                    const parent = path.slice(0, -1);
                    const moveIdx = path[path.length - 1] ?? -1;
                    setManifest({ ...manifest, payload: moveFieldAtParentPath(manifest.payload, parent, moveIdx, 1) });
                    setSelectedPath([...parent, moveIdx + 1]);
                  }}
                />
              ))}
            </ScrollArea>
          </Paper>
          <Paper withBorder p="sm" style={{ flex: '3 1 75%', minWidth: 0, width: '100%' }}>
            <Text fw={600} size="sm" mb="xs">Node Editor</Text>
            <ScrollArea h={470}>
              <Stack gap="xs">
                {selectedPath.length > 0 && selectedNode && (
                  <Group grow align="flex-start">
                    <TextInput
                      label="Field key"
                      value={selectedFieldName}
                      onChange={(e) => {
                        setManifest({
                          ...manifest,
                          payload: renameFieldAtPath(manifest.payload, selectedPath, e.currentTarget.value),
                        });
                      }}
                    />
                    <Switch
                      label="Field required"
                      checked={selectedFieldRequired}
                      onChange={(e) => {
                        setManifest({
                          ...manifest,
                          payload: setFieldRequiredAtPath(manifest.payload, selectedPath, e.currentTarget.checked),
                        });
                      }}
                    />
                  </Group>
                )}
                {!selectedNode && (
                  <Text size="sm" c="dimmed">Select a field in Payload to edit schema details.</Text>
                )}
                {selectedNode && (
                  <>
                <Group grow>
                  <Select
                    label="Type"
                    data={schemaTypeOptions}
                    value={selectedNode.type}
                    onChange={(value) => {
                      const next = (value as FacetSchemaType) || 'STRING';
                      setManifest({
                        ...manifest,
                        payload: updateNodeAtPath(
                          manifest.payload,
                          selectedPath,
                          (node) => normalizeSchemaByType(node, next)
                        ),
                      });
                    }}
                  />
                  <TextInput
                    label="Title"
                    value={selectedNode.title}
                    onChange={(e) => {
                      setManifest({
                        ...manifest,
                        payload: updateNodeAtPath(manifest.payload, selectedPath, (node) => ({ ...node, title: e.currentTarget.value })),
                      });
                    }}
                  />
                </Group>
                <TextInput
                  label="Description"
                  value={selectedNode.description}
                  onChange={(e) => {
                    setManifest({
                      ...manifest,
                      payload: updateNodeAtPath(manifest.payload, selectedPath, (node) => ({ ...node, description: e.currentTarget.value })),
                    });
                  }}
                />
                {selectedNode.type === 'STRING' && (
                  <Select
                    label="Format"
                    data={stringFormatOptions}
                    value={selectedNode.format ?? ''}
                    onChange={(value) => {
                      const normalized = (value ?? '').trim();
                      setManifest({
                        ...manifest,
                        payload: updateNodeAtPath(
                          manifest.payload,
                          selectedPath,
                          (node) => ({ ...node, format: normalized || undefined })
                        ),
                      });
                    }}
                    allowDeselect={false}
                  />
                )}
                {selectedNode.type === 'ENUM' && (
                  <Stack gap="xs">
                    <Text size="sm" fw={500}>Enum values</Text>
                    {(selectedNode.values ?? []).map((entry, idx) => (
                      <Group key={`${entry.value}-${idx}`} align="end" wrap="nowrap">
                        <TextInput
                          label={idx === 0 ? 'Value' : undefined}
                          value={entry.value}
                          onChange={(e) => {
                            const value = e.currentTarget.value;
                            const current = [...(selectedNode.values ?? [])];
                            if (!current[idx]) return;
                            current[idx] = { ...current[idx], value };
                            setManifest({
                              ...manifest,
                              payload: updateNodeAtPath(manifest.payload, selectedPath, (node) => ({ ...node, values: current })),
                            });
                          }}
                          style={{ flex: 1 }}
                        />
                        <TextInput
                          label={idx === 0 ? 'Description' : undefined}
                          value={entry.description}
                          onChange={(e) => {
                            const description = e.currentTarget.value;
                            const current = [...(selectedNode.values ?? [])];
                            if (!current[idx]) return;
                            current[idx] = { ...current[idx], description };
                            setManifest({
                              ...manifest,
                              payload: updateNodeAtPath(manifest.payload, selectedPath, (node) => ({ ...node, values: current })),
                            });
                          }}
                          style={{ flex: 2 }}
                        />
                        <ActionIcon
                          color="red"
                          variant="light"
                          onClick={() => {
                            const values = (selectedNode.values ?? []).filter((_, i) => i !== idx);
                            setManifest({
                              ...manifest,
                              payload: updateNodeAtPath(manifest.payload, selectedPath, (node) => ({ ...node, values })),
                            });
                          }}
                          aria-label="Remove enum value"
                          mb={idx === 0 ? 2 : 0}
                        >
                          <HiOutlineTrash size={14} />
                        </ActionIcon>
                      </Group>
                    ))}
                    <Group align="end" wrap="nowrap">
                      <TextInput
                        label="New value"
                        value={enumValueDraft}
                        onChange={(e) => setEnumValueDraft(e.currentTarget.value)}
                        style={{ flex: 1 }}
                      />
                      <TextInput
                        label="New description"
                        value={enumDescriptionDraft}
                        onChange={(e) => setEnumDescriptionDraft(e.currentTarget.value)}
                        style={{ flex: 2 }}
                      />
                      <Button
                        variant="light"
                        leftSection={<HiOutlinePlus size={14} />}
                        onClick={addEnumValue}
                        disabled={!enumValueDraft.trim() || !enumDescriptionDraft.trim()}
                      >
                        Add
                      </Button>
                    </Group>
                  </Stack>
                )}
                {selectedNode.type === 'OBJECT' && (
                  <TextInput
                    label="Required field keys (comma separated)"
                    value={(selectedNode.required ?? []).join(',')}
                    onChange={(e) => {
                      const required = e.currentTarget.value.split(',').map((v) => v.trim()).filter(Boolean);
                      setManifest({
                        ...manifest,
                        payload: updateNodeAtPath(manifest.payload, selectedPath, (node) => ({ ...ensureObjectShape(node), required })),
                      });
                    }}
                  />
                )}
                  </>
                )}
              </Stack>
            </ScrollArea>
          </Paper>
        </Group>
        </>
      )}
    </Stack>
  );
}

