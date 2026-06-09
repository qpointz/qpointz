import { Badge, Box, Tooltip } from '@mantine/core';
import type { ComponentType } from 'react';
import {
  HiOutlineCalendar,
  HiOutlineCheckCircle,
  HiOutlineClock,
  HiOutlineCodeBracket,
  HiOutlineFingerPrint,
  HiOutlineHashtag,
  HiOutlineLanguage,
  HiOutlineQuestionMarkCircle,
  HiOutlineVariable,
} from 'react-icons/hi2';

/** Logical column families used for icon selection in the results grid. */
export type ColumnTypeFamily =
  | 'integer'
  | 'float'
  | 'string'
  | 'boolean'
  | 'date'
  | 'time'
  | 'binary'
  | 'uuid'
  | 'unknown';

const INTEGER_TYPES = new Set([
  'int',
  'integer',
  'tiny_int',
  'tinyint',
  'small_int',
  'smallint',
  'big_int',
  'bigint',
]);

const FLOAT_TYPES = new Set([
  'float',
  'double',
  'decimal',
  'numeric',
  'real',
  'number',
]);

const STRING_TYPES = new Set([
  'string',
  'varchar',
  'char',
  'character',
  'text',
  'clob',
  'nchar',
  'nvarchar',
]);

const BOOLEAN_TYPES = new Set(['bool', 'boolean']);

const DATE_TYPES = new Set(['date']);

const TIME_TYPES = new Set([
  'time',
  'timestamp',
  'timestamp_tz',
  'datetime',
  'interval_day',
  'interval_year',
]);

const BINARY_TYPES = new Set(['binary', 'blob', 'bytes', 'varbinary']);

const UUID_TYPES = new Set(['uuid']);

const FAMILY_ICON: Record<ColumnTypeFamily, ComponentType<{ size?: number; color?: string }>> = {
  integer: HiOutlineHashtag,
  float: HiOutlineVariable,
  string: HiOutlineLanguage,
  boolean: HiOutlineCheckCircle,
  date: HiOutlineCalendar,
  time: HiOutlineClock,
  binary: HiOutlineCodeBracket,
  uuid: HiOutlineFingerPrint,
  unknown: HiOutlineQuestionMarkCircle,
};

/**
 * Normalizes a backend or inferred column type string for family lookup.
 *
 * @param type raw column type from query schema
 */
export function normalizeColumnTypeKey(type: string): string {
  return type.trim().toLowerCase().replace(/\s+/g, '_');
}

/**
 * Maps a column type string to a display family for icon selection.
 *
 * @param type raw column type from query schema
 */
export function resolveColumnTypeFamily(type: string): ColumnTypeFamily {
  const key = normalizeColumnTypeKey(type);
  if (INTEGER_TYPES.has(key)) {
    return 'integer';
  }
  if (FLOAT_TYPES.has(key)) {
    return 'float';
  }
  if (STRING_TYPES.has(key)) {
    return 'string';
  }
  if (BOOLEAN_TYPES.has(key)) {
    return 'boolean';
  }
  if (DATE_TYPES.has(key)) {
    return 'date';
  }
  if (TIME_TYPES.has(key)) {
    return 'time';
  }
  if (BINARY_TYPES.has(key)) {
    return 'binary';
  }
  if (UUID_TYPES.has(key)) {
    return 'uuid';
  }
  return 'unknown';
}

/**
 * Returns the icon component for a column type.
 *
 * @param type raw column type from query schema
 */
export function resolveColumnTypeIcon(
  type: string,
): ComponentType<{ size?: number; color?: string }> {
  return FAMILY_ICON[resolveColumnTypeFamily(type)];
}

/**
 * Formats a column type for tooltip display (e.g. {@code timestamp_tz} → {@code TIMESTAMP TZ}).
 *
 * @param type raw column type from query schema
 */
export function formatColumnTypeLabel(type: string): string {
  const trimmed = type.trim();
  if (!trimmed) {
    return 'UNKNOWN';
  }
  return trimmed.replace(/_/g, ' ').toUpperCase();
}

const TYPE_ABBREVIATIONS: Record<string, string> = {
  int: 'INT',
  integer: 'INT',
  tiny_int: 'TINT',
  tinyint: 'TINT',
  small_int: 'SINT',
  smallint: 'SINT',
  big_int: 'BIGINT',
  bigint: 'BIGINT',
  float: 'FLOAT',
  double: 'DOUBLE',
  decimal: 'DEC',
  numeric: 'NUM',
  real: 'REAL',
  string: 'STR',
  varchar: 'VCHAR',
  char: 'CHAR',
  text: 'TEXT',
  bool: 'BOOL',
  boolean: 'BOOL',
  date: 'DATE',
  time: 'TIME',
  timestamp: 'TS',
  timestamp_tz: 'TSTZ',
  datetime: 'DT',
  uuid: 'UUID',
  binary: 'BIN',
};

/**
 * Short column-type code for compact grid headers (tooltip carries the full type name).
 *
 * @param type raw column type from query schema
 */
export function abbreviateColumnType(type: string): string {
  const key = normalizeColumnTypeKey(type);
  const mapped = TYPE_ABBREVIATIONS[key];
  if (mapped) {
    return mapped;
  }
  const label = formatColumnTypeLabel(type);
  if (label.length <= 6) {
    return label.replace(/\s+/g, '');
  }
  return label
    .split(/\s+/)
    .map((part) => part.charAt(0))
    .join('')
    .slice(0, 4);
}

/** Whether a column type should be right-aligned in the results grid. */
export function isNumericColumnType(type: string): boolean {
  const family = resolveColumnTypeFamily(type);
  return family === 'integer' || family === 'float';
}

export interface ColumnTypeBadgeProps {
  /** Raw column type from query schema. */
  type: string;
}

/**
 * Compact monospace type pill for result column headers (full type name on hover).
 *
 * @param props badge props
 */
export function ColumnTypeBadge({ type }: ColumnTypeBadgeProps) {
  const label = formatColumnTypeLabel(type);
  return (
    <Tooltip label={label} withArrow withinPortal zIndex={400}>
      <Badge
        size="xs"
        variant="outline"
        color="gray"
        ff="monospace"
        style={{
          textTransform: 'none',
          fontWeight: 500,
          cursor: 'default',
          flexShrink: 0,
          letterSpacing: 0.2,
        }}
        onClick={(event) => event.stopPropagation()}
        aria-label={`Type: ${label}`}
      >
        {abbreviateColumnType(type)}
      </Badge>
    </Tooltip>
  );
}

export interface ColumnTypeIconProps {
  /** Raw column type from query schema. */
  type: string;
  size?: number;
  color?: string;
  /** When true, hovering shows the formatted type name. Defaults to true. */
  showTooltip?: boolean;
}

/**
 * Renders a compact icon representing a query result column type, with an optional type-name tooltip.
 *
 * @param props icon props
 */
export function ColumnTypeIcon({
  type,
  size = 12,
  color,
  showTooltip = true,
}: ColumnTypeIconProps) {
  const Icon = resolveColumnTypeIcon(type);
  const label = formatColumnTypeLabel(type);
  const icon = (
    <Box
      component="span"
      style={{ display: 'inline-flex', lineHeight: 0, opacity: 0.75 }}
      aria-label={`Type: ${label}`}
      onClick={(event) => event.stopPropagation()}
    >
      <Icon size={size} color={color} aria-hidden />
    </Box>
  );

  if (!showTooltip) {
    return icon;
  }

  return (
    <Tooltip label={label} withArrow withinPortal zIndex={400}>
      {icon}
    </Tooltip>
  );
}
