import type { ExportFormatInfo } from './exportHelpers';
import { buildTableExportUrl, filenameFromContentDisposition, sanitizeExportAttachmentBaseName } from './exportHelpers';

const JSON_HEADERS: HeadersInit = { Accept: 'application/json' };

let cachedFormats: ExportFormatInfo[] | null = null;
let inflightFormats: Promise<ExportFormatInfo[]> | null = null;

/** Clears the in-memory export format list (tests or rare admin refresh). */
export function resetExportFormatsCache(): void {
  cachedFormats = null;
  inflightFormats = null;
}

async function loadExportFormatsFromNetwork(): Promise<ExportFormatInfo[]> {
  const res = await fetch('/services/export/formats', {
    credentials: 'include',
    headers: JSON_HEADERS,
  });
  if (!res.ok) {
    throw new Error(`Export formats failed: ${res.status} ${res.statusText}`);
  }
  const data = (await res.json()) as ExportFormatInfo[];
  if (!Array.isArray(data)) {
    throw new Error('Invalid export formats response');
  }
  return data;
}

/**
 * Loads the effective HTTP format list from {@code GET /services/export/formats}.
 * Results are cached for the browser session; concurrent callers share one in-flight request.
 */
export async function fetchExportFormats(): Promise<ExportFormatInfo[]> {
  if (cachedFormats) {
    return cachedFormats;
  }
  if (!inflightFormats) {
    inflightFormats = loadExportFormatsFromNetwork()
      .then((formats) => {
        cachedFormats = formats;
        return formats;
      })
      .finally(() => {
        inflightFormats = null;
      });
  }
  return inflightFormats;
}

/**
 * GET export URL via {@code fetch} then triggers a download from the blob, using the server
 * {@code Content-Disposition} filename when present (same pattern as {@link downloadSqlExport}).
 * Silent “no prompt” behavior still depends on the browser (e.g. “Ask where to save each file” off).
 */
async function downloadBlobFromGet(url: string, fallbackFilename: string): Promise<void> {
  const res = await fetch(url, { credentials: 'include' });
  if (!res.ok) {
    throw new Error(`Export failed: ${res.status} ${res.statusText}`);
  }
  await consumeBlobResponse(res, fallbackFilename);
}

/**
 * Downloads a table export (streaming GET).
 * Passes {@code filename=<sanitized-table>} so the server sets {@code Content-Disposition} as
 * {@code <table>.<ext>} (extension from the chosen format provider).
 *
 * @param options.fileExtension optional fallback extension when the header cannot be parsed (from format metadata)
 */
export async function downloadTableExport(
  schemaName: string,
  tableName: string,
  formatId: string,
  options?: { fileExtension?: string },
): Promise<void> {
  const stem = sanitizeExportAttachmentBaseName(tableName);
  const ext = (options?.fileExtension?.trim() || formatId).replace(/^\./, '');
  const fallbackFilename = `${stem}.${ext}`;
  const url = buildTableExportUrl(schemaName, tableName, formatId, {
    attachmentBaseName: stem,
  });
  await downloadBlobFromGet(url, fallbackFilename);
}

/**
 * Posts ad-hoc SQL for streaming export ({@code POST /services/export/sql}).
 * When {@code attachmentBaseName} is set, it is sent as {@code filename} so the server emits a matching
 * {@code Content-Disposition} header (still uses {@code fetch} because of the request body).
 */
export async function downloadSqlExport(
  sql: string,
  formatId: string,
  options?: { filenameHint?: string; attachmentBaseName?: string }
): Promise<void> {
  const params = new URLSearchParams();
  params.set('format', formatId);
  const stem = options?.attachmentBaseName?.trim();
  if (stem) {
    params.set('filename', stem);
  }
  const url = `/services/export/sql?${params.toString()}`;
  const fallback =
    options?.filenameHint ?? (stem ? `${stem}.${formatId}` : `export.${formatId}`);
  await downloadBlobFromPostPlainText(url, sql, fallback);
}

async function downloadBlobFromPostPlainText(
  url: string,
  body: string,
  fallbackFilename: string
): Promise<void> {
  const res = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'text/plain;charset=UTF-8' },
    body,
  });
  if (!res.ok) {
    throw new Error(`Export failed: ${res.status} ${res.statusText}`);
  }
  await consumeBlobResponse(res, fallbackFilename);
}

function attachmentDisplayName(filename: string, fallback: string): string {
  const raw = filename.trim().length > 0 ? filename.trim() : fallback;
  const seg = raw.replace(/\\/g, '/').split('/').pop();
  return seg && seg.length > 0 ? seg : fallback;
}

async function consumeBlobResponse(res: Response, fallbackFilename: string): Promise<void> {
  const blob = await res.blob();
  const cd = res.headers.get('Content-Disposition');
  const filename = attachmentDisplayName(filenameFromContentDisposition(cd, fallbackFilename), fallbackFilename);
  const url = URL.createObjectURL(blob);
  try {
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.rel = 'noopener noreferrer';
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  } finally {
    URL.revokeObjectURL(url);
  }
}
