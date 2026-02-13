import { useState } from 'react';
import { Box, Text, UnstyledButton } from '@mantine/core';
import {
  HiOutlineCircleStack,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiChevronRight,
  HiChevronDown,
  HiOutlineArrowTopRightOnSquare,
} from 'react-icons/hi2';
import type { RelatedContentRef } from '../../types/relatedContent';

/* ── Tree data structures ───────────────────────────────────────── */

interface ModelTreeNode {
  /** This segment's name (e.g. "customers") */
  segment: string;
  /** Full dot-separated ID up to (and including) this node */
  fullId: string;
  /** The original ref if this node is one of the actual related refs */
  ref?: RelatedContentRef;
  /** Depth in the tree (0 = schema, 1 = table, 2+ = column) */
  depth: number;
  /** Children keyed by next segment */
  children: ModelTreeNode[];
}

/** Build a tree from a flat list of model refs */
export function buildModelTree(refs: RelatedContentRef[]): ModelTreeNode[] {
  const roots: Map<string, ModelTreeNode> = new Map();

  for (const ref of refs) {
    const segments = ref.id.split('.');
    let currentMap = roots;
    let parentNode: ModelTreeNode | null = null;

    for (let i = 0; i < segments.length; i++) {
      const seg = segments[i]!;
      const fullId = segments.slice(0, i + 1).join('.');

      let node = parentNode
        ? parentNode.children.find((c) => c.segment === seg)
        : currentMap.get(seg);

      if (!node) {
        node = {
          segment: seg,
          fullId,
          depth: i,
          children: [],
        };
        if (parentNode) {
          parentNode.children.push(node);
        } else {
          currentMap.set(seg, node);
        }
      }

      // If this is the last segment, attach the ref
      if (i === segments.length - 1) {
        node.ref = ref;
      }

      parentNode = node;
      // currentMap is only used for root level; after that we use parentNode.children
    }
  }

  return Array.from(roots.values());
}

/* ── Icons ──────────────────────────────────────────────────────── */

function NodeIcon({ depth, size, color }: { depth: number; size: number; color: string }) {
  if (depth === 0) return <HiOutlineCircleStack size={size} color={color} style={{ flexShrink: 0 }} />;
  if (depth === 1) return <HiOutlineTableCells size={size} color={color} style={{ flexShrink: 0 }} />;
  return <HiOutlineViewColumns size={size} color={color} style={{ flexShrink: 0 }} />;
}

/* ── Hierarchy rules ─────────────────────────────────────────────
 *  Schema (depth 0) : no indent, always expanded, no expand icon
 *  Table  (depth 1) : single indent, expanded if ≤3 columns,
 *                      collapsed if >3, expand icon only when >3
 *  Column (depth 2+): double indent, leaf (no children)
 * ────────────────────────────────────────────────────────────── */

const COLUMN_COLLAPSE_THRESHOLD = 3;

/* ── Single tree node ───────────────────────────────────────────── */

interface TreeNodeRowProps {
  node: ModelTreeNode;
  isDark: boolean;
  iconColor: string;
  onNavigate: (fullId: string) => void;
  /** Compact mode for the drawer (smaller font / padding) */
  compact?: boolean;
}

function TreeNodeRow({ node, isDark, iconColor, onNavigate, compact }: TreeNodeRowProps) {
  const hasChildren = node.children.length > 0;
  const isSchema = node.depth === 0;
  const isTable = node.depth === 1;
  const isColumn = node.depth >= 2;

  // Schema: always expanded, Table: expanded when ≤ threshold, Column: leaf
  const defaultExpanded = isSchema
    ? true
    : isTable
      ? node.children.length <= COLUMN_COLLAPSE_THRESHOLD
      : false;

  const [expanded, setExpanded] = useState(defaultExpanded);

  // Table nodes show a chevron only when they have >3 column children
  const showChevron = isTable && hasChildren && node.children.length > COLUMN_COLLAPSE_THRESHOLD;

  const fontSize = compact ? '11px' : '12px';
  const py = compact ? 2 : 3;
  // Base left padding matches the flat list items (px="xs" = 10px, compact px=4)
  // Plus a minimal per-level step to visually distinguish hierarchy
  const basePx = compact ? 4 : 10;
  const step = compact ? 5 : 6;
  const indent = basePx + node.depth * step;
  const isRef = !!node.ref;
  
  const handleRowClick = () => {
    if (showChevron) {
      setExpanded((e) => !e);
    } else if (isRef) {
      onNavigate(node.fullId);
    }
  };

  return (
    <>
      <Box
        py={py}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 6,
          paddingLeft: indent,
          paddingRight: 4,
          borderRadius: 4,
          cursor: isRef || showChevron ? 'pointer' : 'default',
          transition: 'background-color 150ms ease',
        }}
        onClick={handleRowClick}
        onMouseEnter={(e) => {
          if (isRef || showChevron) {
            e.currentTarget.style.backgroundColor = isDark
              ? 'var(--mantine-color-dark-6)'
              : 'var(--mantine-color-gray-1)';
          }
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = 'transparent';
        }}
      >
        {/* Chevron slot — table/column rows get a spacer for alignment; schema has none */}
        {showChevron ? (
          <UnstyledButton
            onClick={(e) => { e.stopPropagation(); setExpanded((ex) => !ex); }}
            style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: compact ? 10 : 12, padding: 0, flexShrink: 0 }}
          >
            {expanded
              ? <HiChevronDown size={compact ? 10 : 12} color={isDark ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-5)'} />
              : <HiChevronRight size={compact ? 10 : 12} color={isDark ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-5)'} />
            }
          </UnstyledButton>
        ) : !isSchema ? (
          <Box style={{ width: compact ? 10 : 12, flexShrink: 0 }} />
        ) : null}

        <NodeIcon depth={node.depth} size={compact ? 12 : 14} color={iconColor} />

        <Text
          size="xs"
          fw={isRef ? 500 : 400}
          c={isRef ? (isDark ? 'gray.1' : 'gray.8') : (isDark ? 'gray.3' : 'gray.5')}
          truncate
          style={{ flex: 1, fontSize }}
        >
          {node.segment}
        </Text>

        {/* Navigate arrow for actual refs */}
        {isRef && (
          <UnstyledButton
            onClick={(e) => { e.stopPropagation(); onNavigate(node.fullId); }}
            style={{ display: 'flex', alignItems: 'center', padding: 0, flexShrink: 0 }}
          >
            <HiOutlineArrowTopRightOnSquare
              size={compact ? 10 : 12}
              color={isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-4)'}
            />
          </UnstyledButton>
        )}

        {/* Collapsed child count hint (tables only) */}
        {showChevron && !expanded && (
          <Text size="xs" c="dimmed" style={{ fontSize: '10px', flexShrink: 0 }}>
            +{node.children.length}
          </Text>
        )}
      </Box>

      {/* Children — schemas always show, tables respect expanded state */}
      {hasChildren && (isSchema || expanded) && node.children.map((child) => (
        <TreeNodeRow
          key={child.fullId}
          node={child}
          isDark={isDark}
          iconColor={iconColor}
          onNavigate={onNavigate}
          compact={compact}
        />
      ))}
    </>
  );
}

/* ── Public component ───────────────────────────────────────────── */

interface RelatedModelTreeProps {
  /** Model-type refs to render as a tree */
  refs: RelatedContentRef[];
  isDark: boolean;
  /** Indigo shades: pass the resolved CSS color string */
  iconColor: string;
  /** Called when user clicks a navigable node. Receives the full dot-separated ID. */
  onNavigate: (fullId: string) => void;
  /** Compact mode for smaller spaces (drawer header popover) */
  compact?: boolean;
}

export function RelatedModelTree({ refs, isDark, iconColor, onNavigate, compact }: RelatedModelTreeProps) {
  const roots = buildModelTree(refs);

  return (
    <Box>
      {roots.map((root) => (
        <TreeNodeRow
          key={root.fullId}
          node={root}
          isDark={isDark}
          iconColor={iconColor}
          onNavigate={onNavigate}
          compact={compact}
        />
      ))}
    </Box>
  );
}
