import type { SchemaEntity, SchemaNode, TableDetail } from '../../types/schema';
import { schemaService } from '../../services/api';
import {
  resolveColumnSyncTarget,
  resolveTreeTableId,
  treeTableHasColumnChildren,
} from './catalogEntityId';
import { enrichNodeChildren } from './schemaTreeEnrichment';

function tableColumnNodes(entity: TableDetail): SchemaNode[] {
  return entity.columns.map((column) => ({
    id: column.id,
    type: 'COLUMN' as const,
    name: column.columnName,
  }));
}

/**
 * Attaches column children to an existing explorer tree when the route or selection
 * targets a table/column. Returns the original tree reference when no change is needed.
 */
export async function enrichExplorerTreeColumns(
  tree: SchemaNode[],
  context: string,
  routeEntityId: string,
  selectedEntity: SchemaEntity | null,
): Promise<SchemaNode[]> {
  const syncTarget = resolveColumnSyncTarget(selectedEntity, routeEntityId);
  if (!syncTarget) {
    return tree;
  }

  const treeTableId =
    resolveTreeTableId(tree, syncTarget.schemaName, syncTarget.tableName) ??
    syncTarget.tableCatalogId;
  if (treeTableHasColumnChildren(tree, treeTableId)) {
    return tree;
  }

  if (selectedEntity?.entityType === 'TABLE' && selectedEntity.columns.length > 0) {
    return enrichNodeChildren(tree, treeTableId, tableColumnNodes(selectedEntity));
  }

  const table = await schemaService.getTable(
    syncTarget.schemaName,
    syncTarget.tableName,
    context,
    'none',
  );
  if (!table?.columns?.length) {
    return tree;
  }
  return enrichNodeChildren(tree, treeTableId, tableColumnNodes(table));
}

/**
 * Loads the explorer tree and, when the route or selection targets a table/column,
 * fetches that table's columns and attaches them before returning.
 */
export async function loadExplorerTreeWithColumns(
  context: string,
  routeEntityId: string,
  selectedEntity: SchemaEntity | null,
): Promise<SchemaNode[]> {
  const loadedTree = await schemaService.getTree(context);
  if (!routeEntityId) {
    return loadedTree;
  }
  return enrichExplorerTreeColumns(loadedTree, context, routeEntityId, selectedEntity);
}
