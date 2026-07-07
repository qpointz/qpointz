import { Box } from '@mantine/core';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { notifications } from '@mantine/notifications';
import { DataGrid, DataStatePanel } from './DataGrid';
import { DataErrorPanel } from './DataErrorPanel';
import { DataLoadingPanel } from './DataLoadingPanel';
import { DataToolbar } from './DataToolbar';
import { buildQueryPageLabel } from './dataPagination';
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
  deferErrorMs = 0,
}: QueryDataViewProps) {
  const [exportFormats, setExportFormats] = useState<ExportFormatInfo[]>([]);
  const [exportFormatsLoading, setExportFormatsLoading] = useState(false);
  const [exportFormatsFailed, setExportFormatsFailed] = useState(false);
  const [exportingFormatId, setExportingFormatId] = useState<string | null>(null);
  const [deferredErrorVisible, setDeferredErrorVisible] = useState(deferErrorMs <= 0);

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

  useEffect(() => {
    if (!error || deferErrorMs <= 0 || result || isExecuting || isPageLoading) {
      setDeferredErrorVisible(deferErrorMs <= 0 || !error);
      return undefined;
    }
    setDeferredErrorVisible(false);
    const timer = window.setTimeout(() => setDeferredErrorVisible(true), deferErrorMs);
    return () => window.clearTimeout(timer);
  }, [deferErrorMs, error, isExecuting, isPageLoading, result]);

  const hasSql = Boolean(currentSql.trim());
  const pageLabel = buildQueryPageLabel(result);

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
  const showToolbar = mode === 'playground' && (result != null || isExecuting || isPageLoading);

  const content = useMemo(() => {
    if (isExecuting) {
      return <DataLoadingPanel compact={compactMode} />;
    }
    if (result) {
      return (
        <DataGrid
          result={result}
          maxHeight={gridMaxHeight}
          loading={isPageLoading}
        />
      );
    }
    if (isPageLoading) {
      return <DataLoadingPanel compact={compactMode} />;
    }
    if (error && !deferredErrorVisible) {
      return <DataLoadingPanel compact={compactMode} />;
    }
    if (error) return <DataErrorPanel message={error} compact={compactMode} />;
    return (
      <DataStatePanel
        message="Run a query to see results here"
        compact={compactMode}
      />
    );
  }, [compactMode, deferredErrorVisible, error, gridMaxHeight, isExecuting, isPageLoading, result]);

  return (
    <Box style={{ display: 'flex', flexDirection: 'column', height: '100%', minHeight: 0 }}>
      {showToolbar ? (
        <DataToolbar
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
