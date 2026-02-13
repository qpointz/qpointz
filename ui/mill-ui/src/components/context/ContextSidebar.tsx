import { Box, Text, Badge, NavLink, ScrollArea, Divider, useMantineColorScheme } from '@mantine/core';
import { HiOutlineFolder, HiOutlineTag, HiOutlineLightBulb, HiOutlineChatBubbleLeftRight } from 'react-icons/hi2';
import type { Concept, ConceptFilter } from '../../types/context';
import { useInlineChat } from '../../context/InlineChatContext';
import { useChatReferencesContext } from '../../context/ChatReferencesContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

interface ContextSidebarProps {
  concepts: Concept[];
  categories: { name: string; count: number }[];
  tags: { name: string; count: number }[];
  selectedId: string | null;
  filter: ConceptFilter;
  onSelectConcept: (concept: Concept) => void;
  onFilterChange: (filter: ConceptFilter) => void;
}

export function ContextSidebar({
  concepts,
  categories,
  tags,
  selectedId,
  filter,
  onSelectConcept,
  onFilterChange,
}: ContextSidebarProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const { getSessionByContextId } = useInlineChat();
  const { getRefsForContextId } = useChatReferencesContext();

  const handleCategoryClick = (category: string) => {
    if (filter.type === 'category' && filter.value === category) {
      onFilterChange({ type: null, value: null });
    } else {
      onFilterChange({ type: 'category', value: category });
    }
  };

  const handleTagClick = (tag: string) => {
    if (filter.type === 'tag' && filter.value === tag) {
      onFilterChange({ type: null, value: null });
    } else {
      onFilterChange({ type: 'tag', value: tag });
    }
  };

  return (
    <ScrollArea style={{ flex: 1 }}>
      {/* Categories */}
      {flags.sidebarKnowledgeCategories && (
        <>
          <Box p="xs">
            <Text size="xs" fw={600} c="dimmed" tt="uppercase" px="xs" mb="xs">
              Categories
            </Text>
            {categories.map((cat) => (
              <NavLink
                key={cat.name}
                label={<Text size="sm">{cat.name}</Text>}
                leftSection={<HiOutlineFolder size={14} />}
                rightSection={
                  <Badge size="xs" variant="light" color="gray">
                    {cat.count}
                  </Badge>
                }
                active={filter.type === 'category' && filter.value === cat.name}
                onClick={() => handleCategoryClick(cat.name)}
                variant="light"
                color={isDark ? 'cyan' : 'teal'}
                style={{ borderRadius: 6 }}
                styles={{ root: { padding: '4px 8px' } }}
              />
            ))}
          </Box>

          <Divider my="xs" color={isDark ? 'gray.7' : 'gray.3'} />
        </>
      )}

      {/* Concepts */}
      <Box p="xs">
        <Text size="xs" fw={600} c="dimmed" tt="uppercase" px="xs" mb="xs">
          {filter.type ? `Filtered Concepts (${concepts.length})` : `All Concepts (${concepts.length})`}
        </Text>
        {concepts.length === 0 ? (
          <Text size="sm" c="dimmed" ta="center" py="md">
            No concepts found
          </Text>
        ) : (
          concepts.map((concept) => {
            const hasChat = !!getSessionByContextId(concept.id);
            const chatRefs = flags.chatReferencesEnabled && flags.chatReferencesSidebarIndicator
              ? getRefsForContextId(concept.id)
              : [];
            const hasRelatedChats = chatRefs.length > 0;
            return (
              <NavLink
                key={concept.id}
                label={
                  <Box style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Text size="sm" lineClamp={1}>{concept.name}</Text>
                    {hasChat && (
                      <HiOutlineChatBubbleLeftRight
                        size={10}
                        color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
                      />
                    )}
                    {hasRelatedChats && (
                      <Badge
                        size="xs"
                        variant="light"
                        color="violet"
                        circle
                        style={{
                          minWidth: 14,
                          height: 14,
                          padding: 0,
                          fontSize: '9px',
                        }}
                      >
                        {chatRefs.length}
                      </Badge>
                    )}
                  </Box>
                }
                description={<Text size="xs" c="dimmed" style={{ fontSize: 10 }}>{concept.category}</Text>}
                leftSection={<HiOutlineLightBulb size={14} />}
                active={selectedId === concept.id}
                onClick={() => onSelectConcept(concept)}
                variant="light"
                color={isDark ? 'cyan' : 'teal'}
                style={{ borderRadius: 6 }}
                styles={{ root: { padding: '4px 8px' } }}
              />
            );
          })
        )}
      </Box>

      {/* Tags */}
      {flags.sidebarKnowledgeTags && (
        <>
          <Divider my="xs" color={isDark ? 'gray.7' : 'gray.3'} />

          <Box p="xs">
            <Text size="xs" fw={600} c="dimmed" tt="uppercase" px="xs" mb="xs">
              Tags
            </Text>
            {tags.slice(0, 10).map((tag) => (
              <NavLink
                key={tag.name}
                label={<Text size="sm">{`#${tag.name}`}</Text>}
                leftSection={<HiOutlineTag size={14} />}
                rightSection={
                  <Badge size="xs" variant="light" color="gray">
                    {tag.count}
                  </Badge>
                }
                active={filter.type === 'tag' && filter.value === tag.name}
                onClick={() => handleTagClick(tag.name)}
                variant="light"
                color={isDark ? 'cyan' : 'teal'}
                style={{ borderRadius: 6 }}
                styles={{ root: { padding: '4px 8px' } }}
              />
            ))}
          </Box>
        </>
      )}
    </ScrollArea>
  );
}
