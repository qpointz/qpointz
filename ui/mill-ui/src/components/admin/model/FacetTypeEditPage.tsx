import {
  type Dispatch,
  type SetStateAction,
  memo,
  useEffect,
  useLayoutEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import {
  ActionIcon,
  Box,
  Chip,
  Button,
  Group,
  Paper,
  ScrollArea,
  Select,
  Stack,
  Switch,
  Text,
  Textarea,
  TextInput,
  Tooltip,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { HiOutlineArrowDown, HiOutlineArrowUp, HiOutlineClipboardDocument, HiOutlinePlus, HiOutlineTrash } from 'react-icons/hi2';
import { useNavigate } from 'react-router';
import { facetTypeService } from '../../../services/api';
import type { FacetEnumValue, FacetPayloadSchema, FacetSchemaType, FacetTypeManifest } from '../../../types/facetTypes';
import { ApplicableToPills } from './ApplicableToPills';
import { StereotypeTagsPills } from './StereotypeTagsPills';
import { stereotypeTagsFromWire, stereotypeWireFromTags } from './stereotypeTags';
import { normalizeTargetValue } from './knownTargets';
import { JsonYamlEditor, type JsonYamlEditorHandle } from '../../common/JsonYamlEditor';
import { normalizeFacetTypeKeyForApi, slugifyFacetTypeTitle } from '../../../utils/urnSlug';
import { facetTypeManifestFromWire, facetTypeManifestToWire } from '../../../services/facetTypeWire';
import { facetTypeContentSchemaRequiresExpertMode } from '../../../utils/facetPayloadFormSupport';

interface FacetTypeEditPageProps {
  mode: 'create' | 'edit';
  typeKey?: string;
  readOnly: boolean;
}

/** Manifest fields excluding payload; kept separate so typing metadata does not re-render the payload tree. */
type FacetTypeManifestMeta = Omit<FacetTypeManifest, 'payload'>;

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

function getNodeAtPath(root: FacetPayloadSchema, path: NodePath): FacetPayloadSchema | null {
  let node: FacetPayloadSchema = root;
  for (const idx of path) {
    const fields = node.fields ?? [];
    if (idx < 0 || idx >= fields.length) return null;
    const child = fields[idx]?.schema;
    if (child == null) return null;
    node = child;
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

function getFieldStereotypeTagsAtPath(root: FacetPayloadSchema, path: NodePath): string[] {
  if (path.length === 0) return [];
  const parentPath = path.slice(0, -1);
  const fieldIndex = path[path.length - 1] ?? -1;
  if (fieldIndex < 0) return [];
  const parent = parentPath.length === 0 ? root : getNodeAtPath(root, parentPath);
  if (!parent || parent.type !== 'OBJECT') return [];
  const raw = parent.fields?.[fieldIndex]?.stereotype;
  return stereotypeTagsFromWire(raw);
}

function setFieldStereotypeTagsAtPath(root: FacetPayloadSchema, path: NodePath, tags: string[]): FacetPayloadSchema {
  if (path.length === 0) return root;
  const parentPath = path.slice(0, -1);
  const fieldIndex = path[path.length - 1] ?? -1;
  const nodeAtPath = getNodeAtPath(root, path);
  const valueSchemaType = nodeAtPath?.type ?? 'STRING';
  const wire = stereotypeWireFromTags(tags, valueSchemaType);
  return updateNodeAtPath(root, parentPath, (node) => {
    const objectNode = ensureObjectShape(node);
    const fields = [...(objectNode.fields ?? [])];
    const existing = fields[fieldIndex];
    if (!existing) return objectNode;
    fields[fieldIndex] = {
      ...existing,
      stereotype: wire,
    };
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

const TreeNodeItem = memo(function TreeNodeItem({
  root,
  path,
  label,
  selectedPath,
  onSelect,
  onAddChild,
  onDelete,
  onMoveUp,
  onMoveDown,
}: TreeNodeItemProps) {
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
});

interface FacetTypePayloadSectionProps {
  payload: FacetPayloadSchema;
  setPayload: Dispatch<SetStateAction<FacetPayloadSchema>>;
}

const FacetTypePayloadSection = memo(function FacetTypePayloadSection({
  payload,
  setPayload,
}: FacetTypePayloadSectionProps) {
  const [selectedPath, setSelectedPath] = useState<NodePath>([]);
  const [enumValueDraft, setEnumValueDraft] = useState('');
  const [enumDescriptionDraft, setEnumDescriptionDraft] = useState('');
  const [stereotypeDraft, setStereotypeDraft] = useState('');

  const selectedNode = useMemo(
    () => (selectedPath.length > 0 ? getNodeAtPath(payload, selectedPath) : null),
    [payload, selectedPath]
  );
  const selectedFieldName = useMemo(
    () => getFieldNameAtPath(payload, selectedPath) ?? '',
    [payload, selectedPath]
  );
  const selectedFieldRequired = useMemo(
    () => (selectedPath.length > 0 ? getFieldRequiredAtPath(payload, selectedPath) : true),
    [payload, selectedPath]
  );
  const selectedFieldStereotypeTags = useMemo(
    () => (selectedPath.length > 0 ? getFieldStereotypeTagsAtPath(payload, selectedPath) : []),
    [payload, selectedPath]
  );

  /** Reset inline drafts when the selected tree node changes (stereotype input, enum rows). */
  useEffect(() => {
    setStereotypeDraft('');
    setEnumValueDraft('');
    setEnumDescriptionDraft('');
  }, [selectedPath]);

  const addEnumValue = () => {
    if (!selectedNode || selectedNode.type !== 'ENUM') return;
    const value = enumValueDraft.trim();
    const description = enumDescriptionDraft.trim();
    if (!value || !description) return;
    const current = selectedNode.values ?? [];
    if (current.some((v) => v.value === value)) return;
    const values: FacetEnumValue[] = [...current, { value, description }];
    setPayload((p) => updateNodeAtPath(p, selectedPath, (node) => ({ ...node, values })));
    setEnumValueDraft('');
    setEnumDescriptionDraft('');
  };

  return (
    <Group align="stretch" grow wrap="nowrap" style={{ minHeight: 520 }}>
      <Paper withBorder p="sm" style={{ flex: '1 1 25%', minWidth: 220 }}>
        <Group justify="space-between" mb="xs">
          <Text fw={600} size="sm">
            Content Schema
          </Text>
          <Tooltip label="Add field">
            <ActionIcon
              size="sm"
              variant="subtle"
              onClick={() => {
                const current = payload.fields ?? [];
                setPayload((p) => addFieldToObjectPath(p, []));
                setSelectedPath([current.length]);
              }}
              disabled={payload.type !== 'OBJECT'}
              aria-label="Add content schema field"
            >
              <HiOutlinePlus size={14} />
            </ActionIcon>
          </Tooltip>
        </Group>
        <ScrollArea h={470}>
          {(payload.fields ?? []).map((field, idx) => (
            <TreeNodeItem
              key={`root.${idx}`}
              root={payload}
              path={[idx]}
              label={field.name}
              selectedPath={selectedPath}
              onSelect={setSelectedPath}
              onAddChild={(path) => {
                setPayload((p) => addFieldToObjectPath(p, path));
              }}
              onDelete={(path) => {
                const parent = path.slice(0, -1);
                const deleteIdx = path[path.length - 1] ?? -1;
                setPayload((p) => removeFieldAtParentPath(p, parent, deleteIdx));
                setSelectedPath(parent);
              }}
              onMoveUp={(path) => {
                const parent = path.slice(0, -1);
                const moveIdx = path[path.length - 1] ?? -1;
                setPayload((p) => moveFieldAtParentPath(p, parent, moveIdx, -1));
                if (moveIdx > 0) setSelectedPath([...parent, moveIdx - 1]);
              }}
              onMoveDown={(path) => {
                const parent = path.slice(0, -1);
                const moveIdx = path[path.length - 1] ?? -1;
                setPayload((p) => moveFieldAtParentPath(p, parent, moveIdx, 1));
                setSelectedPath([...parent, moveIdx + 1]);
              }}
            />
          ))}
        </ScrollArea>
      </Paper>
      <Paper withBorder p="sm" style={{ flex: '3 1 75%', minWidth: 0, width: '100%' }}>
        <Text fw={600} size="sm" mb="xs">
          Field Editor
        </Text>
        <ScrollArea h={470}>
          <Stack gap="xs">
            {selectedPath.length > 0 && selectedNode && (
              <Group grow align="flex-start">
                <TextInput
                  label="Field key"
                  value={selectedFieldName}
                  onChange={(e) => {
                    const v = e.currentTarget.value;
                    setPayload((p) => renameFieldAtPath(p, selectedPath, v));
                  }}
                />
                <Switch
                  label="Field required"
                  checked={selectedFieldRequired}
                  onChange={(e) => {
                    const checked = e.currentTarget.checked;
                    setPayload((p) => setFieldRequiredAtPath(p, selectedPath, checked));
                  }}
                />
              </Group>
            )}
            {selectedPath.length > 0 && selectedNode && (
              <Stack gap={6}>
                <Text size="sm" fw={500}>
                  Stereotype
                </Text>
                <StereotypeTagsPills
                  tags={selectedFieldStereotypeTags}
                  editable
                  draftValue={stereotypeDraft}
                  onDraftChange={setStereotypeDraft}
                  onAdd={(tag) => {
                    setPayload((p) => {
                      const cur = getFieldStereotypeTagsAtPath(p, selectedPath);
                      if (cur.includes(tag)) return p;
                      return setFieldStereotypeTagsAtPath(p, selectedPath, [...cur, tag]);
                    });
                  }}
                  onRemove={(tag) => {
                    setPayload((p) => {
                      const cur = getFieldStereotypeTagsAtPath(p, selectedPath);
                      return setFieldStereotypeTagsAtPath(
                        p,
                        selectedPath,
                        cur.filter((t) => t !== tag)
                      );
                    });
                  }}
                />
              </Stack>
            )}
            {!selectedNode && (
              <Text size="sm" c="dimmed">
                Select a field in Content Schema to edit schema details.
              </Text>
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
                      setPayload((p) => {
                        let u = updateNodeAtPath(p, selectedPath, (node) => normalizeSchemaByType(node, next));
                        const tags = getFieldStereotypeTagsAtPath(u, selectedPath);
                        u = setFieldStereotypeTagsAtPath(u, selectedPath, tags);
                        return u;
                      });
                    }}
                  />
                  <TextInput
                    label="Title"
                    value={selectedNode.title}
                    onChange={(e) => {
                      const v = e.currentTarget.value;
                      setPayload((p) =>
                        updateNodeAtPath(p, selectedPath, (node) => ({ ...node, title: v }))
                      );
                    }}
                  />
                </Group>
                <Textarea
                  label="Description"
                  value={selectedNode.description}
                  onChange={(e) => {
                    const v = e.currentTarget.value;
                    setPayload((p) =>
                      updateNodeAtPath(p, selectedPath, (node) => ({ ...node, description: v }))
                    );
                  }}
                  minRows={2}
                  autosize
                  maxRows={20}
                />
                {selectedNode.type === 'STRING' && (
                  <Select
                    label="Format"
                    data={stringFormatOptions}
                    value={selectedNode.format ?? ''}
                    onChange={(value) => {
                      const normalized = (value ?? '').trim();
                      setPayload((p) =>
                        updateNodeAtPath(p, selectedPath, (node) => ({
                          ...node,
                          format: normalized || undefined,
                        }))
                      );
                    }}
                    allowDeselect={false}
                  />
                )}
                {selectedNode.type === 'ENUM' && (
                  <Stack gap="xs">
                    <Text size="sm" fw={500}>
                      Enum values
                    </Text>
                    {(selectedNode.values ?? []).map((entry, idx) => {
                      if (entry == null || typeof entry !== 'object') return null;
                      const row = entry as FacetEnumValue;
                      return (
                      <Group key={`${row.value ?? 'enum'}-${idx}`} align="flex-start" wrap="nowrap">
                        <TextInput
                          label={idx === 0 ? 'Value' : undefined}
                          value={row.value ?? ''}
                          onChange={(e) => {
                            const v = e.currentTarget.value;
                            setPayload((p) =>
                              updateNodeAtPath(p, selectedPath, (node) => {
                                if (node.type !== 'ENUM') return node;
                                const current = [...(node.values ?? [])];
                                const prev = current[idx];
                                if (!prev || typeof prev !== 'object') return node;
                                current[idx] = { ...(prev as FacetEnumValue), value: v };
                                return { ...node, values: current };
                              })
                            );
                          }}
                          style={{ flex: 1 }}
                        />
                        <Textarea
                          label={idx === 0 ? 'Description' : undefined}
                          value={row.description ?? ''}
                          onChange={(e) => {
                            const d = e.currentTarget.value;
                            setPayload((p) =>
                              updateNodeAtPath(p, selectedPath, (node) => {
                                if (node.type !== 'ENUM') return node;
                                const current = [...(node.values ?? [])];
                                const prev = current[idx];
                                if (!prev || typeof prev !== 'object') return node;
                                current[idx] = { ...(prev as FacetEnumValue), description: d };
                                return { ...node, values: current };
                              })
                            );
                          }}
                          minRows={2}
                          autosize
                          maxRows={12}
                          style={{ flex: 2 }}
                        />
                        <ActionIcon
                          color="red"
                          variant="light"
                          onClick={() => {
                            setPayload((p) =>
                              updateNodeAtPath(p, selectedPath, (node) => {
                                if (node.type !== 'ENUM') return node;
                                const values = (node.values ?? []).filter((_, i) => i !== idx);
                                return { ...node, values };
                              })
                            );
                          }}
                          aria-label="Remove enum value"
                          mt={idx === 0 ? 28 : 4}
                        >
                          <HiOutlineTrash size={14} />
                        </ActionIcon>
                      </Group>
                      );
                    })}
                    <Group align="flex-start" wrap="nowrap">
                      <TextInput
                        label="New value"
                        value={enumValueDraft}
                        onChange={(e) => setEnumValueDraft(e.currentTarget.value)}
                        style={{ flex: 1 }}
                      />
                      <Textarea
                        label="New description"
                        value={enumDescriptionDraft}
                        onChange={(e) => setEnumDescriptionDraft(e.currentTarget.value)}
                        minRows={2}
                        autosize
                        maxRows={12}
                        style={{ flex: 2 }}
                      />
                      <Button
                        mt={24}
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
                      setPayload((p) =>
                        updateNodeAtPath(p, selectedPath, (node) => ({
                          ...ensureObjectShape(node),
                          required,
                        }))
                      );
                    }}
                  />
                )}
              </>
            )}
          </Stack>
        </ScrollArea>
      </Paper>
    </Group>
  );
});

export function FacetTypeEditPage({ mode, typeKey, readOnly }: FacetTypeEditPageProps) {
  const navigate = useNavigate();
  const [meta, setMeta] = useState<FacetTypeManifestMeta>(() => ({
    typeKey: '',
    title: '',
    description: '',
    category: 'general',
    enabled: true,
    mandatory: false,
    targetCardinality: 'SINGLE',
    applicableTo: [],
    schemaVersion: '1.0',
  }));
  const [payload, setPayload] = useState<FacetPayloadSchema>(() => defaultSchema('OBJECT'));
  const wireManifest = useMemo(() => ({ ...meta, payload }), [meta, payload]);
  const payloadRequiresExpert = useMemo(
    () => facetTypeContentSchemaRequiresExpertMode(payload),
    [payload]
  );

  const [expertMode, setExpertMode] = useState(false);
  const [expertDraft, setExpertDraft] = useState<{ valid: boolean; value?: unknown; error?: string } | null>(null);
  const expertEditorRef = useRef<JsonYamlEditorHandle>(null);
  const [applicableDraft, setApplicableDraft] = useState('');
  const [showTypeKeyCopy, setShowTypeKeyCopy] = useState(false);
  /** In create mode, title auto-fills typeKey until the user edits the key field (or clears it). */
  const typeKeyUserEditedRef = useRef(false);
  /** Tracks prior `expertMode` to seed `expertDraft` once when expert mode turns on. */
  const expertModePrevRef = useRef(false);

  const applyLoadedManifest = (data: FacetTypeManifest) => {
    const { payload: pl, ...rest } = data;
    setMeta(rest);
    setPayload(pl);
  };

  useEffect(() => {
    if (mode !== 'edit' || !typeKey) return;
    void facetTypeService.get(typeKey).then(applyLoadedManifest).catch((e) => {
      notifications.show({
        color: 'red',
        title: 'Failed to load facet type',
        message: e instanceof Error ? e.message : 'Unknown error',
      });
    });
  }, [mode, typeKey]);

  useLayoutEffect(() => {
    if (payloadRequiresExpert) {
      setExpertMode(true);
    }
  }, [payloadRequiresExpert]);

  useEffect(() => {
    if (expertMode) {
      if (!expertModePrevRef.current) {
        setExpertDraft({ valid: true, value: facetTypeManifestToWire(wireManifest) });
      }
      expertModePrevRef.current = true;
    } else {
      expertModePrevRef.current = false;
    }
  }, [expertMode, wireManifest]);

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

  /** Prefer synchronous parse from the expert editor (YAML-safe); fall back to debounced draft if ref unset. */
  const resolveExpertWireFromEditor = (): { ok: true; value: unknown } | { ok: false; error: string } => {
    const sync = expertEditorRef.current?.getParsedForSubmit();
    if (sync) return sync;
    if (!expertDraft?.valid || expertDraft.value === undefined) {
      return { ok: false, error: expertDraft?.error ?? 'Fix JSON/YAML before saving.' };
    }
    return { ok: true, value: expertDraft.value };
  };

  const save = async () => {
    let manifestToSave: FacetTypeManifest = wireManifest;
    if (expertMode) {
      const resolved = resolveExpertWireFromEditor();
      if (!resolved.ok) {
        notifications.show({
          color: 'red',
          title: 'Invalid expert document',
          message: resolved.error,
        });
        return;
      }
      try {
        manifestToSave = facetTypeManifestFromWire(resolved.value);
      } catch (e) {
        notifications.show({
          color: 'red',
          title: 'Invalid expert document',
          message: e instanceof Error ? e.message : 'Manifest must include facetTypeUrn/typeKey, title, and contentSchema/payload.',
        });
        return;
      }
    }
    const titleSlug = slugifyFacetTypeTitle(manifestToSave.title);
    const fallbackKey = titleSlug.length > 0 ? titleSlug : 'facet-type';
    const rawKey = manifestToSave.typeKey.trim() || fallbackKey;
    let resolvedTypeKey: string;
    try {
      resolvedTypeKey = normalizeFacetTypeKeyForApi(rawKey);
    } catch {
      notifications.show({
        color: 'red',
        title: 'Invalid type key',
        message: 'typeKey is empty after trimming. Set a title or enter a local key / URN.',
      });
      return;
    }
    manifestToSave = { ...manifestToSave, typeKey: resolvedTypeKey };
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

  const addApplicable = (raw: string) => {
    const value = normalizeTargetValue(raw);
    if (!value) return;
    setMeta((m) => {
      const current = m.applicableTo ?? [];
      if (current.includes(value)) return m;
      return { ...m, applicableTo: [...current, value] };
    });
  };

  const removeApplicable = (value: string) => {
    setMeta((m) => ({
      ...m,
      applicableTo: (m.applicableTo ?? []).filter((v) => v !== value),
    }));
  };

  const copyTypeKey = async () => {
    try {
      await navigator.clipboard.writeText(meta.typeKey);
      notifications.show({ color: 'green', title: 'Copied', message: 'Facet type URN copied to clipboard.' });
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
                const resolved = resolveExpertWireFromEditor();
                if (!resolved.ok) {
                  notifications.show({
                    color: 'red',
                    title: 'Invalid expert document',
                    message: resolved.error,
                  });
                  return;
                }
                let loaded: FacetTypeManifest;
                try {
                  loaded = facetTypeManifestFromWire(resolved.value);
                } catch (e) {
                  notifications.show({
                    color: 'red',
                    title: 'Invalid manifest',
                    message: e instanceof Error ? e.message : 'Could not map expert JSON to a facet type.',
                  });
                  return;
                }
                if (facetTypeContentSchemaRequiresExpertMode(loaded.payload)) {
                  notifications.show({
                    color: 'yellow',
                    title: 'Expert mode required',
                    message:
                      'Content schema uses an array of objects or nested arrays that the form tree cannot edit. Simplify the schema or stay in expert JSON/YAML.',
                  });
                  return;
                }
                applyLoadedManifest(loaded);
                setExpertMode(false);
                return;
              }
              setExpertMode(true);
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
      {payloadRequiresExpert && (
        <Text size="xs" c="dimmed">
          Expert JSON/YAML stays on for this type: the form tree cannot edit array-of-object or nested-array item
          schemas (use expert mode to avoid losing structure).
        </Text>
      )}

      {expertMode ? (
        <JsonYamlEditor
          ref={expertEditorRef}
          value={facetTypeManifestToWire(wireManifest)}
          onApply={(next) => {
            try {
              applyLoadedManifest(facetTypeManifestFromWire(next));
            } catch (e) {
              notifications.show({
                color: 'red',
                title: 'Invalid manifest',
                message: e instanceof Error ? e.message : 'Expected facet type keys (facetTypeUrn, title, contentSchema).',
              });
              return;
            }
            notifications.show({ color: 'green', title: 'Applied', message: 'Manifest updated from expert mode.' });
          }}
          onDraftParsed={setExpertDraft}
          minHeight={360}
        />
      ) : (
        <Stack gap="md">
          <Stack gap="xs">
            <TextInput
              label="Title"
              value={meta.title}
              onChange={(e) => {
                const title = e.currentTarget.value;
                setMeta((m) => {
                  const next = { ...m, title };
                  if (mode === 'create' && !typeKeyUserEditedRef.current) {
                    next.typeKey = slugifyFacetTypeTitle(title);
                  }
                  return next;
                });
              }}
            />
          </Stack>

          <Group align="flex-end" wrap="wrap" gap="md">
            <TextInput
              label="Category"
              value={meta.category ?? 'general'}
              onChange={(e) => setMeta({ ...meta, category: e.currentTarget.value || 'general' })}
              style={{ flex: '1 1 140px', minWidth: 120, maxWidth: 220 }}
            />
            <Select
              label="Target cardinality"
              data={[
                { value: 'SINGLE', label: 'single' },
                { value: 'MULTIPLE', label: 'multiple' },
              ]}
              value={meta.targetCardinality ?? 'SINGLE'}
              onChange={(value) =>
                setMeta({
                  ...meta,
                  targetCardinality: (value as 'SINGLE' | 'MULTIPLE' | null) ?? 'SINGLE',
                })
              }
              allowDeselect={false}
              style={{ width: 180 }}
            />
            <Switch
              label="Enabled"
              checked={meta.enabled}
              onChange={(e) => setMeta({ ...meta, enabled: e.currentTarget.checked })}
            />
            <Switch
              label="Mandatory"
              checked={meta.mandatory}
              onChange={(e) => setMeta({ ...meta, mandatory: e.currentTarget.checked })}
            />
            <Select
              label="Schema version"
              data={[{ value: '1.0', label: '1.0' }]}
              value={meta.schemaVersion ?? '1.0'}
              onChange={(value) => setMeta({ ...meta, schemaVersion: value ?? '1.0' })}
              allowDeselect={false}
              style={{ width: 140 }}
            />
          </Group>

          <Textarea
            label="Description"
            placeholder="Human-readable summary of this facet type"
            value={meta.description}
            onChange={(e) => setMeta({ ...meta, description: e.currentTarget.value })}
            minRows={2}
            autosize
            maxRows={24}
          />

          <Stack gap={6}>
            <Text size="sm" fw={500}>
              URN
            </Text>
            {(readOnly || mode === 'edit') ? (
              <Group
                gap={4}
                align="center"
                px={2}
                py={4}
                wrap="nowrap"
                style={{ borderRadius: 6, maxWidth: '100%' }}
                onMouseEnter={() => setShowTypeKeyCopy(true)}
                onMouseLeave={() => setShowTypeKeyCopy(false)}
              >
                <Text ff="monospace" size="sm" style={{ wordBreak: 'break-all' }}>
                  {meta.typeKey || '—'}
                </Text>
                <Tooltip label="Copy URN" withArrow disabled={!showTypeKeyCopy}>
                  <ActionIcon
                    variant="subtle"
                    size="sm"
                    onClick={() => void copyTypeKey()}
                    aria-label="Copy facet type URN"
                    style={{ opacity: showTypeKeyCopy ? 1 : 0, transition: 'opacity 120ms ease', flexShrink: 0 }}
                  >
                    <HiOutlineClipboardDocument size={14} />
                  </ActionIcon>
                </Tooltip>
              </Group>
            ) : (
              <TextInput
                label="Local id or full URN"
                value={meta.typeKey}
                onChange={(e) => {
                  const v = e.currentTarget.value;
                  typeKeyUserEditedRef.current = v.trim().length > 0;
                  setMeta({ ...meta, typeKey: v });
                }}
                description="Bare ids are normalized to urn:mill/metadata/facet-type:… on save."
                placeholder="e.g. my-facet or full URN"
              />
            )}
          </Stack>

          <Stack gap={4}>
            <Text size="sm" fw={500}>
              Applicable to (URNs / slugs, empty = any)
            </Text>
            <ApplicableToPills
              values={meta.applicableTo ?? []}
              editable
              draftValue={applicableDraft}
              onDraftChange={setApplicableDraft}
              onAdd={addApplicable}
              onRemove={removeApplicable}
            />
          </Stack>

          <FacetTypePayloadSection payload={payload} setPayload={setPayload} />
        </Stack>
      )}
    </Stack>
  );
}

