import { useState, useRef, useCallback, useEffect } from 'react';
import {
  ActionIcon,
  TextInput,
  Box,
  Text,
  Group,
  Badge,
  ScrollArea,
  UnstyledButton,
  Button,
  Overlay,
  useMantineColorScheme,
} from '@mantine/core';
import { useDebouncedValue, useHotkeys } from '@mantine/hooks';
import { useNavigate } from 'react-router';
import {
  HiOutlineMagnifyingGlass,
  HiOutlineXMark,
  HiOutlineSquares2X2,
  HiOutlineCube,
  HiOutlineTableCells,
  HiOutlineTag,
  HiOutlineLightBulb,
  HiOutlineBeaker,
  HiOutlineChatBubbleLeftRight,
} from 'react-icons/hi2';
import { searchService } from '../../services/api';
import type { SearchResult, SearchResultType } from '../../types/search';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

/** Left sidebar width — must match CollapsibleSidebar.tsx */
const LEFT_SIDEBAR = 280;
/** Header height */
const HEADER_HEIGHT = 56;
const MAX_DROPDOWN_HEIGHT = 420;
const MIN_QUERY_LENGTH = 2;

// ---------------------------------------------------------------------------
// Type → display helpers
// ---------------------------------------------------------------------------

const TYPE_ICON: Record<SearchResultType, React.ElementType> = {
  view: HiOutlineSquares2X2,
  schema: HiOutlineCube,
  table: HiOutlineTableCells,
  attribute: HiOutlineTag,
  concept: HiOutlineLightBulb,
  query: HiOutlineBeaker,
};

const TYPE_LABEL: Record<SearchResultType, string> = {
  view: 'Views',
  schema: 'Model',
  table: 'Model',
  attribute: 'Model',
  concept: 'Knowledge',
  query: 'Analysis',
};

const TYPE_BADGE: Record<SearchResultType, string> = {
  view: 'View',
  schema: 'Schema',
  table: 'Table',
  attribute: 'Column',
  concept: 'Concept',
  query: 'Query',
};

/** Group results in display order */
const GROUP_ORDER: SearchResultType[] = ['view', 'schema', 'table', 'attribute', 'concept', 'query'];

function groupResults(results: SearchResult[]): { label: string; items: SearchResult[] }[] {
  const groups: Map<string, SearchResult[]> = new Map();

  for (const r of results) {
    const label = TYPE_LABEL[r.type];
    if (!groups.has(label)) groups.set(label, []);
    groups.get(label)!.push(r);
  }

  const labelOrder = GROUP_ORDER.map((t) => TYPE_LABEL[t]);
  const seen = new Set<string>();
  const orderedLabels: string[] = [];
  for (const l of labelOrder) {
    if (!seen.has(l) && groups.has(l)) {
      orderedLabels.push(l);
      seen.add(l);
    }
  }

  return orderedLabels.map((label) => ({ label, items: groups.get(label)! }));
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export function GlobalSearch() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const inputRef = useRef<HTMLInputElement>(null);
  const panelRef = useRef<HTMLDivElement>(null);
  const anchorRef = useRef<HTMLButtonElement>(null);

  const [expanded, setExpanded] = useState(false);
  const [query, setQuery] = useState('');
  const [debouncedQuery] = useDebouncedValue(query, 150);
  const [results, setResults] = useState<SearchResult[]>([]);
  const [highlightIdx, setHighlightIdx] = useState(-1);
  const [loading, setLoading] = useState(false);

  // ---- Search ----
  useEffect(() => {
    if (debouncedQuery.trim().length < MIN_QUERY_LENGTH) {
      setResults([]);
      setHighlightIdx(-1);
      return;
    }

    let cancelled = false;
    setLoading(true);

    searchService.search(debouncedQuery).then((r) => {
      if (!cancelled) {
        setResults(r);
        setHighlightIdx(-1);
        setLoading(false);
      }
    });

    return () => { cancelled = true; };
  }, [debouncedQuery]);

  // ---- Expand / collapse ----
  const expand = useCallback(() => {
    setExpanded(true);
    requestAnimationFrame(() => {
      requestAnimationFrame(() => inputRef.current?.focus());
    });
  }, []);

  const collapse = useCallback(() => {
    setExpanded(false);
    setQuery('');
    setResults([]);
    setHighlightIdx(-1);
  }, []);

  // ---- Hotkey: Cmd+K / Ctrl+K ----
  useHotkeys([['mod+K', () => {
    if (expanded) {
      inputRef.current?.focus();
    } else {
      expand();
    }
  }]]);

  // ---- Close on outside click (for the floating panel) ----
  useEffect(() => {
    if (!expanded) return;

    function handleClick(e: MouseEvent) {
      if (panelRef.current && !panelRef.current.contains(e.target as Node)) {
        collapse();
      }
    }

    // Delay listener to avoid catching the triggering click
    const timer = setTimeout(() => {
      document.addEventListener('mousedown', handleClick);
    }, 0);

    return () => {
      clearTimeout(timer);
      document.removeEventListener('mousedown', handleClick);
    };
  }, [expanded, collapse]);

  // ---- Navigate to a result ----
  const selectResult = useCallback(
    (result: SearchResult) => {
      navigate(result.route);
      collapse();
    },
    [navigate, collapse],
  );

  // ---- Navigate to chat with query pre-filled ----
  const askInChat = useCallback(() => {
    navigate('/chat', { state: { searchQuery: query.trim() } });
    collapse();
  }, [navigate, query, collapse]);

  // ---- Keyboard navigation ----
  const flatResults = results;
  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === 'Escape') {
        collapse();
        return;
      }

      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setHighlightIdx((prev) => (prev < flatResults.length - 1 ? prev + 1 : 0));
        return;
      }

      if (e.key === 'ArrowUp') {
        e.preventDefault();
        setHighlightIdx((prev) => (prev > 0 ? prev - 1 : flatResults.length - 1));
        return;
      }

      if (e.key === 'Enter' && highlightIdx >= 0 && highlightIdx < flatResults.length) {
        e.preventDefault();
        const target = flatResults[highlightIdx];
        if (target) selectResult(target);
      }
    },
    [collapse, flatResults, highlightIdx, selectResult],
  );

  // ---- Derived state ----
  const showDropdown = expanded && query.trim().length >= MIN_QUERY_LENGTH;
  const noResults = showDropdown && results.length === 0 && !loading;
  const grouped = groupResults(results);

  // ---- Colors ----
  const accentColor = isDark ? 'cyan' : 'teal';
  const highlightBg = isDark ? 'var(--mantine-color-dark-4)' : 'var(--mantine-color-gray-2)';
  const borderColor = 'var(--mantine-color-default-border)';

  return (
    <>
      {/* Collapsed: search icon in nav bar */}
      <ActionIcon
        ref={anchorRef}
        variant="subtle"
        color={isDark ? 'gray.4' : 'gray.6'}
        size="lg"
        onClick={expand}
        aria-label="Search (Ctrl+K)"
        style={{ visibility: expanded ? 'hidden' : 'visible' }}
      >
        <HiOutlineMagnifyingGlass size={18} />
      </ActionIcon>

      {/* Expanded: floating overlay + search panel */}
      {expanded && (
        <>
          {/* Dim backdrop */}
          <Overlay
            fixed
            opacity={isDark ? 0.55 : 0.25}
            color={isDark ? '#000' : '#000'}
            zIndex={999}
            onClick={collapse}
          />

          {/* Floating search panel — centered within the content area (between left sidebar and right edge) */}
          <Box
            ref={panelRef}
            style={{
              position: 'fixed',
              top: HEADER_HEIGHT + 12,
              left: LEFT_SIDEBAR,
              right: 0,
              display: 'flex',
              justifyContent: 'center',
              pointerEvents: 'none',
              zIndex: 1000,
            }}
          >
          <Box
            style={{
              width: '70%',
              maxWidth: 680,
              minWidth: 360,
              pointerEvents: 'auto',
            }}
          >
            {/* Search input */}
            <TextInput
              ref={inputRef}
              placeholder="Search views, models, concepts, queries…"
              value={query}
              onChange={(e) => setQuery(e.currentTarget.value)}
              onKeyDown={handleKeyDown}
              leftSection={<HiOutlineMagnifyingGlass size={18} />}
              rightSection={
                query ? (
                  <ActionIcon
                    variant="subtle"
                    color={isDark ? 'gray.4' : 'gray.6'}
                    size="sm"
                    onClick={() => { setQuery(''); inputRef.current?.focus(); }}
                  >
                    <HiOutlineXMark size={16} />
                  </ActionIcon>
                ) : (
                  <Text size="xs" c="dimmed" pr={4}>Esc</Text>
                )
              }
              size="md"
              styles={{
                input: {
                  backgroundColor: 'var(--mantine-color-body)',
                  borderColor,
                  fontSize: 15,
                  height: 44,
                  borderRadius: showDropdown
                    ? 'var(--mantine-radius-md) var(--mantine-radius-md) 0 0'
                    : 'var(--mantine-radius-md)',
                },
              }}
            />

            {/* Results dropdown — directly below input, no gap */}
            {showDropdown && (
              <Box
                style={{
                  backgroundColor: 'var(--mantine-color-body)',
                  borderLeft: `1px solid ${borderColor}`,
                  borderRight: `1px solid ${borderColor}`,
                  borderBottom: `1px solid ${borderColor}`,
                  borderRadius: '0 0 var(--mantine-radius-md) var(--mantine-radius-md)',
                  boxShadow: isDark
                    ? '0 12px 32px rgba(0,0,0,0.6)'
                    : '0 12px 32px rgba(0,0,0,0.12)',
                  overflow: 'hidden',
                }}
              >
                {noResults ? (
                  <Box px="md" py="lg" style={{ textAlign: 'center' }}>
                    <Text size="sm" c="dimmed" mb="sm">
                      No results for &ldquo;{query.trim()}&rdquo;
                    </Text>
                    <Button
                      variant="light"
                      color={accentColor}
                      size="sm"
                      leftSection={<HiOutlineChatBubbleLeftRight size={16} />}
                      onClick={askInChat}
                    >
                      Ask in Chat
                    </Button>
                  </Box>
                ) : (
                  <ScrollArea.Autosize mah={MAX_DROPDOWN_HEIGHT}>
                    <Box py={4}>
                      {grouped.map((group) => (
                        <Box key={group.label}>
                          {/* Group header */}
                          <Text
                            size="xs"
                            fw={600}
                            c="dimmed"
                            px="sm"
                            pt={8}
                            pb={4}
                            tt="uppercase"
                            style={{ letterSpacing: '0.05em' }}
                          >
                            {group.label}
                          </Text>
                          {/* Items */}
                          {group.items.map((result) => {
                            const globalIdx = flatResults.indexOf(result);
                            const isHighlighted = globalIdx === highlightIdx;
                            const Icon = TYPE_ICON[result.type];
                            return (
                              <UnstyledButton
                                key={result.id}
                                w="100%"
                                px="sm"
                                py={6}
                                style={{
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: 10,
                                  backgroundColor: isHighlighted ? highlightBg : undefined,
                                  borderRadius: 0,
                                  cursor: 'pointer',
                                }}
                                onMouseEnter={() => setHighlightIdx(globalIdx)}
                                onClick={() => selectResult(result)}
                              >
                                <Icon
                                  size={16}
                                  style={{
                                    flexShrink: 0,
                                    color: isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-6)',
                                  }}
                                />
                                <Box style={{ flex: 1, minWidth: 0 }}>
                                  <Group gap={6} wrap="nowrap">
                                    <Text size="sm" fw={500} lineClamp={1}>
                                      {result.name}
                                    </Text>
                                    {result.breadcrumb && (
                                      <Text size="xs" c="dimmed" lineClamp={1}>
                                        {result.breadcrumb}
                                      </Text>
                                    )}
                                  </Group>
                                  {result.description && (
                                    <Text size="xs" c="dimmed" lineClamp={1}>
                                      {result.description}
                                    </Text>
                                  )}
                                </Box>
                                <Badge
                                  size="xs"
                                  variant="light"
                                  color="gray"
                                  style={{ flexShrink: 0 }}
                                >
                                  {TYPE_BADGE[result.type]}
                                </Badge>
                              </UnstyledButton>
                            );
                          })}
                        </Box>
                      ))}
                    </Box>
                  </ScrollArea.Autosize>
                )}
              </Box>
            )}
          </Box>
          </Box>
        </>
      )}
    </>
  );
}
