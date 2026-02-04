import { Box, Text, useMantineColorScheme } from '@mantine/core';
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router';
import { HiOutlineLightBulb } from 'react-icons/hi2';
import { ContextSidebar } from './ContextSidebar';
import { ConceptDetails } from './ConceptDetails';
import { getConceptById, filterConcepts } from '../../data/mockConcepts';
import type { Concept, ConceptFilter } from '../../types/context';

export function ContextLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const params = useParams<{ conceptId?: string }>();

  const [selectedConcept, setSelectedConcept] = useState<Concept | null>(null);
  const [filter, setFilter] = useState<ConceptFilter>({ type: null, value: null });

  // Sync URL params to selected concept
  useEffect(() => {
    if (params.conceptId) {
      const concept = getConceptById(params.conceptId);
      setSelectedConcept(concept || null);
    } else {
      setSelectedConcept(null);
    }
  }, [params.conceptId]);

  const handleSelectConcept = (concept: Concept) => {
    setSelectedConcept(concept);
    navigate(`/context/${concept.id}`);
  };

  const handleFilterChange = (newFilter: ConceptFilter) => {
    setFilter(newFilter);
  };

  const filteredConcepts = filterConcepts(filter.type, filter.value);

  return (
    <Box
      style={{
        display: 'flex',
        height: '100%',
        overflow: 'hidden',
      }}
    >
      {/* Sidebar */}
      <ContextSidebar
        concepts={filteredConcepts}
        selectedId={selectedConcept?.id || null}
        filter={filter}
        onSelectConcept={handleSelectConcept}
        onFilterChange={handleFilterChange}
      />

      {/* Main Content */}
      <Box
        style={{
          flex: 1,
          backgroundColor: isDark ? 'var(--mantine-color-slate-8)' : 'white',
          overflow: 'hidden',
        }}
      >
        {selectedConcept ? (
          <ConceptDetails concept={selectedConcept} />
        ) : (
          <Box
            style={{
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Box
              style={{
                width: 80,
                height: 80,
                borderRadius: '50%',
                backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: 24,
              }}
            >
              <HiOutlineLightBulb
                size={36}
                color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
              />
            </Box>
            <Text size="xl" fw={600} c={isDark ? 'slate.1' : 'slate.8'} mb="xs">
              Context Explorer
            </Text>
            <Text size="sm" c="dimmed" ta="center" maw={400}>
              Browse business concepts by category or tag. Select a concept to view its definition, related entities, and SQL.
            </Text>
          </Box>
        )}
      </Box>
    </Box>
  );
}
