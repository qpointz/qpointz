export interface KnownTarget {
  key: string;
  label: string;
  slug: string;
  urn: string;
  icon: string;
}

export const KNOWN_TARGETS: KnownTarget[] = [
  {
    key: 'schema',
    label: 'Schema',
    slug: 'schema',
    urn: 'urn:mill/metadata/entity-type:schema',
    icon: 'S',
  },
  {
    key: 'table',
    label: 'Table',
    slug: 'table',
    urn: 'urn:mill/metadata/entity-type:table',
    icon: 'T',
  },
  {
    key: 'column',
    label: 'Column',
    slug: 'column',
    urn: 'urn:mill/metadata/entity-type:attribute',
    icon: 'C',
  },
];

export function normalizeTargetValue(value: string): string {
  const v = value.trim();
  if (!v) return v;
  const bySlug = KNOWN_TARGETS.find((t) => t.slug === v);
  if (bySlug) return bySlug.urn;
  return v;
}

export function targetMeta(value: string): { label: string; icon: string; known: boolean } {
  const byUrn = KNOWN_TARGETS.find((t) => t.urn === value);
  if (byUrn) return { label: byUrn.slug, icon: byUrn.icon, known: true };
  const bySlug = KNOWN_TARGETS.find((t) => t.slug === value);
  if (bySlug) return { label: bySlug.slug, icon: bySlug.icon, known: true };
  return { label: value, icon: '•', known: false };
}

