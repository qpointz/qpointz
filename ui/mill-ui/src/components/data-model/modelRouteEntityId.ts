import type { SchemaNode } from '../../types/schema';
import { catalogIdStartsWith, catalogIdsEqual } from './catalogEntityId';

/**
 * Builds the dotted entity id used by schema explorer APIs from `/model/:schema/:table/:attribute` params.
 */
export function entityIdFromModelRouteParams(params: {
  schema?: string;
  table?: string;
  attribute?: string;
}): string {
  if (!params.schema) return '';
  let entityId = params.schema;
  if (params.table) {
    entityId += `.${params.table}`;
    if (params.attribute) {
      entityId += `.${params.attribute}`;
    }
  }
  return entityId;
}

export { treeTableHasColumnChildren } from './catalogEntityId';

/**
 * Returns node ids that must be expanded so {@link selectedId} is visible in the tree.
 * Handles lazy-loaded table columns via prefix match on the selected dotted id.
 */
export function collectTreeExpansionIds(tree: SchemaNode[], selectedId: string): Set<string> {
  const expand = new Set<string>();

  function walk(nodes: SchemaNode[], ancestors: string[]): boolean {
    for (const node of nodes) {
      if (catalogIdsEqual(node.id, selectedId)) {
        ancestors.forEach((id) => expand.add(id));
        return true;
      }

      const lineage = [...ancestors, node.id];
      if (node.children && node.children.length > 0) {
        if (walk(node.children, lineage)) {
          expand.add(node.id);
          return true;
        }
      }

      if (catalogIdStartsWith(selectedId, node.id)) {
        lineage.forEach((id) => expand.add(id));
        return true;
      }
    }
    return false;
  }

  walk(tree, []);
  return expand;
}

/**
 * Model root ids (or top-level nodes when no model root) expanded on first render.
 */
export function defaultExpandedRootIds(tree: SchemaNode[]): Set<string> {
  const ids = new Set<string>();
  const modelRoots = tree.filter((node) => node.type === 'MODEL');
  if (modelRoots.length > 0) {
    modelRoots.forEach((node) => ids.add(node.id));
    return ids;
  }
  tree.forEach((node) => ids.add(node.id));
  return ids;
}
