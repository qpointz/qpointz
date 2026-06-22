import type { ChatMessageArtifact } from '../types/chat';
import type { ArtifactResponseWire } from '../types/chatWire';
import type { QueryColumn } from '../types/query';
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

function parseSqlArtifact(payload: Record<string, unknown>): ChatMessageArtifact | null {
  const sql = typeof payload.sql === 'string' ? payload.sql : '';
  if (!sql.trim()) return null;
  return {
    kind: 'sql',
    sql,
    dialectId: typeof payload.dialectId === 'string' ? payload.dialectId : undefined,
  };
}

function parseDataArtifact(payload: Record<string, unknown>): ChatMessageArtifact | null {
  const executionId = typeof payload.executionId === 'string' ? payload.executionId : '';
  if (!executionId) return null;
  return {
    kind: 'data',
    executionId,
    sql: typeof payload.sql === 'string' ? payload.sql : undefined,
    rowCount: asNumber(payload.rowCount),
    truncated: typeof payload.truncated === 'boolean' ? payload.truncated : undefined,
    columns: asColumns(payload.columns),
  };
}

export function parseArtifactWire(wire: ArtifactResponseWire): ChatMessageArtifact | null {
  const payload = wire.payload ?? {};
  if (wire.kind === 'sql') return parseSqlArtifact(payload);
  if (wire.kind === 'data') return parseDataArtifact(payload);
  if (wire.kind === 'facet-proposal' || wire.kind === 'schema-capture') {
    return parseFacetProposalArtifact(payload);
  }
  return null;
}

export function parseWireArtifacts(wire: ArtifactResponseWire[] | undefined): ChatMessageArtifact[] {
  if (!wire?.length) return [];
  return wire
    .map(parseArtifactWire)
    .filter((artifact): artifact is ChatMessageArtifact => artifact !== null);
}
