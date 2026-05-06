import { describe, it, expect } from 'vitest';
import {
  pickDefaultExportFormatId,
  buildTableExportUrl,
  sanitizeExportAttachmentBaseName,
  filenameFromContentDisposition,
  type ExportFormatInfo,
} from '../exportHelpers';

describe('exportHelpers', () => {
  describe('pickDefaultExportFormatId', () => {
    it('prefers csv when present (case-insensitive)', () => {
      const formats: ExportFormatInfo[] = [
        { id: 'avro', mediaType: 'application/avro', fileExtension: 'avro' },
        { id: 'CSV', mediaType: 'text/csv', fileExtension: 'csv' },
        { id: 'json', mediaType: 'application/json', fileExtension: 'json' },
      ];
      expect(pickDefaultExportFormatId(formats)).toBe('CSV');
    });

    it('uses first id alphabetically when csv absent', () => {
      const formats: ExportFormatInfo[] = [
        { id: 'tsv', mediaType: 'text/tab-separated-values', fileExtension: 'tsv' },
        { id: 'avro', mediaType: 'application/avro', fileExtension: 'avro' },
      ];
      expect(pickDefaultExportFormatId(formats)).toBe('avro');
    });

    it('falls back to csv token when list empty', () => {
      expect(pickDefaultExportFormatId([])).toBe('csv');
    });
  });

  describe('sanitizeExportAttachmentBaseName', () => {
    it('trims and replaces path-like segments', () => {
      expect(sanitizeExportAttachmentBaseName('  orders  ')).toBe('orders');
      expect(sanitizeExportAttachmentBaseName('a\\b/c..x')).toBe('a_b_c_x');
    });

    it('uses export when empty after trim', () => {
      expect(sanitizeExportAttachmentBaseName('   ')).toBe('export');
    });
  });

  describe('buildTableExportUrl', () => {
    it('encodes schema, table, and format query', () => {
      expect(buildTableExportUrl('sales', 'orders', 'csv')).toBe(
        '/services/export/schemas/sales/tables/orders?format=csv',
      );
      expect(buildTableExportUrl('meta schema', 't1', 'json')).toBe(
        '/services/export/schemas/meta%20schema/tables/t1?format=json',
      );
    });

    it('adds filename query param when attachment stem is set', () => {
      expect(buildTableExportUrl('sales', 'orders', 'csv', { attachmentBaseName: 'orders' })).toBe(
        '/services/export/schemas/sales/tables/orders?format=csv&filename=orders',
      );
    });
  });

  describe('filenameFromContentDisposition', () => {
    it('parses quoted filename', () => {
      expect(
        filenameFromContentDisposition('attachment; filename="my-file.csv"', 'fallback.csv'),
      ).toBe('my-file.csv');
    });

    it('uses fallback when header missing', () => {
      expect(filenameFromContentDisposition(null, 'f.csv')).toBe('f.csv');
    });
  });
});
