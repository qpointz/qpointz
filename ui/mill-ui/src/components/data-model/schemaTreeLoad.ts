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
 * Loads the explorer tree and, when the route or selection targets a table/column,
 * fetches that table's columns and attaches them before returning.
 */
export async function loadExplorerTreeWithColumns(
  context: string,
  routeEntityId: string,
  selectedEntity: SchemaEntity | null,
): Promise<SchemaNode[]> {
  const loadedTree = await schemaService.getTree(context);
  const syncTarget = resolveColumnSyncTarget(selectedEntity, routeEntityId);
  if (!syncTarget) {
    return loadedTree;
  }

  const treeTableId =
    resolveTreeTableId(loadedTree, syncTarget.schemaName, syncTarget.tableName) ??
    syncTarget.tableCatalogId;
  if (treeTableHasColumnChildren(loadedTree, treeTableId)) {
    return loadedTree;
  }

  if (selectedEntity?.entityType === 'TABLE' && selectedEntity.columns.length > 0) {
    return enrichNodeChildren(loadedTree, treeTableId, tableColumnNodes(selectedEntity));
  }

  const table = await schemaService.getTable(
    syncTarget.schemaName,
    syncTarget.tableName,
    context,
    'none',
  );
  if (!table?.columns?.length) {
    return loadedTree;
  }
  return enrichNodeChildren(loadedTree, treeTableId, tableColumnNodes(table));
}
