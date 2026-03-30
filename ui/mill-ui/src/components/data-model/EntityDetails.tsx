import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  Anchor,
  Box,
  Text,
  Badge,
  Group,
  useMantineColorScheme,
  Button,
  Card,
  Stack,
  Textarea,
  Switch,
  TextInput,
  NumberInput,
  Chip,
  Menu,
  ActionIcon,
  Select,
  Tooltip,
  Divider,
  Tabs,
  Modal,
  TagsInput,
  Alert,
} from '@mantine/core';
import {
  HiOutlineCircleStack,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiOutlineDocumentText,
  HiOutlineCube,
  HiOutlineArrowsRightLeft,
  HiOutlinePencilSquare,
  HiOutlinePlus,
  HiOutlineTrash,
  HiOutlineChevronDown,
  HiOutlineChevronRight,
  HiOutlineInformationCircle,
  HiOutlineArrowTopRightOnSquare,
  HiOutlineEnvelope,
} from 'react-icons/hi2';
import type { EntityFacets, SchemaEntity } from '../../types/schema';
import { StructuralFacet } from './facets/StructuralFacet';
import { InlineChatButton } from '../common/InlineChatButton';
import { RelatedContentButton } from '../common/RelatedContentButton';
import { JsonYamlEditor } from '../common/JsonYamlEditor';
import { SyntaxCodeEditor } from '../common/SyntaxCodeEditor';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { notifications } from '@mantine/notifications';
import { facetTypeService, metadataEntityUrnForFacetApi, schemaService } from '../../services/api';
import type { FacetTypeManifest } from '../../types/facetTypes';
import type { FacetPayloadSchema } from '../../types/facetTypes';
import {
  facetEmailLooksValid,
  facetHyperlinkHref,
  facetMailtoHref,
  facetStringStereotype,
  facetTagsPresentationActive,
  facetHyperlinkPresentationActive,
  type FacetStringStereotypeKind,
} from '../../utils/facetStereotype';
import {
  type FacetRenderUnit,
  multipleFacetItemValues,
  multipleInstanceCaption,
} from './facetPayloadUtils';
import {
  effectiveFacetPayloadSchemaForEdit,
  facetPayloadSchemaFormSupported,
} from '../../utils/facetPayloadFormSupport';

interface EntityDetailsProps {
  entity: SchemaEntity;
  facets: EntityFacets;
  selectedContext?: string;
  onFacetsChanged?: () => Promise<void>;
}

const entityIcons = {
  SCHEMA: HiOutlineCircleStack,
  TABLE: HiOutlineTableCells,
  COLUMN: HiOutlineViewColumns,
};

const entityLabels = {
  SCHEMA: 'Schema',
  TABLE: 'Table',
  COLUMN: 'Column',
};

/** Title for a facet card header when registry metadata is still loading or missing for this type key. */
function facetBoxBaseTitle(
  facetType: string,
  titleByKey: Record<string, string>,
  descriptor: FacetTypeManifest | null
): string {
  const fromRegistry = titleByKey[facetType]?.trim();
  if (fromRegistry) return fromRegistry;
  const payloadTitle = descriptor?.payload?.title?.trim();
  if (payloadTitle) return payloadTitle;
  const slug = facetType.replace('urn:mill/metadata/facet-type:', '');
  if (!slug) return facetType;
  return slug.charAt(0).toUpperCase() + slug.slice(1).toLowerCase();
}

/**
 * Read-only display for hyperlink stereotype: plain string = URL text + href; object = `title` (optional) + `href`
 * (required). Opens in a new tab with an external-link icon.
 */
function FacetHyperlinkReadOnly({ value }: { value: unknown }) {
  const linkRow = (href: string, linkText: string) => (
    <Group gap={6} wrap="nowrap" align="flex-start">
      <Anchor
        href={href}
        target="_blank"
        rel="noopener noreferrer"
        size="sm"
        style={{ wordBreak: 'break-all', lineHeight: 1.35 }}
      >
        {linkText}
      </Anchor>
      <HiOutlineArrowTopRightOnSquare size={14} aria-hidden style={{ flexShrink: 0, marginTop: 2, opacity: 0.7 }} />
    </Group>
  );

  if (value != null && typeof value === 'object' && !Array.isArray(value)) {
    const o = value as Record<string, unknown>;
    const hrefRaw = o.href;
    const titleRaw = o.title;
    const hrefStr =
      hrefRaw == null ? '' : typeof hrefRaw === 'string' ? hrefRaw.trim() : String(hrefRaw).trim();
    if (!hrefStr) {
      return (
        <Alert color="red" variant="light" py={4} px="sm" fz="xs" role="alert">
          wrong link
        </Alert>
      );
    }
    const href = facetHyperlinkHref(hrefStr);
    const titleStr =
      titleRaw == null
        ? ''
        : typeof titleRaw === 'string'
          ? titleRaw.trim()
          : String(titleRaw).trim();
    /** Show Title + URL explicitly so optional label is visible in model view (not only anchor text). */
    return (
      <Stack gap={6}>
        <Group gap="sm" wrap="nowrap" align="flex-start">
          <Text size="xs" c="dimmed" w={44} style={{ flexShrink: 0 }}>
            Title
          </Text>
          <Text size="sm" style={{ flex: 1, wordBreak: 'break-word', lineHeight: 1.35 }}>
            {titleStr.length > 0 ? titleStr : '—'}
          </Text>
        </Group>
        <Group gap="sm" wrap="nowrap" align="flex-start">
          <Text size="xs" c="dimmed" w={44} style={{ flexShrink: 0 }}>
            URL
          </Text>
          <Box style={{ flex: 1, minWidth: 0 }}>{linkRow(href, hrefStr)}</Box>
        </Group>
      </Stack>
    );
  }

  const s = typeof value === 'string' ? value.trim() : '';
  if (!s) {
    return (
      <Text size="sm" c="dimmed">
        —
      </Text>
    );
  }
  const href = facetHyperlinkHref(s);
  return linkRow(href, s);
}

/** Read-only display for STRING fields with `email` stereotype: mailto link plus envelope glyph. */
function FacetEmailReadOnly({ value }: { value: unknown }) {
  const s = typeof value === 'string' ? value.trim() : '';
  if (!s) {
    return (
      <Text size="sm" c="dimmed">
        —
      </Text>
    );
  }
  const href = facetMailtoHref(s);
  return (
    <Group gap={6} wrap="nowrap" align="flex-start">
      <Anchor href={href} size="sm" style={{ wordBreak: 'break-all', lineHeight: 1.35 }}>
        {s}
      </Anchor>
      <HiOutlineEnvelope size={14} aria-hidden style={{ flexShrink: 0, marginTop: 2, opacity: 0.7 }} />
    </Group>
  );
}

/** Appends errors for non-empty values that fail email format when stereotype is `email` (STRING or STRING[]). */
function appendEmailStereotypeValidationErrors(
  schema: FacetPayloadSchema,
  value: unknown,
  errors: string[],
  path: string
): void {
  if (schema.type !== 'OBJECT') return;
  const obj = (value && typeof value === 'object' ? value : {}) as Record<string, unknown>;
  (schema.fields ?? []).forEach((field) => {
    const nextPath = path ? `${path}.${field.name}` : field.name;
    const fv = obj[field.name];
    if (facetStringStereotype(field.schema, field.stereotype) === 'email') {
      if (field.schema.type === 'STRING') {
        const trimmed = String(fv ?? '').trim();
        if (trimmed.length > 0 && !facetEmailLooksValid(trimmed)) {
          errors.push(`${nextPath} must be a valid email address`);
        }
      } else if (field.schema.type === 'ARRAY' && field.schema.items?.type === 'STRING') {
        const arr = Array.isArray(fv) ? fv : [];
        arr.forEach((item, i) => {
          const trimmed = String(item ?? '').trim();
          if (trimmed.length > 0 && !facetEmailLooksValid(trimmed)) {
            errors.push(`${nextPath}[${i}] must be a valid email address`);
          }
        });
      }
    }
    if (field.schema.type === 'OBJECT' && fv != null && typeof fv === 'object' && !Array.isArray(fv)) {
      appendEmailStereotypeValidationErrors(field.schema, fv, errors, nextPath);
    }
    if (field.schema.type === 'ARRAY' && Array.isArray(fv) && field.schema.items?.type === 'OBJECT') {
      fv.forEach((el, i) => {
        if (el != null && typeof el === 'object') {
          appendEmailStereotypeValidationErrors(field.schema.items!, el, errors, `${nextPath}[${i}]`);
        }
      });
    }
  });
}

function facetHyperlinkObjectHrefMissing(value: unknown): boolean {
  if (value == null || typeof value !== 'object' || Array.isArray(value)) return true;
  const href = (value as Record<string, unknown>).href;
  const s = href == null ? '' : typeof href === 'string' ? href.trim() : String(href).trim();
  return s.length === 0;
}

/** Appends errors for missing `href` on OBJECT / ARRAY-of-OBJECT values with hyperlink stereotype. */
function appendHyperlinkStereotypeValidationErrors(
  schema: FacetPayloadSchema,
  value: unknown,
  errors: string[],
  path: string
): void {
  if (schema.type !== 'OBJECT') return;
  const obj = (value && typeof value === 'object' ? value : {}) as Record<string, unknown>;
  (schema.fields ?? []).forEach((field) => {
    const nextPath = path ? `${path}.${field.name}` : field.name;
    const fv = obj[field.name];
    if (facetHyperlinkPresentationActive(field.schema, field.stereotype)) {
      if (field.schema.type === 'OBJECT') {
        if (facetHyperlinkObjectHrefMissing(fv)) {
          errors.push(`${nextPath}: wrong link`);
        }
      } else if (field.schema.type === 'ARRAY' && field.schema.items?.type === 'OBJECT') {
        const arr = Array.isArray(fv) ? fv : [];
        arr.forEach((item, i) => {
          if (facetHyperlinkObjectHrefMissing(item)) {
            errors.push(`${nextPath}[${i}]: wrong link`);
          }
        });
      }
    }
    if (field.schema.type === 'OBJECT' && fv != null && typeof fv === 'object' && !Array.isArray(fv)) {
      appendHyperlinkStereotypeValidationErrors(field.schema, fv, errors, nextPath);
    }
    if (field.schema.type === 'ARRAY' && Array.isArray(fv) && field.schema.items?.type === 'OBJECT') {
      fv.forEach((el, i) => {
        if (el != null && typeof el === 'object') {
          appendHyperlinkStereotypeValidationErrors(field.schema.items!, el, errors, `${nextPath}[${i}]`);
        }
      });
    }
  });
}

/** Required-field validation for facet payloads: walks OBJECT trees and ARRAY-of-OBJECT elements. */
function appendFacetPayloadRequiredErrors(
  schema: FacetPayloadSchema,
  value: unknown,
  errors: string[]
): void {
  const isBlank = (s: FacetPayloadSchema, v: unknown): boolean => {
    if (v == null) return true;
    if (s.type === 'STRING' || s.type === 'ENUM') {
      return String(v).trim().length === 0;
    }
    if (s.type === 'ARRAY') {
      return !Array.isArray(v) || v.length === 0;
    }
    if (s.type === 'NUMBER') {
      return typeof v !== 'number' || Number.isNaN(v);
    }
    return false;
  };
  const walkObject = (s: FacetPayloadSchema, v: unknown, path: string): void => {
    if (s.type !== 'OBJECT') return;
    const obj = (v && typeof v === 'object' && !Array.isArray(v) ? v : {}) as Record<string, unknown>;
    (s.fields ?? []).forEach((field) => {
      const required = field.required ?? true;
      const nextPath = path ? `${path}.${field.name}` : field.name;
      const fieldValue = obj[field.name];
      if (required && isBlank(field.schema, fieldValue)) {
        errors.push(`${nextPath} is required`);
      }
      if (field.schema.type === 'OBJECT' && fieldValue != null && typeof fieldValue === 'object' && !Array.isArray(fieldValue)) {
        walkObject(field.schema, fieldValue, nextPath);
      }
      if (field.schema.type === 'ARRAY' && field.schema.items?.type === 'OBJECT' && Array.isArray(fieldValue)) {
        fieldValue.forEach((el, i) => {
          const row = `${nextPath}[${i}]`;
          if (el != null && typeof el === 'object' && !Array.isArray(el)) {
            walkObject(field.schema.items!, el, row);
          } else if (required) {
            errors.push(`${row} must be an object`);
          }
        });
      }
    });
  };
  walkObject(schema, value, '');
}

export function EntityDetails({
  entity,
  facets,
  selectedContext = 'global',
  onFacetsChanged = async () => {},
}: EntityDetailsProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const Icon = entityIcons[entity.entityType];
  const entityName = entity.entityType === 'SCHEMA'
    ? entity.schemaName
    : entity.entityType === 'TABLE'
      ? entity.tableName
      : entity.columnName;
  const metadataFacetTargetId = metadataEntityUrnForFacetApi(entity);
  const canMutateMetadataFacets = metadataFacetTargetId !== null;
  const hasStructural = flags.modelStructuralFacet && facets.structural && Object.keys(facets.structural).length > 0;

  const baseTypeLabel = entity.entityType === 'COLUMN' ? entity.type.type : undefined;
  const [editFacetType, setEditFacetType] = useState<string | null>(null);
  /** When set, {@link editFacetType} refers to one element of a MULTIPLE payload. */
  const [editInstanceIndex, setEditInstanceIndex] = useState<number | null>(null);
  const [editMode, setEditMode] = useState<'form' | 'json'>('form');
  const [editJson, setEditJson] = useState('');
  /** Latest parse of the expert JSON/YAML editor (debounced inside [JsonYamlEditor]). */
  const [jsonExpertDraft, setJsonExpertDraft] = useState<{
    valid: boolean;
    value?: unknown;
    error?: string;
  } | null>(null);
  const [editSnapshot, setEditSnapshot] = useState('');
  const [editFormValue, setEditFormValue] = useState<unknown>({});
  const editFormValueRef = useRef(editFormValue);
  const editJsonRef = useRef(editJson);
  editFormValueRef.current = editFormValue;
  editJsonRef.current = editJson;

  const [facetTypes, setFacetTypes] = useState<FacetTypeManifest[]>([]);
  const [activeCategoryTab, setActiveCategoryTab] = useState<string | null>(null);
  const [facetToDelete, setFacetToDelete] = useState<{ facetType: string; instanceIndex: number | null } | null>(null);

  const allByType = facets.byType ?? {};
  const relationFacetKey = 'urn:mill/metadata/facet-type:relation';
  const relationCount = allByType[relationFacetKey] !== undefined
    ? multipleFacetItemValues(allByType[relationFacetKey]).length
    : (facets.relations?.length ?? 0);

  const orderedFacetTypes = useMemo(() => {
    const keys = Object.keys(allByType);
    const defaults = [
      'urn:mill/metadata/facet-type:descriptive',
      'urn:mill/metadata/facet-type:structural',
      'urn:mill/metadata/facet-type:relation',
    ];
    const priority = defaults.filter((k) => keys.includes(k));
    const rest = keys.filter((k) => !defaults.includes(k)).sort((a, b) => a.localeCompare(b));
    return [...priority, ...rest];
  }, [allByType]);

  const descriptorByType = useMemo(() => {
    const map: Record<string, FacetTypeManifest> = {};
    for (const ft of facetTypes) map[ft.typeKey] = ft;
    return map;
  }, [facetTypes]);

  const normalizeCategory = (raw?: string): string => {
    const value = (raw ?? '').trim();
    return value ? value.toLowerCase() : 'general';
  };

  const formatCategoryLabel = (category: string): string => {
    const spaced = category.replace(/-/g, ' ');
    return spaced.length === 0 ? 'General' : `${spaced.charAt(0).toUpperCase()}${spaced.slice(1)}`;
  };

  /**
   * Per-category list of UI units: one card per SINGLE facet, one card per MULTIPLE instance,
   * or one "empty" card when a MULTIPLE facet has no rows yet.
   */
  const facetUnitsByCategory = useMemo(() => {
    const grouped: Record<string, FacetRenderUnit[]> = {};
    for (const typeKey of orderedFacetTypes) {
      const descriptor = descriptorByType[typeKey];
      const category = normalizeCategory(descriptor?.category);
      let categoryUnits = grouped[category];
      if (!categoryUnits) {
        categoryUnits = [];
        grouped[category] = categoryUnits;
      }

      const isMultiple = (descriptor?.targetCardinality ?? 'SINGLE') === 'MULTIPLE';
      /** MULTIPLE facets use per-instance cards; do not gate on `descriptor.payload` (schema may load late or be absent). */
      const splitValues = isMultiple ? multipleFacetItemValues(allByType[typeKey]) : null;

      if (splitValues !== null) {
        if (splitValues.length === 0) {
          categoryUnits.push({ kind: 'multipleEmpty', facetType: typeKey });
        } else {
          splitValues.forEach((_, idx) => {
            categoryUnits.push({
              kind: 'multipleRow',
              facetType: typeKey,
              index: idx,
              total: splitValues.length,
            });
          });
        }
      } else {
        categoryUnits.push({ kind: 'single', facetType: typeKey });
      }
    }
    const orderedCategories = Object.keys(grouped).sort((a, b) => {
      if (a === 'general') return -1;
      if (b === 'general') return 1;
      return a.localeCompare(b);
    });
    return { grouped, orderedCategories };
  }, [orderedFacetTypes, descriptorByType, allByType]);

  useEffect(() => {
    const categories = facetUnitsByCategory.orderedCategories;
    if (categories.length === 0) {
      setActiveCategoryTab(null);
      return;
    }
    if (!activeCategoryTab || !categories.includes(activeCategoryTab)) {
      setActiveCategoryTab(categories[0] ?? null);
    }
  }, [facetUnitsByCategory, activeCategoryTab]);

  useEffect(() => {
    const ac = new AbortController();
    const { signal } = ac;
    const targetType =
      entity.entityType === 'SCHEMA'
        ? 'schema'
        : entity.entityType === 'TABLE'
          ? 'table'
          : 'attribute';
    void facetTypeService
      .list({ targetType, enabledOnly: true }, signal)
      .then((rows) => {
        if (!signal.aborted) setFacetTypes(rows);
      })
      .catch((e) => {
        if (e instanceof DOMException && e.name === 'AbortError') return;
        setFacetTypes([]);
      });
    return () => {
      ac.abort();
    };
  }, [entity.entityType]);

  const facetTypeTitleByKey = useMemo(() => {
    const map: Record<string, string> = {};
    for (const ft of facetTypes) {
      const title = ft.title?.trim();
      if (title) map[ft.typeKey] = title;
    }
    return map;
  }, [facetTypes]);

  /** Facet types the user can add, grouped by manifest category (`general` is the default bucket). */
  const addFacetMenu = useMemo(() => {
    const rows = facetTypes
      .filter((t) => {
        const exists = Boolean(allByType[t.typeKey]);
        const cardinality = t.targetCardinality ?? 'SINGLE';
        return !(exists && cardinality === 'SINGLE');
      })
      .map((t) => ({
        value: t.typeKey,
        label: t.title,
        category: normalizeCategory(t.category),
      }));

    const byCategory: Record<string, typeof rows> = {};
    for (const row of rows) {
      const list = byCategory[row.category] ?? [];
      list.push(row);
      byCategory[row.category] = list;
    }

    const nonGeneralCategories = Object.keys(byCategory)
      .filter((c) => c !== 'general')
      .sort((a, b) => a.localeCompare(b));

    const generalList = byCategory.general ?? [];
    const defaultTypeKey =
      generalList[0]?.value
      ?? nonGeneralCategories.map((c) => byCategory[c]?.[0]?.value).find(Boolean);

    return {
      byCategory,
      nonGeneralCategories,
      generalList,
      defaultTypeKey,
      isEmpty: rows.length === 0,
    };
  }, [facetTypes, allByType]);

  const addFacetByType = async (facetType: string) => {
    if (!metadataFacetTargetId) {
      notifications.show({
        color: 'yellow',
        title: 'No metadata binding',
        message: 'This object has no metadata entity URN; facets cannot be edited until it is bound.',
      });
      return;
    }
    try {
      const descriptor = facetTypes.find((f) => f.typeKey === facetType);
      const isMultiple = (descriptor?.targetCardinality ?? 'SINGLE') === 'MULTIPLE';
      const existing = allByType[facetType];
      /** MULTIPLE: POST one `{}` per new facet row; updates use PATCH with the row uid from GET facets. */
      const nextIndex =
        isMultiple && existing !== undefined ? multipleFacetItemValues(existing).length : 0;
      await schemaService.setEntityFacet(metadataFacetTargetId, facetType, selectedContext, {});
      await onFacetsChanged();
      const d = facetTypes.find((f) => f.typeKey === facetType);
      setActiveCategoryTab(normalizeCategory(d?.category));
      if (isMultiple) {
        openEdit(facetType, nextIndex, {});
      } else {
        openEdit(facetType);
      }
    } catch (e) {
      notifications.show({ color: 'red', title: 'Add facet failed', message: e instanceof Error ? e.message : 'Unknown error' });
    }
  };

  const addAnotherMultipleInstance = async (facetType: string) => {
    if (!metadataFacetTargetId) return;
    try {
      const nextIndex = multipleFacetItemValues(allByType[facetType]).length;
      await schemaService.setEntityFacet(metadataFacetTargetId, facetType, selectedContext, {});
      await onFacetsChanged();
      openEdit(facetType, nextIndex, {});
    } catch (e) {
      notifications.show({ color: 'red', title: 'Add entry failed', message: e instanceof Error ? e.message : 'Unknown error' });
    }
  };

  /**
   * @param itemHint optional value for this instance when `allByType` may not have refreshed yet after an async save
   */
  const openEdit = (facetType: string, instanceIndex?: number, itemHint?: unknown) => {
    const descriptor = facetTypes.find((f) => f.typeKey === facetType);
    const isMultiple = (descriptor?.targetCardinality ?? 'SINGLE') === 'MULTIPLE';
    if (isMultiple && typeof instanceIndex === 'number') {
      const items = multipleFacetItemValues(allByType[facetType]);
      const item = itemHint !== undefined ? itemHint : (items[instanceIndex] ?? {});
      const text = JSON.stringify(item, null, 2);
      const eff = effectiveFacetPayloadSchemaForEdit(descriptor, instanceIndex);
      const useForm = eff != null && facetPayloadSchemaFormSupported(eff);
      setEditFacetType(facetType);
      setEditInstanceIndex(instanceIndex);
      setEditMode(useForm ? 'form' : 'json');
      setEditJson(text);
      setEditSnapshot(text);
      setEditFormValue(item);
      setJsonExpertDraft({ valid: true, value: item });
      return;
    }
    const payload = allByType[facetType] ?? (isMultiple ? [] : {});
    const text = JSON.stringify(payload, null, 2);
    const eff = effectiveFacetPayloadSchemaForEdit(descriptor, null);
    const useForm = eff != null && facetPayloadSchemaFormSupported(eff);
    setEditFacetType(facetType);
    setEditInstanceIndex(null);
    setEditMode(useForm ? 'form' : 'json');
    setEditJson(text);
    setEditSnapshot(text);
    setEditFormValue(payload);
    setJsonExpertDraft({ valid: true, value: payload });
  };

  /** Restore form and expert editor payload to the snapshot from when Edit was opened. */
  const resetFacetEditToSnapshot = useCallback(() => {
    setEditJson(editSnapshot);
    try {
      const v = JSON.parse(editSnapshot);
      setEditFormValue(v);
      setJsonExpertDraft({ valid: true, value: v });
    } catch {
      setEditFormValue({});
      setJsonExpertDraft({ valid: false, error: 'Invalid snapshot JSON' });
    }
  }, [editSnapshot]);

  const saveEdit = async () => {
    if (!editFacetType) return;
    if (!metadataFacetTargetId) {
      notifications.show({
        color: 'yellow',
        title: 'No metadata binding',
        message: 'Cannot save facets without a metadata entity URN.',
      });
      return;
    }
    try {
      let parsed: unknown;
      const manifest = facetTypes.find((f) => f.typeKey === editFacetType) ?? null;
      const editSchema = effectiveFacetPayloadSchemaForEdit(manifest, editInstanceIndex);
      const expertOnly =
        editSchema == null || !facetPayloadSchemaFormSupported(editSchema);
      if (expertOnly || editMode === 'json') {
        if (jsonExpertDraft?.valid && jsonExpertDraft.value !== undefined) {
          parsed = jsonExpertDraft.value;
        } else {
          notifications.show({
            color: 'red',
            title: 'Invalid document',
            message: jsonExpertDraft?.error ?? 'Fix JSON or YAML in expert mode before saving.',
          });
          return;
        }
      } else {
        parsed = editFormValue;
      }
      const isMultiple = (manifest?.targetCardinality ?? 'SINGLE') === 'MULTIPLE';
      if (editInstanceIndex !== null && isMultiple) {
        if (!expertOnly && editMode === 'form' && manifest?.payload) {
          const errors: string[] = [];
          appendFacetPayloadRequiredErrors(manifest.payload, parsed, errors);
          appendEmailStereotypeValidationErrors(manifest.payload, parsed, errors, '');
          appendHyperlinkStereotypeValidationErrors(manifest.payload, parsed, errors, '');
          if (errors.length > 0) {
            notifications.show({
              color: 'red',
              title: 'Validation failed',
              message: errors.slice(0, 3).join('; '),
            });
            return;
          }
        }
        let instancePayload: unknown = parsed;
        if (Array.isArray(instancePayload)) {
          instancePayload =
            editInstanceIndex != null && editInstanceIndex >= 0
              ? instancePayload[editInstanceIndex]
              : instancePayload[0];
        }
        if (instancePayload == null || typeof instancePayload !== 'object' || Array.isArray(instancePayload)) {
          notifications.show({
            color: 'red',
            title: 'Invalid payload',
            message: 'MULTIPLE facet rows must save as a single JSON object (one instance).',
          });
          return;
        }
        const rowUid = facets.instanceUidsByType?.[editFacetType]?.[editInstanceIndex ?? -1];
        if (typeof rowUid === 'string' && rowUid.length > 0) {
          await schemaService.patchEntityFacetPayload(
            metadataFacetTargetId,
            editFacetType,
            rowUid,
            instancePayload
          );
        } else {
          await schemaService.setEntityFacet(
            metadataFacetTargetId,
            editFacetType,
            selectedContext,
            instancePayload
          );
        }
        await onFacetsChanged();
        notifications.show({ color: 'green', title: 'Facet saved', message: editFacetType });
        setEditFacetType(null);
        setEditInstanceIndex(null);
        return;
      }
      if (!expertOnly && editMode === 'form' && manifest?.payload) {
        const errors: string[] = [];
        appendFacetPayloadRequiredErrors(manifest.payload, parsed, errors);
        appendEmailStereotypeValidationErrors(manifest.payload, parsed, errors, '');
        appendHyperlinkStereotypeValidationErrors(manifest.payload, parsed, errors, '');
        if (errors.length > 0) {
          notifications.show({
            color: 'red',
            title: 'Validation failed',
            message: errors.slice(0, 3).join('; '),
          });
          return;
        }
      }
      await schemaService.setEntityFacet(metadataFacetTargetId, editFacetType, selectedContext, parsed);
      await onFacetsChanged();
      notifications.show({ color: 'green', title: 'Facet saved', message: editFacetType });
      setEditFacetType(null);
      setEditInstanceIndex(null);
    } catch (e) {
      notifications.show({ color: 'red', title: 'Save failed', message: e instanceof Error ? e.message : 'Unknown error' });
    }
  };

  const activeManifest = useMemo(
    () => facetTypes.find((f) => f.typeKey === editFacetType) ?? null,
    [facetTypes, editFacetType]
  );
  const effectivePayloadSchema = useMemo<FacetPayloadSchema | null>(
    () => effectiveFacetPayloadSchemaForEdit(activeManifest, editInstanceIndex),
    [activeManifest, editInstanceIndex]
  );

  const facetFormSupported = useMemo(
    () => effectivePayloadSchema != null && facetPayloadSchemaFormSupported(effectivePayloadSchema),
    [effectivePayloadSchema]
  );

  /** Sync JSON snapshot only when switching modes — avoids JSON.stringify on every keystroke in form mode. */
  const handleFacetEditModeChange = useCallback(
    (v: string | null) => {
      const next = (v as 'form' | 'json') ?? 'form';
      if (next === 'form' && !facetFormSupported) {
        notifications.show({
          color: 'yellow',
          title: 'Form not available',
          message:
            'This facet uses nested structures the form cannot edit (for example arrays of objects). Use Expert JSON/YAML.',
        });
        return;
      }
      setEditMode((prev) => {
        if (next === 'json' && prev === 'form') {
          const fv = editFormValueRef.current;
          setEditJson(JSON.stringify(fv, null, 2));
          setJsonExpertDraft({ valid: true, value: fv });
        } else if (next === 'form' && prev === 'json') {
          try {
            setEditFormValue(JSON.parse(editJsonRef.current));
          } catch {
            /* invalid JSON — keep previous form state */
          }
        }
        return next;
      });
    },
    [facetFormSupported]
  );

  useEffect(() => {
    if (!editFacetType || facetFormSupported) return;
    setEditMode((prev) => {
      if (prev === 'json') return prev;
      const fv = editFormValueRef.current;
      setEditJson(JSON.stringify(fv, null, 2));
      setJsonExpertDraft({ valid: true, value: fv });
      return 'json';
    });
  }, [editFacetType, facetFormSupported]);

  const renderField = (
    schema: FacetPayloadSchema,
    value: unknown,
    onChange: (next: unknown) => void,
    keyPrefix: string,
    stringStere: FacetStringStereotypeKind = 'none'
  ): React.ReactNode => {
    if (schema.type === 'OBJECT') {
      const obj = (value && typeof value === 'object' ? value : {}) as Record<string, unknown>;
      return (
        <Stack gap={6}>
          {(schema.fields ?? []).map((field) => (
            <Stack key={`${keyPrefix}.${field.name}`} gap={6}>
              {(field.schema.type === 'OBJECT' || field.schema.type === 'ARRAY') ? (
                <>
                  <Group gap={4}>
                    {field.schema.description && (
                      <Tooltip label={field.schema.description} withArrow>
                        <ActionIcon size="xs" variant="subtle" aria-label="Field description">
                          <HiOutlineInformationCircle size={12} />
                        </ActionIcon>
                      </Tooltip>
                    )}
                    <Divider
                      label={<Text size="xs" fw={500}>{field.schema.title || field.name}</Text>}
                      labelPosition="left"
                      style={{ flex: 1 }}
                    />
                  </Group>
                  <Box ml="md" pl="sm" style={{ borderLeft: '2px solid var(--mantine-color-default-border)' }}>
                    {renderField(
                      field.schema,
                      obj[field.name],
                      (next) => onChange({ ...obj, [field.name]: next }),
                      `${keyPrefix}.${field.name}`,
                      facetStringStereotype(field.schema, field.stereotype)
                    )}
                  </Box>
                </>
              ) : (
                <Group gap="sm" wrap="nowrap" align="center">
                  <Box style={{ minWidth: 170 }}>
                    <Group gap={4} wrap="nowrap" align="center">
                      {field.schema.description && (
                        <Tooltip label={field.schema.description} withArrow>
                          <ActionIcon size="xs" variant="subtle" aria-label="Field description">
                            <HiOutlineInformationCircle size={12} />
                          </ActionIcon>
                        </Tooltip>
                      )}
                      <Text size="xs" fw={400} c="dimmed">{field.schema.title || field.name}</Text>
                      {(field.required ?? true) && (
                        <Text size="xs" c="red">*</Text>
                      )}
                    </Group>
                  </Box>
                  <Box style={{ flex: 1 }}>
                    {renderField(
                      field.schema,
                      obj[field.name],
                      (next) => onChange({ ...obj, [field.name]: next }),
                      `${keyPrefix}.${field.name}`,
                      facetStringStereotype(field.schema, field.stereotype)
                    )}
                  </Box>
                </Group>
              )}
            </Stack>
          ))}
        </Stack>
      );
    }
    if (schema.type === 'STRING') {
      const strVal = typeof value === 'string' ? value : '';
      const emailInvalid =
        stringStere === 'email' && strVal.trim().length > 0 && !facetEmailLooksValid(strVal);
      const inputType = stringStere === 'email' ? 'email' : stringStere === 'hyperlink' ? 'url' : 'text';
      const placeholder =
        schema.description ??
        (stringStere === 'hyperlink' ? 'https://…' : stringStere === 'email' ? 'name@example.com' : undefined);
      return (
        <TextInput
          size="xs"
          type={inputType}
          value={strVal}
          onChange={(e) => onChange(e.currentTarget.value)}
          placeholder={placeholder}
          error={emailInvalid ? 'Enter a valid email address' : undefined}
        />
      );
    }
    if (schema.type === 'NUMBER') {
      return (
        <NumberInput
          size="xs"
          value={typeof value === 'number' ? value : undefined}
          onChange={(v) => onChange(typeof v === 'number' ? v : 0)}
        />
      );
    }
    if (schema.type === 'BOOLEAN') {
      return (
        <Switch
          size="sm"
          checked={Boolean(value)}
          onChange={(e) => onChange(e.currentTarget.checked)}
        />
      );
    }
    if (schema.type === 'ENUM') {
      return (
        <Select
          size="xs"
          data={(schema.values ?? []).map((v) => ({
            value: v.value,
            label: v.description ? `${v.value} - ${v.description}` : v.value,
          }))}
          value={typeof value === 'string' ? value : null}
          onChange={(v) => onChange(v ?? '')}
        />
      );
    }
    if (schema.type === 'ARRAY') {
      const itemsSchema = schema.items;
      const itemType = itemsSchema?.type;
      if (itemsSchema?.type === 'OBJECT') {
        const arr = Array.isArray(value) ? value : [];
        const itemLabel = schema.title?.trim() || 'Item';
        return (
          <Stack gap="sm">
            {arr.map((item, idx) => (
              <Card key={`${keyPrefix}-ao-${idx}`} withBorder p="xs">
                <Group justify="space-between" mb={6}>
                  <Text size="xs" c="dimmed">
                    {arr.length === 1 ? itemLabel : `${itemLabel} ${idx + 1}`}
                  </Text>
                  <Button
                    size="compact-xs"
                    variant="subtle"
                    color="red"
                    onClick={() => onChange(arr.filter((_, i) => i !== idx))}
                  >
                    Remove
                  </Button>
                </Group>
                {renderField(
                  itemsSchema,
                  item,
                  (next) => {
                    const nextArr = [...arr];
                    nextArr[idx] = next;
                    onChange(nextArr);
                  },
                  `${keyPrefix}[${idx}]`,
                  'none'
                )}
              </Card>
            ))}
            <Button
              size="xs"
              variant="light"
              leftSection={<HiOutlinePlus size={14} />}
              onClick={() => onChange([...arr, {}])}
            >
              Add item
            </Button>
          </Stack>
        );
      }
      if (itemsSchema && (itemType === 'STRING' || itemType === 'ENUM')) {
        const arr = Array.isArray(value) ? value : [];
        const tags = arr.map((x) => String(x ?? ''));
        const emailInvalid =
          itemType === 'STRING' &&
          stringStere === 'email' &&
          tags.some((t) => t.trim().length > 0 && !facetEmailLooksValid(t));
        const placeholder =
          schema.description ||
          (itemType === 'STRING' && stringStere === 'hyperlink'
            ? 'URLs — comma or Enter'
            : itemType === 'STRING' && stringStere === 'email'
              ? 'Emails — comma or Enter'
              : 'Type values, comma or Enter');
        return (
          <TagsInput
            size="xs"
            placeholder={placeholder}
            value={tags}
            onChange={(next) => onChange(next)}
            data={itemType === 'ENUM' ? (itemsSchema.values ?? []).map((ev) => ev.value) : undefined}
            splitChars={[',', ';']}
            clearable
            error={emailInvalid ? 'Each entry must be a valid email address' : undefined}
          />
        );
      }
      if (itemsSchema?.type === 'NUMBER') {
        const arr = Array.isArray(value) ? value : [];
        const tags = arr.map((x) =>
          typeof x === 'number' && Number.isFinite(x) ? String(x) : String(x ?? '')
        );
        return (
          <TagsInput
            size="xs"
            placeholder={schema.description || 'Numbers, comma or Enter'}
            value={tags}
            onChange={(next) => {
              const nums = next
                .map((s) => {
                  const t = s.trim();
                  if (t.length === 0) return null;
                  const n = Number(t);
                  return Number.isFinite(n) ? n : null;
                })
                .filter((n): n is number => n !== null);
              onChange(nums);
            }}
            splitChars={[',', ';']}
            clearable
          />
        );
      }
    }
    return (
      <Textarea
        size="xs"
        minRows={3}
        value={JSON.stringify(value ?? (schema.type === 'ARRAY' ? [] : {}), null, 2)}
        onChange={(e) => {
          try {
            onChange(JSON.parse(e.currentTarget.value));
          } catch {
            // keep editing until valid json
          }
        }}
        styles={{ input: { fontFamily: 'monospace' } }}
      />
    );
  };

  const renderReadOnlyField = (
    schema: FacetPayloadSchema,
    value: unknown,
    keyPrefix: string,
    stringStere: FacetStringStereotypeKind = 'none',
    /** Field-level stereotype wire (for tags / empty-array behaviour). */
    fieldStereotypeWire?: string | string[] | null
  ): React.ReactNode => {
    const labelWithInfo = (title: string, description?: string) => (
      <Group gap={4} wrap="nowrap" align="center">
        {description && (
          <Tooltip label={description} withArrow>
            <ActionIcon size="xs" variant="subtle" aria-label="Field description">
              <HiOutlineInformationCircle size={12} />
            </ActionIcon>
          </Tooltip>
        )}
        <Text size="xs" fw={400} c="dimmed">{title}</Text>
      </Group>
    );

    const primitiveValue = (s: FacetPayloadSchema, v: unknown): React.ReactNode => {
      if (s.type === 'BOOLEAN') {
        return <Badge size="xs" variant="light" color={v ? 'green' : 'gray'}>{String(Boolean(v))}</Badge>;
      }
      if (s.type === 'ENUM') {
        const raw = String(v ?? '');
        const selected = (s.values ?? []).find((entry) => entry.value === raw);
        const label = selected?.description ? `${selected.value} - ${selected.description}` : raw;
        return <Badge size="xs" variant="light">{label}</Badge>;
      }
      return <Text size="sm">{v == null ? '' : String(v)}</Text>;
    };

    if (schema.type === 'OBJECT') {
      const obj = (value && typeof value === 'object' ? value : {}) as Record<string, unknown>;
      return (
        <Stack gap={6}>
          {(schema.fields ?? []).map((field) => (
            <Box key={`${keyPrefix}.${field.name}`}>
              {field.schema.type === 'OBJECT' && facetHyperlinkPresentationActive(field.schema, field.stereotype) ? (
                <Group gap="sm" wrap="nowrap" align="center">
                  <Box style={{ minWidth: 170 }}>
                    {labelWithInfo(field.schema.title || field.name, field.schema.description)}
                  </Box>
                  <Box style={{ flex: 1 }}>
                    <FacetHyperlinkReadOnly value={obj[field.name]} />
                  </Box>
                </Group>
              ) : field.schema.type === 'OBJECT' || field.schema.type === 'ARRAY' ? (
                <Stack gap={6}>
                  <Group gap={4}>
                    {field.schema.description && (
                      <Tooltip label={field.schema.description} withArrow>
                        <ActionIcon size="xs" variant="subtle" aria-label="Field description">
                          <HiOutlineInformationCircle size={12} />
                        </ActionIcon>
                      </Tooltip>
                    )}
                    <Divider
                      label={<Text size="xs" fw={500}>{field.schema.title || field.name}</Text>}
                      labelPosition="left"
                      style={{ flex: 1 }}
                    />
                  </Group>
                  <Box
                    ml="md"
                    pl="sm"
                    style={{
                      borderLeft: '2px solid var(--mantine-color-default-border)',
                    }}
                  >
                    {renderReadOnlyField(
                      field.schema,
                      obj[field.name],
                      `${keyPrefix}.${field.name}`,
                      facetStringStereotype(field.schema, field.stereotype),
                      field.stereotype
                    )}
                  </Box>
                </Stack>
              ) : (
                <Group gap="sm" wrap="nowrap" align="center">
                  <Box style={{ minWidth: 170 }}>
                    {labelWithInfo(field.schema.title || field.name, field.schema.description)}
                  </Box>
                  <Box style={{ flex: 1 }}>
                    {(() => {
                      if (facetHyperlinkPresentationActive(field.schema, field.stereotype)) {
                        return <FacetHyperlinkReadOnly value={obj[field.name]} />;
                      }
                      if (facetStringStereotype(field.schema, field.stereotype) === 'email') {
                        return <FacetEmailReadOnly value={obj[field.name]} />;
                      }
                      return primitiveValue(field.schema, obj[field.name]);
                    })()}
                  </Box>
                </Group>
              )}
            </Box>
          ))}
        </Stack>
      );
    }
    if (schema.type === 'ARRAY') {
      if (!Array.isArray(value) || value.length === 0) {
        if (facetTagsPresentationActive(schema, fieldStereotypeWire)) {
          return null;
        }
        return <Text size="xs" c="dimmed">-</Text>;
      }
      const itemSchema = schema.items;
      const listHyperlink = facetHyperlinkPresentationActive(schema, fieldStereotypeWire);
      if (itemSchema?.type === 'OBJECT' && listHyperlink) {
        return (
          <Stack gap={6} pl={4}>
            {value.map((item, idx) => (
              <Group key={`${keyPrefix}-href-obj-${idx}`} gap={8} align="flex-start" wrap="nowrap">
                <Text size="xs" c="dimmed" mt={3} style={{ flexShrink: 0 }}>
                  •
                </Text>
                <Box style={{ flex: 1, minWidth: 0 }}>
                  <FacetHyperlinkReadOnly value={item} />
                </Box>
              </Group>
            ))}
          </Stack>
        );
      }
      const primitiveItem =
        itemSchema &&
        (itemSchema.type === 'STRING' || itemSchema.type === 'NUMBER' || itemSchema.type === 'ENUM');
      if (primitiveItem && itemSchema) {
        if (itemSchema.type === 'STRING' && (listHyperlink || stringStere === 'email')) {
          return (
            <Group gap="sm" wrap="wrap" align="flex-start">
              {value.map((item, idx) => (
                <Box key={`${keyPrefix}-p-${idx}`}>
                  {listHyperlink ? (
                    <FacetHyperlinkReadOnly value={item} />
                  ) : (
                    <FacetEmailReadOnly value={item} />
                  )}
                </Box>
              ))}
            </Group>
          );
        }
        return (
          <Group gap={6} wrap="wrap">
            {value.map((item, idx) => (
              <Badge key={`${keyPrefix}-p-${idx}`} size="sm" variant="light">
                {itemSchema.type === 'ENUM'
                  ? (() => {
                      const raw = String(item ?? '');
                      const selected = (itemSchema.values ?? []).find((e) => e.value === raw);
                      return selected?.description ? `${selected.value} — ${selected.description}` : raw;
                    })()
                  : String(item ?? '')}
              </Badge>
            ))}
          </Group>
        );
      }
      return (
        <Stack gap={6} pl={4}>
          {value.map((item, idx) => (
            <Group key={`${keyPrefix}[${idx}]`} gap={8} align="flex-start" wrap="nowrap">
              <Text size="xs" c="dimmed" mt={3} style={{ flexShrink: 0 }}>
                •
              </Text>
              <Box style={{ flex: 1, minWidth: 0 }}>
                {schema.items ? renderReadOnlyField(schema.items, item, `${keyPrefix}[${idx}]`, 'none', undefined) : (
                  <Text size="xs">{String(item)}</Text>
                )}
              </Box>
            </Group>
          ))}
        </Stack>
      );
    }
    return primitiveValue(schema, value);
  };

  const deleteFacet = async (facetType: string) => {
    if (!metadataFacetTargetId) return;
    try {
      await schemaService.deleteEntityFacet(metadataFacetTargetId, facetType, selectedContext);
      await onFacetsChanged();
      if (editFacetType === facetType) {
        setEditFacetType(null);
        setEditInstanceIndex(null);
      }
      notifications.show({ color: 'green', title: 'Facet deleted', message: facetType });
    } catch (e) {
      notifications.show({ color: 'red', title: 'Delete failed', message: e instanceof Error ? e.message : 'Unknown error' });
    } finally {
      setFacetToDelete(null);
    }
  };

  const deleteFacetInstance = async (facetType: string, instanceIndex: number) => {
    if (!metadataFacetTargetId) return;
    try {
      const uid = facets.instanceUidsByType?.[facetType]?.[instanceIndex];
      await schemaService.deleteEntityFacet(metadataFacetTargetId, facetType, selectedContext, uid);
      await onFacetsChanged();
      if (editFacetType === facetType && editInstanceIndex === instanceIndex) {
        setEditFacetType(null);
        setEditInstanceIndex(null);
      }
      notifications.show({ color: 'green', title: 'Entry removed', message: facetType });
    } catch (e) {
      notifications.show({ color: 'red', title: 'Delete failed', message: e instanceof Error ? e.message : 'Unknown error' });
    } finally {
      setFacetToDelete(null);
    }
  };

  return (
    <Box h="100%">
      {/* Header */}
      <Box
        p="md"
        style={{
          borderBottom: '1px solid var(--mantine-color-default-border)',
          background: isDark
            ? 'linear-gradient(135deg, var(--mantine-color-dark-8) 0%, var(--mantine-color-dark-7) 100%)'
            : 'linear-gradient(135deg, var(--mantine-color-teal-0) 0%, white 100%)',
        }}
      >
        <Group gap="md" mb="xs" justify="space-between" wrap="nowrap">
          <Group gap="md" wrap="nowrap" style={{ minWidth: 0 }}>
            <Box
              style={{
                width: 40,
                height: 40,
                borderRadius: 8,
                backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <Icon size={20} color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'} />
            </Box>
            <Box style={{ minWidth: 0 }}>
              <Group gap="xs">
                <Text size="lg" fw={600} c={isDark ? 'gray.1' : 'gray.8'} truncate>
                  {facets.descriptive?.displayName || entityName}
                </Text>
                <Badge variant="light" color={isDark ? 'cyan' : 'teal'} size="sm">
                  {entityLabels[entity.entityType]}
                </Badge>
              </Group>
              <Text size="sm" c="dimmed" ff="monospace" truncate title={entity.id}>
                {metadataFacetTargetId ?? entity.id}
              </Text>
            </Box>
          </Group>
          <Group gap={4} wrap="nowrap">
            <Menu withinPortal position="bottom-end">
              <Group gap={0} wrap="nowrap" mr="xs">
                <Button
                  size="xs"
                  leftSection={<HiOutlinePlus size={14} />}
                  onClick={() => {
                    const key = addFacetMenu.defaultTypeKey;
                    if (key) void addFacetByType(key);
                  }}
                  disabled={addFacetMenu.isEmpty || !canMutateMetadataFacets}
                  style={{ borderTopRightRadius: 0, borderBottomRightRadius: 0 }}
                >
                  Add Facet
                </Button>
                <Menu.Target>
                  <ActionIcon
                    size={30}
                    variant="filled"
                    style={{ borderTopLeftRadius: 0, borderBottomLeftRadius: 0 }}
                    disabled={addFacetMenu.isEmpty || !canMutateMetadataFacets}
                  >
                    <HiOutlineChevronDown size={14} />
                  </ActionIcon>
                </Menu.Target>
              </Group>
              <Menu.Dropdown>
                {addFacetMenu.isEmpty ? (
                  <Menu.Item disabled>No applicable facet types</Menu.Item>
                ) : (
                  <>
                    {addFacetMenu.generalList.length > 0 && (
                      <>
                        <Menu.Label>{formatCategoryLabel('general')}</Menu.Label>
                        {addFacetMenu.generalList.map((opt) => (
                          <Menu.Item key={opt.value} onClick={() => void addFacetByType(opt.value)}>
                            {opt.label}
                          </Menu.Item>
                        ))}
                      </>
                    )}
                    {addFacetMenu.generalList.length > 0 && addFacetMenu.nonGeneralCategories.length > 0 ? (
                      <Menu.Divider />
                    ) : null}
                    {addFacetMenu.nonGeneralCategories.map((cat) => (
                      <Menu.Sub key={cat}>
                        <Menu.Sub.Target>
                          <Menu.Sub.Item
                            rightSection={<HiOutlineChevronRight size={14} />}
                            closeMenuOnClick={false}
                          >
                            {formatCategoryLabel(cat)}
                          </Menu.Sub.Item>
                        </Menu.Sub.Target>
                        <Menu.Sub.Dropdown>
                          {(addFacetMenu.byCategory[cat] ?? []).map((opt) => (
                            <Menu.Sub.Item
                              key={opt.value}
                              onClick={() => void addFacetByType(opt.value)}
                            >
                              {opt.label}
                            </Menu.Sub.Item>
                          ))}
                        </Menu.Sub.Dropdown>
                      </Menu.Sub>
                    ))}
                  </>
                )}
              </Menu.Dropdown>
            </Menu>
            {relationCount > 0 && (
              <Badge variant="light" color="indigo" size="xs">
                Related {relationCount}
              </Badge>
            )}
            <RelatedContentButton
              contextType="model"
              contextId={entity.id}
              contextLabel={entityName}
              contextEntityType={entity.entityType}
            />
            <InlineChatButton
              contextType="model"
              contextId={entity.id}
              contextLabel={entityName}
              contextEntityType={entity.entityType}
            />
          </Group>
        </Group>

        {/* Quick badges for structural info */}
        {flags.modelQuickBadges && facets.structural && (
          <Group gap="xs" mt="sm">
            {facets.structural.isPrimaryKey && (
              <Badge color={isDark ? 'cyan' : 'teal'} variant="filled" size="sm">
                PK
              </Badge>
            )}
            {facets.structural.isForeignKey && (
              <Badge color="orange" variant="filled" size="sm">
                FK
              </Badge>
            )}
            {facets.structural.isUnique && (
              <Badge color={isDark ? 'cyan' : 'teal'} variant="light" size="sm">
                Unique
              </Badge>
            )}
            {facets.structural.nullable === false && (
              <Badge color="red" variant="light" size="sm">
                Not Null
              </Badge>
            )}
            {flags.modelPhysicalType && (facets.structural.physicalType || facets.structural.type) && (
              <Badge variant="outline" color="gray" size="sm">
                {facets.structural.type || facets.structural.physicalType}
              </Badge>
            )}
          </Group>
        )}
      </Box>

      {/* Content */}
      <Box p="md" style={{ overflow: 'auto', height: 'calc(100% - 120px)' }}>
        <Box
          mb="md"
          p="sm"
          style={{
            border: '1px solid var(--mantine-color-default-border)',
            borderRadius: 8,
            backgroundColor: isDark ? 'var(--mantine-color-dark-7)' : 'var(--mantine-color-gray-0)',
          }}
        >
          <Group gap="xs" mb="xs">
            <Badge variant="outline" color="gray" size="sm">
              Explorer {entity.id}
            </Badge>
            {metadataFacetTargetId && (
              <Badge variant="outline" color="gray" size="sm">
                Metadata URN
              </Badge>
            )}
            {baseTypeLabel && (
              <Badge variant="light" color={isDark ? 'cyan' : 'teal'} size="sm">
                {baseTypeLabel}
              </Badge>
            )}
          </Group>
          {entity.entityType === 'SCHEMA' && (
            <Text size="sm" c="dimmed">
              Tables: {entity.tables.length}
            </Text>
          )}
          {entity.entityType === 'TABLE' && (
            <Text size="sm" c="dimmed">
              Table type: {entity.tableType} · Columns: {entity.columns.length}
            </Text>
          )}
          {entity.entityType === 'COLUMN' && (
            <Text size="sm" c="dimmed">
              Column: {entity.columnName} · Position: {entity.fieldIndex}
            </Text>
          )}
        </Box>

        {orderedFacetTypes.length === 0 ? (
          <Box py="xl" ta="center">
            <Text c="dimmed">No metadata facets available for this entity yet.</Text>
          </Box>
        ) : (
          <Tabs value={activeCategoryTab} onChange={setActiveCategoryTab}>
            <Tabs.List mb="sm">
              {facetUnitsByCategory.orderedCategories.map((category) => (
                <Tabs.Tab key={category} value={category}>
                  {formatCategoryLabel(category)}
                </Tabs.Tab>
              ))}
            </Tabs.List>
            {facetUnitsByCategory.orderedCategories.map((category) => (
              <Tabs.Panel key={category} value={category}>
                <Stack gap="md">
                  {(facetUnitsByCategory.grouped[category] ?? []).map((unit) => {
                    const facetType = unit.facetType;
                    const descriptor = descriptorByType[facetType] ?? null;
                    const baseTitle = facetBoxBaseTitle(facetType, facetTypeTitleByKey, descriptor);

                    const facetHeaderIcons = (
                      <Group gap="xs">
                        {facetType.endsWith(':descriptive') && <HiOutlineDocumentText size={16} />}
                        {facetType.endsWith(':structural') && <HiOutlineCube size={16} />}
                        {facetType.endsWith(':relation') && <HiOutlineArrowsRightLeft size={16} />}
                        <Text fw={600} size="sm">
                          {baseTitle}
                        </Text>
                      </Group>
                    );

                    if (unit.kind === 'multipleEmpty') {
                      return (
                        <Card key={`${facetType}-empty`} withBorder p="xs">
                          <Group justify="space-between" mb={6}>
                            {facetHeaderIcons}
                            <Button
                              size="compact-xs"
                              leftSection={<HiOutlinePlus size={12} />}
                              disabled={!canMutateMetadataFacets}
                              onClick={() => void addAnotherMultipleInstance(facetType)}
                            >
                              Add entry
                            </Button>
                          </Group>
                          <Text size="sm" c="dimmed">
                            No entries for this facet yet. Add one to start.
                          </Text>
                        </Card>
                      );
                    }

                    if (unit.kind === 'multipleRow') {
                      const items = multipleFacetItemValues(allByType[facetType]);
                      const item = items[unit.index];
                      const itemSchema = descriptor?.payload;
                      const caption = multipleInstanceCaption(item, unit.index, unit.total);
                      const cardTitle = caption ? `${baseTitle} · ${caption}` : baseTitle;
                      const isEditing = editFacetType === facetType && editInstanceIndex === unit.index;
                      const readOnlyBody = itemSchema ? (
                        <Box>
                          {renderReadOnlyField(
                            itemSchema,
                            item,
                            `${facetType}[${unit.index}]`
                          )}
                        </Box>
                      ) : (
                        <SyntaxCodeEditor
                          value={JSON.stringify(item ?? {}, null, 2)}
                          language="json"
                          minHeight={200}
                          readOnly
                        />
                      );
                      return (
                        <Card key={`${facetType}-${unit.index}`} withBorder p="xs">
                          <Group justify="space-between" mb={6}>
                            <Group gap="xs">
                              {facetType.endsWith(':descriptive') && <HiOutlineDocumentText size={16} />}
                              {facetType.endsWith(':structural') && <HiOutlineCube size={16} />}
                              {facetType.endsWith(':relation') && <HiOutlineArrowsRightLeft size={16} />}
                              <Text fw={600} size="sm">
                                {cardTitle}
                              </Text>
                            </Group>
                            {isEditing ? (
                              <Group gap="xs">
                                <Button
                                  size="compact-xs"
                                  variant="light"
                                  onClick={() => resetFacetEditToSnapshot()}
                                >
                                  Reset
                                </Button>
                                <Button
                                  size="compact-xs"
                                  variant="light"
                                  onClick={() => {
                                    setEditFacetType(null);
                                    setEditInstanceIndex(null);
                                  }}
                                >
                                  Cancel
                                </Button>
                                <Button size="compact-xs" onClick={() => void saveEdit()}>Save</Button>
                              </Group>
                            ) : (
                              <Group gap="xs">
                                <Button
                                  size="compact-xs"
                                  variant="light"
                                  leftSection={<HiOutlinePencilSquare size={12} />}
                                  disabled={!canMutateMetadataFacets}
                                  onClick={() => openEdit(facetType, unit.index)}
                                >
                                  Edit
                                </Button>
                                <Button
                                  size="compact-xs"
                                  variant="light"
                                  color="red"
                                  leftSection={<HiOutlineTrash size={12} />}
                                  disabled={!canMutateMetadataFacets}
                                  onClick={() => setFacetToDelete({ facetType, instanceIndex: unit.index })}
                                >
                                  Delete
                                </Button>
                              </Group>
                            )}
                          </Group>
                          {isEditing ? (
                            <Stack gap="xs">
                              {!facetFormSupported ? (
                                <>
                                  <Text size="xs" c="dimmed">
                                    {(editFacetType === facetType && effectivePayloadSchema == null
                                      ? 'Descriptor payload schema unavailable.'
                                      : 'This facet payload includes structures the form cannot edit (for example arrays of objects).')}{' '}
                                    Edit below as JSON or YAML.
                                  </Text>
                                  <JsonYamlEditor
                                    key={`${facetType}-${unit.index}-expert`}
                                    value={editFormValue}
                                    onApply={(next) => {
                                      setEditFormValue(next);
                                      setEditJson(JSON.stringify(next, null, 2));
                                      setJsonExpertDraft({ valid: true, value: next });
                                      notifications.show({
                                        color: 'green',
                                        title: 'Applied',
                                        message: 'Payload updated from expert editor.',
                                      });
                                    }}
                                    onDraftParsed={setJsonExpertDraft}
                                    minHeight={360}
                                  />
                                </>
                              ) : (
                                <>
                                  <Group justify="space-between">
                                    <Chip.Group
                                      multiple={false}
                                      value={editMode}
                                      onChange={handleFacetEditModeChange}
                                    >
                                      <Group gap="xs">
                                        <Chip size="xs" value="form">Form</Chip>
                                        <Chip size="xs" value="json">Expert JSON/YAML</Chip>
                                      </Group>
                                    </Chip.Group>
                                  </Group>
                                  {editMode === 'json' ? (
                                    <JsonYamlEditor
                                      key={`${facetType}-${unit.index}-expert`}
                                      value={editFormValue}
                                      onApply={(next) => {
                                        setEditFormValue(next);
                                        setEditJson(JSON.stringify(next, null, 2));
                                        setJsonExpertDraft({ valid: true, value: next });
                                        notifications.show({
                                          color: 'green',
                                          title: 'Applied',
                                          message: 'Payload updated from expert editor.',
                                        });
                                      }}
                                      onDraftParsed={setJsonExpertDraft}
                                      minHeight={360}
                                    />
                                  ) : effectivePayloadSchema ? (
                                    renderField(effectivePayloadSchema, editFormValue, setEditFormValue, `${facetType}-inst-${unit.index}`)
                                  ) : (
                                    <Text size="xs" c="dimmed">Descriptor payload schema unavailable; switch to expert JSON/YAML mode.</Text>
                                  )}
                                </>
                              )}
                            </Stack>
                          ) : (
                            readOnlyBody
                          )}
                        </Card>
                      );
                    }

                    let readOnlyBody: React.ReactNode;
                    if (facetType.endsWith(':structural') && hasStructural) {
                      readOnlyBody = <StructuralFacet facet={facets.structural!} />;
                    } else if (descriptor?.payload) {
                      readOnlyBody = (
                        <Box>
                          {renderReadOnlyField(descriptor.payload, allByType[facetType], facetType)}
                        </Box>
                      );
                    } else {
                      readOnlyBody = (
                        <SyntaxCodeEditor
                          value={JSON.stringify(allByType[facetType] ?? {}, null, 2)}
                          language="json"
                          minHeight={200}
                          readOnly
                        />
                      );
                    }

                    const isEditingSingle = editFacetType === facetType && editInstanceIndex === null;

                    return (
                      <Card key={facetType} withBorder p="xs">
                        <Group justify="space-between" mb={6}>
                          {facetHeaderIcons}
                          {isEditingSingle ? (
                            <Group gap="xs">
                              <Button size="compact-xs" variant="light" onClick={() => resetFacetEditToSnapshot()}>
                                Reset
                              </Button>
                              <Button size="compact-xs" variant="light" onClick={() => {
                                setEditFacetType(null);
                                setEditInstanceIndex(null);
                              }}>Cancel</Button>
                              <Button size="compact-xs" onClick={() => void saveEdit()}>Save</Button>
                            </Group>
                          ) : (
                            <Group gap="xs">
                              <Button
                                size="compact-xs"
                                variant="light"
                                leftSection={<HiOutlinePencilSquare size={12} />}
                                disabled={!canMutateMetadataFacets}
                                onClick={() => openEdit(facetType)}
                              >
                                Edit
                              </Button>
                              <Button
                                size="compact-xs"
                                variant="light"
                                color="red"
                                leftSection={<HiOutlineTrash size={12} />}
                                disabled={!canMutateMetadataFacets}
                                onClick={() => setFacetToDelete({ facetType, instanceIndex: null })}
                              >
                                Delete
                              </Button>
                            </Group>
                          )}
                        </Group>
                        {isEditingSingle ? (
                          <Stack gap="xs">
                            {!facetFormSupported ? (
                              <>
                                <Text size="xs" c="dimmed">
                                  {(editFacetType === facetType && effectivePayloadSchema == null
                                    ? 'Descriptor payload schema unavailable.'
                                    : 'This facet payload includes structures the form cannot edit (for example arrays of objects).')}{' '}
                                  Edit below as JSON or YAML.
                                </Text>
                                <JsonYamlEditor
                                  key={`${facetType}-expert`}
                                  value={editFormValue}
                                  onApply={(next) => {
                                    setEditFormValue(next);
                                    setEditJson(JSON.stringify(next, null, 2));
                                    setJsonExpertDraft({ valid: true, value: next });
                                    notifications.show({
                                      color: 'green',
                                      title: 'Applied',
                                      message: 'Payload updated from expert editor.',
                                    });
                                  }}
                                  onDraftParsed={setJsonExpertDraft}
                                  minHeight={360}
                                />
                              </>
                            ) : (
                              <>
                                <Group justify="space-between">
                                  <Chip.Group
                                    multiple={false}
                                    value={editMode}
                                    onChange={handleFacetEditModeChange}
                                  >
                                    <Group gap="xs">
                                      <Chip size="xs" value="form">Form</Chip>
                                      <Chip size="xs" value="json">Expert JSON/YAML</Chip>
                                    </Group>
                                  </Chip.Group>
                                </Group>
                                {editMode === 'json' ? (
                                  <JsonYamlEditor
                                    key={`${facetType}-expert`}
                                    value={editFormValue}
                                    onApply={(next) => {
                                      setEditFormValue(next);
                                      setEditJson(JSON.stringify(next, null, 2));
                                      setJsonExpertDraft({ valid: true, value: next });
                                      notifications.show({
                                        color: 'green',
                                        title: 'Applied',
                                        message: 'Payload updated from expert editor.',
                                      });
                                    }}
                                    onDraftParsed={setJsonExpertDraft}
                                    minHeight={360}
                                  />
                                ) : effectivePayloadSchema ? (
                                  renderField(effectivePayloadSchema, editFormValue, setEditFormValue, facetType)
                                ) : (
                                  <Text size="xs" c="dimmed">Descriptor payload schema unavailable; switch to expert JSON/YAML mode.</Text>
                                )}
                              </>
                            )}
                          </Stack>
                        ) : (
                          readOnlyBody
                        )}
                      </Card>
                    );
                  })}
                </Stack>
              </Tabs.Panel>
            ))}
          </Tabs>
        )}
      </Box>

      <Modal
        opened={facetToDelete !== null}
        onClose={() => setFacetToDelete(null)}
        title={
          facetToDelete == null
            ? ''
            : facetToDelete.instanceIndex === null
              ? 'Confirm facet deletion'
              : 'Remove facet entry'
        }
        centered
      >
        <Stack gap="sm">
          <Text size="sm">
            {facetToDelete == null ? null : facetToDelete.instanceIndex === null ? (
              <>
                Delete entire facet <b>{facetToDelete.facetType}</b> from scope <b>{selectedContext}</b>?
              </>
            ) : (
              <>
                Remove this entry from facet <b>{facetToDelete.facetType}</b> in scope <b>{selectedContext}</b>?
              </>
            )}
          </Text>
          <Group justify="flex-end">
            <Button variant="light" onClick={() => setFacetToDelete(null)}>Cancel</Button>
            <Button
              color="red"
              onClick={() => {
                if (!facetToDelete) return;
                if (facetToDelete.instanceIndex === null) {
                  void deleteFacet(facetToDelete.facetType);
                } else {
                  void deleteFacetInstance(facetToDelete.facetType, facetToDelete.instanceIndex);
                }
              }}
            >
              Delete
            </Button>
          </Group>
        </Stack>
      </Modal>

    </Box>
  );
}
