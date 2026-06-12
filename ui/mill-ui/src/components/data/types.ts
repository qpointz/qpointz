import type { QueryResult } from '../../types/query';

export type QueryDataViewMode = 'playground' | 'condensed' | 'expanded';

export interface QueryDataViewProps {
  mode: QueryDataViewMode;
  result: QueryResult | null;
  error: string | null;
  isExecuting: boolean;
  isPageLoading?: boolean;
  pageSize?: number;
  onPageSizeChange?: (pageSize: number) => void;
  currentSql?: string;
  exportAttachmentBaseName?: string;
  onFormatSql?: () => void;
  onCopySql?: () => void;
  onClearSql?: () => void;
  sqlCopied?: boolean;
  onPageChange?: (pageIndex: number) => void;
  /** When false, hides the export control in the data toolbar (chat action bar owns export). */
  showExport?: boolean;
}
