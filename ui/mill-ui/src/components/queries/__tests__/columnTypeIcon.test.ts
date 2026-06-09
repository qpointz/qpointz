import { describe, expect, it } from 'vitest';
import {
  abbreviateColumnType,
  formatColumnTypeLabel,
  isNumericColumnType,
  resolveColumnTypeFamily,
  resolveColumnTypeIcon,
} from '../columnTypeIcon';
import {
  HiOutlineCalendar,
  HiOutlineCheckCircle,
  HiOutlineClock,
  HiOutlineHashtag,
  HiOutlineLanguage,
  HiOutlineQuestionMarkCircle,
  HiOutlineVariable,
} from 'react-icons/hi2';

describe('columnTypeIcon', () => {
  it('should map integer family types', () => {
    expect(resolveColumnTypeFamily('INT')).toBe('integer');
    expect(resolveColumnTypeFamily('big_int')).toBe('integer');
    expect(resolveColumnTypeIcon('smallint')).toBe(HiOutlineHashtag);
  });

  it('should map float family types', () => {
    expect(resolveColumnTypeFamily('DOUBLE')).toBe('float');
    expect(resolveColumnTypeIcon('decimal')).toBe(HiOutlineVariable);
  });

  it('should map string family types', () => {
    expect(resolveColumnTypeFamily('STRING')).toBe('string');
    expect(resolveColumnTypeIcon('varchar')).toBe(HiOutlineLanguage);
  });

  it('should map boolean, date, and time family types', () => {
    expect(resolveColumnTypeFamily('BOOL')).toBe('boolean');
    expect(resolveColumnTypeIcon('boolean')).toBe(HiOutlineCheckCircle);
    expect(resolveColumnTypeFamily('DATE')).toBe('date');
    expect(resolveColumnTypeIcon('date')).toBe(HiOutlineCalendar);
    expect(resolveColumnTypeFamily('TIMESTAMP_TZ')).toBe('time');
    expect(resolveColumnTypeIcon('timestamp')).toBe(HiOutlineClock);
  });

  it('should fall back to unknown for unrecognized types', () => {
    expect(resolveColumnTypeFamily('jsonb')).toBe('unknown');
    expect(resolveColumnTypeIcon('jsonb')).toBe(HiOutlineQuestionMarkCircle);
  });

  it('should format type labels for tooltips', () => {
    expect(formatColumnTypeLabel('int')).toBe('INT');
    expect(formatColumnTypeLabel('timestamp_tz')).toBe('TIMESTAMP TZ');
    expect(formatColumnTypeLabel('')).toBe('UNKNOWN');
  });

  it('should abbreviate common column types for header badges', () => {
    expect(abbreviateColumnType('string')).toBe('STR');
    expect(abbreviateColumnType('timestamp_tz')).toBe('TSTZ');
    expect(abbreviateColumnType('double')).toBe('DOUBLE');
  });

  it('should treat numeric families as right-aligned', () => {
    expect(isNumericColumnType('INT')).toBe(true);
    expect(isNumericColumnType('varchar')).toBe(false);
  });
});
