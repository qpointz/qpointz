import { Box, Text, Badge, NavLink, ScrollArea, Divider, useMantineColorScheme } from '@mantine/core';
import { HiOutlineFolder, HiOutlineTag, HiOutlineLightBulb } from 'react-icons/hi2';
import type { Concept, ConceptFilter } from '../../types/context';
import { getCategories, getTags } from '../../data/mockConcepts';

interface ContextSidebarProps {
  concepts: Concept[];
  selectedId: string | null;
  filter: ConceptFilter;
  onSelectConcept: (concept: Concept) => void;
  onFilterChange: (filter: ConceptFilter) => void;
}

export function ContextSidebar({
  concepts,
  selectedId,
  filter,
  onSelectConcept,
  onFilterChange,
}: ContextSidebarProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const categories = getCategories();
  const tags = getTags();

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
    <Box
      style={{
        width: 280,
        borderRight: `1px solid ${isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-3)'}`,
        backgroundColor: isDark ? 'var(--mantine-color-slate-9)' : 'var(--mantine-color-slate-0)',
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
      }}
    >
      <ScrollArea style={{ flex: 1 }}>
        {/* Categories */}
        <Box p="xs">
          <Text size="xs" fw={600} c="dimmed" tt="uppercase" px="xs" mb="xs">
            Categories
          </Text>
          {categories.map((cat) => (
            <NavLink
              key={cat.name}
              label={cat.name}
              leftSection={<HiOutlineFolder size={16} />}
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
            />
          ))}
        </Box>

        <Divider my="xs" color={isDark ? 'slate.7' : 'gray.3'} />

        {/* Tags */}
        <Box p="xs">
          <Text size="xs" fw={600} c="dimmed" tt="uppercase" px="xs" mb="xs">
            Tags
          </Text>
          {tags.slice(0, 10).map((tag) => (
            <NavLink
              key={tag.name}
              label={`#${tag.name}`}
              leftSection={<HiOutlineTag size={16} />}
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
            />
          ))}
        </Box>

        <Divider my="xs" color={isDark ? 'slate.7' : 'gray.3'} />

        {/* Filtered Concepts */}
        <Box p="xs">
          <Text size="xs" fw={600} c="dimmed" tt="uppercase" px="xs" mb="xs">
            {filter.type ? `Filtered Concepts (${concepts.length})` : `All Concepts (${concepts.length})`}
          </Text>
          {concepts.length === 0 ? (
            <Text size="sm" c="dimmed" ta="center" py="md">
              No concepts found
            </Text>
          ) : (
            concepts.map((concept) => (
              <NavLink
                key={concept.id}
                label={concept.name}
                description={concept.category}
                leftSection={<HiOutlineLightBulb size={16} />}
                active={selectedId === concept.id}
                onClick={() => onSelectConcept(concept)}
                variant="light"
                color={isDark ? 'cyan' : 'teal'}
                style={{ borderRadius: 6 }}
              />
            ))
          )}
        </Box>
      </ScrollArea>
    </Box>
  );
}
