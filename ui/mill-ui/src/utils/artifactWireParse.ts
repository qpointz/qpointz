import type { ChatMessageArtifact } from '../types/chat';
import type { ArtifactResponseWire } from '../types/chatWire';
import type { QueryColumn } from '../types/query';
import { chartVisualizationsFromPayload } from '../components/charts/chartData';
import { parseFacetProposalArtifact } from './facetWireNormalize';

function asNumber(value: unknown): number | undefined {
  if (typeof value === 'number' && Number.isFinite(value)) return value;
  if (typeof value === 'string' && value.trim().length > 0) {
    const parsed = Number(value);
    if (Number.isFinite(parsed)) return parsed;
  }
  return undefined;
}

function asColumns(value: unknown): QueryColumn[] | undefined {
  if (!Array.isArray(value)) return undefined;
  const columns = value
    .map((entry) => {
      if (!entry || typeof entry !== 'object') return null;
      const row = entry as Record<string, unknown>;
      const name = typeof row.name === 'string' ? row.name : '';
      const type = typeof row.type === 'string' ? row.type : 'unknown';
      if (!name) return null;
      return { name, type };
    })
    .filter((column): column is QueryColumn => column !== null);
  return columns.length ? columns : undefined;
}

function asSqlInfo(value: unknown): { title?: string; description?: string } | undefined {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return undefined;
  const row = value as Record<string, unknown>;
  const title = typeof row.title === 'string' ? row.title : undefined;
  const description = typeof row.description === 'string' ? row.description : undefined;
  if (!title && !description) return undefined;
  return {
    ...(title ? { title } : {}),
    ...(description ? { description } : {}),
  };
}

function wireMeta(wire: ArtifactResponseWire): { artifactId?: string; status?: string } {
  return {
    artifactId: wire.artifactId ?? undefined,
    status: wire.status ?? undefined,
  };
}

function parseSqlArtifact(wire: ArtifactResponseWire): ChatMessageArtifact | null {
  const payload = wire.payload ?? {};
  const sql = typeof payload.sql === 'string' ? payload.sql : '';
  if (!sql.trim()) return null;
  const info = asSqlInfo(payload.info);
  const schema = asColumns(payload.schema);
  const visualizations = chartVisualizationsFromPayload(payload.visualizations);
  return {
    kind: 'sql',
    sql,
    dialectId: typeof payload.dialectId === 'string' ? payload.dialectId : undefined,
    ...(info ? { info } : {}),
    ...(schema ? { schema } : {}),
    ...(visualizations.length > 0 ? { visualizations } : {}),
    ...wireMeta(wire),
  };
}

function parseDataArtifact(wire: ArtifactResponseWire): ChatMessageArtifact | null {
  const payload = wire.payload ?? {};
  const sql = typeof payload.sql === 'string' ? payload.sql : undefined;
  const rowCount = asNumber(payload.rowCount);
  const columns = asColumns(payload.columns);
  const sourceArtifactId =
    typeof payload.sourceArtifactId === 'string' ? payload.sourceArtifactId : undefined;
  // executionId is ephemeral query-session state — never hydrate it from the wire.
  if (!sql && rowCount == null && !columns?.length && !sourceArtifactId) {
    return null;
  }
  return {
    kind: 'data',
    sql,
    rowCount,
    truncated: typeof payload.truncated === 'boolean' ? payload.truncated : undefined,
    columns,
    sourceArtifactId,
    ...wireMeta(wire),
  };
}

export function parseArtifactWire(wire: ArtifactResponseWire): ChatMessageArtifact | null {
  const payload = wire.payload ?? {};
  if (wire.kind === 'sql') return parseSqlArtifact(wire);
  if (wire.kind === 'data') return parseDataArtifact(wire);
  if (wire.kind === 'facet-proposal' || wire.kind === 'schema-capture') {
    const facet = parseFacetProposalArtifact(payload);
    if (!facet) return null;
    return { ...facet, ...wireMeta(wire) };
  }
  return null;
}

export function parseWireArtifacts(wire: ArtifactResponseWire[] | undefined): ChatMessageArtifact[] {
  if (!wire?.length) return [];
  return wire
    .map(parseArtifactWire)
    .filter((artifact): artifact is ChatMessageArtifact => artifact !== null);
}
