/**
 * Pure helpers for the streaming export HTTP API (`/services/export`).
 */

export interface ExportFormatInfo {
  id: string;
  mediaType: string;
  fileExtension: string;
}

/**
 * Picks the default format for primary Export clicks.
 *
 * Rule: prefer `csv` when the server lists it (case-insensitive); otherwise the first format
 * when ids are sorted lexicographically (stable, deterministic).
 */
export function pickDefaultExportFormatId(formats: ExportFormatInfo[]): string {
  if (formats.length === 0) {
    return 'csv';
  }
  const csv = formats.find((f) => f.id.toLowerCase() === 'csv');
  if (csv) {
    return csv.id;
  }
  const sorted = [...formats].sort((a, b) => a.id.localeCompare(b.id));
  return sorted[0]?.id ?? 'csv';
}

/**
 * Sanitizes a logical table (or similar) name for use as an attachment base name.
 * Aligns loosely with {@code ExportFilenameSanitizer} on the server (no path segments).
 *
 * @param raw physical or logical table name from the model
 * @return non-empty safe stem without extension
 */
export function sanitizeExportAttachmentBaseName(raw: string): string {
  const t = raw
    .trim()
    .replace(/\\/g, '_')
    .replace(/\//g, '_')
    .replace(/\.\./g, '_');
  return t.length > 0 ? t : 'export';
}

export type BuildTableExportUrlOptions = {
  /** Passed as {@code filename} query param so the server sets {@code Content-Disposition} (extension added server-side). */
  attachmentBaseName?: string;
};

/**
 * Builds a relative URL for {@code GET /services/export/schemas/.../tables/...}.
 *
 * @param schemaName physical schema
 * @param tableName physical table
 * @param formatId export format id ({@code ?format=})
 * @param options optional {@code filename} hint for the download name stem
 */
export function buildTableExportUrl(
  schemaName: string,
  tableName: string,
  formatId: string,
  options?: BuildTableExportUrlOptions,
): string {
  const base = `/services/export/schemas/${encodeURIComponent(schemaName)}/tables/${encodeURIComponent(tableName)}`;
  const params = new URLSearchParams();
  params.set('format', formatId);
  const stem = options?.attachmentBaseName?.trim();
  if (stem) {
    params.set('filename', stem);
  }
  return `${base}?${params.toString()}`;
}

/**
 * Parses `Content-Disposition` for a filename (RFC 5987 `filename*` or `filename`).
 */
export function filenameFromContentDisposition(header: string | null, fallback: string): string {
  if (!header || header.trim() === '') {
    return fallback;
  }
  const star = /filename\*\s*=\s*([^']*)''([^;\n]+)/i.exec(header);
  if (star?.[2]) {
    try {
      return decodeURIComponent(star[2].trim());
    } catch {
      /* fall through */
    }
  }
  const quoted = /filename\s*=\s*"([^"]+)"/i.exec(header);
  if (quoted?.[1]) {
    return quoted[1].trim();
  }
  const plain = /filename\s*=\s*([^;\n]+)/i.exec(header);
  if (plain?.[1]) {
    return plain[1].trim().replace(/^["']|["']$/g, '');
  }
  return fallback;
}
