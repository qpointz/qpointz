import type { ReactNode } from 'react';
import {
  ActionIcon,
  Alert,
  Anchor,
  Badge,
  Box,
  Divider,
  Group,
  Stack,
  Text,
  Tooltip,
} from '@mantine/core';
import {
  HiOutlineArrowTopRightOnSquare,
  HiOutlineEnvelope,
  HiOutlineInformationCircle,
} from 'react-icons/hi2';
import type { FacetPayloadSchema } from '../../../types/facetTypes';
import {
  facetHyperlinkHref,
  facetHyperlinkPresentationActive,
  facetMailtoHref,
  facetStringStereotype,
  facetTagsPresentationActive,
  type FacetStringStereotypeKind,
} from '../../../utils/facetStereotype';
import { renderGenericFacetObjectReadOnly } from '../genericFacetObjectReadOnly';

/**
 * Read-only display for hyperlink stereotype: plain string = URL text + href; object = `title` (optional) + `href`
 * (required). Opens in a new tab with an external-link icon.
 */
export function FacetHyperlinkReadOnly({ value }: { value: unknown }) {
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
export function FacetEmailReadOnly({ value }: { value: unknown }) {
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

export interface FacetPayloadReadOnlyProps {
  schema: FacetPayloadSchema;
  value: unknown;
  keyPrefix: string;
  stringStereotype?: FacetStringStereotypeKind;
  /** Field-level stereotype wire (for tags / empty-array behaviour). */
  fieldStereotypeWire?: string | string[] | null;
}

/** Schema-driven recursive read-only facet payload renderer (shared by Data Model and chat). */
export function FacetPayloadReadOnly({
  schema,
  value,
  keyPrefix,
  stringStereotype = 'none',
  fieldStereotypeWire,
}: FacetPayloadReadOnlyProps): ReactNode {
  const labelWithInfo = (title: string, description?: string) => (
    <Group gap={4} wrap="nowrap" align="center">
      {description && (
        <Tooltip label={description} withArrow>
          <ActionIcon size="xs" variant="subtle" aria-label="Field description">
            <HiOutlineInformationCircle size={12} />
          </ActionIcon>
        </Tooltip>
      )}
      <Text size="xs" fw={400} c="dimmed">
        {title}
      </Text>
    </Group>
  );

  const primitiveValue = (s: FacetPayloadSchema, v: unknown): ReactNode => {
    if (s.type === 'BOOLEAN') {
      return (
        <Badge size="xs" variant="light" color={v ? 'green' : 'gray'}>
          {String(Boolean(v))}
        </Badge>
      );
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
    const raw = value;
    const declared = schema.fields ?? [];
    if (declared.length === 0) {
      if (raw == null) {
        return (
          <Text size="xs" c="dimmed">
            —
          </Text>
        );
      }
      if (typeof raw === 'object' && !Array.isArray(raw)) {
        return renderGenericFacetObjectReadOnly(raw as Record<string, unknown>);
      }
      return (
        <Text size="xs" ff="monospace" style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
          {JSON.stringify(raw, null, 2)}
        </Text>
      );
    }
    const obj = (raw && typeof raw === 'object' && !Array.isArray(raw) ? raw : {}) as Record<string, unknown>;
    return (
      <Stack gap={6}>
        {declared.map((field) => (
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
                  <FacetPayloadReadOnly
                    schema={field.schema}
                    value={obj[field.name]}
                    keyPrefix={`${keyPrefix}.${field.name}`}
                    stringStereotype={facetStringStereotype(field.schema, field.stereotype)}
                    fieldStereotypeWire={field.stereotype}
                  />
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
      if (itemSchema.type === 'STRING' && (listHyperlink || stringStereotype === 'email')) {
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
              {schema.items ? (
                <FacetPayloadReadOnly
                  schema={schema.items}
                  value={item}
                  keyPrefix={`${keyPrefix}[${idx}]`}
                  stringStereotype="none"
                />
              ) : (
                <Text size="xs">{String(item)}</Text>
              )}
            </Box>
          </Group>
        ))}
      </Stack>
    );
  }
  return primitiveValue(schema, value);
}
