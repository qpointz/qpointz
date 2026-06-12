import { Box } from '@mantine/core';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { notifications } from '@mantine/notifications';
import { DataGrid, DataStatePanel } from './DataGrid';
import { DataToolbar } from './DataToolbar';
import type { QueryDataViewProps } from './types';
import { downloadSqlExport, fetchExportFormats, type ExportFormatInfo } from '../../services/api';

export function QueryDataView({
  mode,
  result,
  error,
  isExecuting,
  isPageLoading = false,
  pageSize,
  onPageSizeChange,
  currentSql = '',
  exportAttachmentBaseName = 'query-results',
  onFormatSql,
  onCopySql,
  onClearSql,
  sqlCopied = false,
  onPageChange,
  showExport = true,
}: QueryDataViewProps) {
  const [exportFormats, setExportFormats] = useState<ExportFormatInfo[]>([]);
  const [exportFormatsLoading, setExportFormatsLoading] = useState(false);
  const [exportFormatsFailed, setExportFormatsFailed] = useState(false);
  const [exportingFormatId, setExportingFormatId] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setExportFormatsLoading(true);
    setExportFormatsFailed(false);
    void fetchExportFormats()
      .then((formats) => {
        if (!cancelled) setExportFormats(formats);
      })
      .catch(() => {
        if (!cancelled) {
          setExportFormatsFailed(true);
          setExportFormats([]);
        }
      })
      .finally(() => {
        if (!cancelled) setExportFormatsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const hasSql = Boolean(currentSql.trim());
  const totalPages = result?.page.totalResult != null && result.page.pageSize > 0
    ? Math.max(1, Math.ceil(result.page.totalResult / result.page.pageSize))
    : null;
  const pageLabel = result
    ? totalPages != null
      ? `Page ${result.page.pageIndex + 1} / ${totalPages}`
      : `Page ${result.page.pageIndex + 1}`
    : 'Page 0';

  const runSqlExport = useCallback(async (formatId: string) => {
    const sql = currentSql.trim();
    if (!sql) return;
    const meta = exportFormats.find((format) => format.id.toLowerCase() === formatId.toLowerCase());
    const ext = (meta?.fileExtension?.trim() || formatId).replace(/^\./, '');
    setExportingFormatId(formatId);
    try {
      await downloadSqlExport(sql, formatId, {
        filenameHint: `${exportAttachmentBaseName}.${ext}`,
        attachmentBaseName: exportAttachmentBaseName,
      });
    } catch (exportError) {
      notifications.show({
        color: 'red',
        title: 'Export failed',
        message: exportError instanceof Error ? exportError.message : 'Unknown error',
      });
    } finally {
      setExportingFormatId(null);
    }
  }, [currentSql, exportAttachmentBaseName, exportFormats]);

  const disablePaginationControls = isPageLoading || isExecuting || !result;
  const showSqlActions = mode === 'playground';
  const compactMode = mode === 'condensed';
  const gridMaxHeight = compactMode ? 280 : undefined;
  const showToolbar = !compactMode || result != null || isExecuting || isPageLoading;

  const content = useMemo(() => {
    if (isExecuting) return <DataStatePanel message="Executing query..." compact={compactMode} />;
    if (isPageLoading) return <DataStatePanel message="Loading page..." compact={compactMode} />;
    if (error) return <DataStatePanel message={error} color="red" compact={compactMode} />;
    if (!result) {
      return (
        <DataStatePanel
          message="Run a query to see results here"
          compact={compactMode}
        />
      );
    }
    return <DataGrid result={result} maxHeight={gridMaxHeight} />;
  }, [compactMode, error, gridMaxHeight, isExecuting, isPageLoading, result]);

  return (
    <Box style={{ display: 'flex', flexDirection: 'column', height: '100%', minHeight: 0 }}>
      {showToolbar ? (
        <DataToolbar
          title="Results"
          pageSize={pageSize}
          onPageSizeChange={onPageSizeChange}
          disablePaginationControls={disablePaginationControls}
          hasPrevious={result?.page.hasPrevious ?? false}
          hasNext={result?.page.hasNext ?? false}
          pageLabel={pageLabel}
          onPrevPage={result && onPageChange ? () => onPageChange(result.page.pageIndex - 1) : undefined}
          onNextPage={result && onPageChange ? () => onPageChange(result.page.pageIndex + 1) : undefined}
          showSqlActions={showSqlActions}
          hasSql={hasSql}
          sqlCopied={sqlCopied}
          onFormatSql={onFormatSql}
          onCopySql={onCopySql}
          onClearSql={onClearSql}
          exportFormats={exportFormats}
          exportFormatsLoading={exportFormatsLoading}
          exportFormatsFailed={exportFormatsFailed}
          exportingFormatId={exportingFormatId}
          onExport={runSqlExport}
          showExport={showExport}
        />
      ) : null}
      <Box style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
        {content}
      </Box>
    </Box>
  );
}
