import type { SchemaEntity, SchemaNode } from '../../types/schema';

/** Normalizes dotted catalog ids for case-insensitive comparison. */
export function normalizeCatalogId(id: string): string {
  return id.trim().toLowerCase();
}

/** Case-insensitive equality for schema explorer dotted ids. */
export function catalogIdsEqual(a: string, b: string): boolean {
  return normalizeCatalogId(a) === normalizeCatalogId(b);
}

/**
 * True when {@link id} equals {@link prefix} or is a descendant (`prefix.segment…`).
 */
export function catalogIdStartsWith(id: string, prefix: string): boolean {
  const normId = normalizeCatalogId(id);
  const normPrefix = normalizeCatalogId(prefix);
  return normId === normPrefix || normId.startsWith(`${normPrefix}.`);
}

/**
 * Finds the tree node's canonical `id` for a catalog path (case-insensitive).
 */
export function findTreeNodeId(tree: readonly SchemaNode[], catalogId: string): string | null {
  const target = normalizeCatalogId(catalogId);
  for (const node of tree) {
    if (normalizeCatalogId(node.id) === target) {
      return node.id;
    }
    if (node.children?.length) {
      const found = findTreeNodeId(node.children, catalogId);
      if (found) return found;
    }
  }
  return null;
}

/**
 * Resolves the table node id present in {@link tree} for schema/table coordinates.
 */
export function resolveTreeTableId(
  tree: readonly SchemaNode[],
  schemaName: string,
  tableName: string,
): string | null {
  return findTreeNodeId(tree, `${schemaName}.${tableName}`);
}

export interface ColumnSyncTarget {
  schemaName: string;
  tableName: string;
  tableCatalogId: string;
}

/**
 * Derives which table's columns should be loaded into the explorer tree from the
 * selected entity and/or deep-link route id (works before entity fetch completes).
 */
export function resolveColumnSyncTarget(
  entity: SchemaEntity | null,
  routeEntityId: string,
): ColumnSyncTarget | null {
  if (entity?.entityType === 'TABLE') {
    return {
      schemaName: entity.schemaName,
      tableName: entity.tableName,
      tableCatalogId: entity.id,
    };
  }
  if (entity?.entityType === 'COLUMN') {
    return {
      schemaName: entity.schemaName,
      tableName: entity.tableName,
      tableCatalogId: `${entity.schemaName}.${entity.tableName}`,
    };
  }
  const parts = routeEntityId.split('.').filter(Boolean);
  if (parts.length >= 2) {
    const schemaName = parts[0]!;
    const tableName = parts[1]!;
    return {
      schemaName,
      tableName,
      tableCatalogId: `${schemaName}.${tableName}`,
    };
  }
  return null;
}

/**
 * Returns true when the table node already has column children in the explorer tree.
 */
export function treeTableHasColumnChildren(tree: readonly SchemaNode[], tableCatalogId: string): boolean {
  for (const node of tree) {
    if (node.type === 'TABLE' && catalogIdsEqual(node.id, tableCatalogId)) {
      return Boolean(node.children && node.children.length > 0);
    }
    if (node.children?.length && treeTableHasColumnChildren(node.children, tableCatalogId)) {
      return true;
    }
  }
  return false;
}
