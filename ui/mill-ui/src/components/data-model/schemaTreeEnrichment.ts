import type { SchemaNode } from '../../types/schema';
import { catalogIdsEqual } from './catalogEntityId';

/**
 * Replaces children on the node whose catalog id matches {@link targetCatalogId}
 * (case-insensitive), recursing through the explorer tree.
 */
export function enrichNodeChildren(
  nodes: SchemaNode[],
  targetCatalogId: string,
  children: SchemaNode[],
): SchemaNode[] {
  return nodes.map((node) => {
    if (catalogIdsEqual(node.id, targetCatalogId)) {
      return { ...node, children };
    }
    if (!node.children || node.children.length === 0) {
      return node;
    }
    return { ...node, children: enrichNodeChildren(node.children, targetCatalogId, children) };
  });
}
