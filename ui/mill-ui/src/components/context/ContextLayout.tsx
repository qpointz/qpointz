import { Box, Text, useMantineColorScheme } from '@mantine/core';
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router';
import { HiOutlineLightBulb, HiOutlineAcademicCap } from 'react-icons/hi2';
import { ContextSidebar } from './ContextSidebar';
import { ConceptDetails } from './ConceptDetails';
import { CollapsibleSidebar } from '../common/CollapsibleSidebar';
import { conceptService } from '../../services/api';
import { useChatReferencesContext } from '../../context/ChatReferencesContext';
import type { Concept, ConceptFilter } from '../../types/context';

export function ContextLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const params = useParams<{ conceptId?: string }>();
  const { prefetchRefs } = useChatReferencesContext();

  const [selectedConcept, setSelectedConcept] = useState<Concept | null>(null);
  const [filter, setFilter] = useState<ConceptFilter>({ type: null, value: null });
  const [concepts, setConcepts] = useState<Concept[]>([]);
  const [categories, setCategories] = useState<{ name: string; count: number }[]>([]);
  const [tags, setTags] = useState<{ name: string; count: number }[]>([]);

  // Load categories, tags, and initial concepts on mount
  useEffect(() => {
    conceptService.getCategories().then(setCategories).catch(() => setCategories([]));
    conceptService.getTags().then(setTags).catch(() => setTags([]));

    // Prefetch chat references for all concepts
    conceptService.getConcepts().then((allConcepts) => {
      const allIds = allConcepts.map((c) => c.id);
      prefetchRefs('knowledge', allIds);
    }).catch(() => {
      // Silently ignore
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps -- load once on mount
  }, []);

  // Load concepts when filter changes
  useEffect(() => {
    conceptService.getConcepts(filter).then(setConcepts).catch(() => setConcepts([]));
  }, [filter]);

  // Sync URL params to selected concept
  useEffect(() => {
    if (params.conceptId) {
      conceptService.getConceptById(params.conceptId).then((concept) => {
        setSelectedConcept(concept);
      }).catch(() => {
        setSelectedConcept(null);
      });
    } else {
      setSelectedConcept(null);
    }
  }, [params.conceptId]);

  const handleSelectConcept = (concept: Concept) => {
    setSelectedConcept(concept);
    navigate(`/knowledge/${concept.id}`);
  };

  const handleFilterChange = (newFilter: ConceptFilter) => {
    setFilter(newFilter);
  };

  return (
    <Box
      style={{
        display: 'flex',
        height: '100%',
        overflow: 'hidden',
      }}
    >
      {/* Sidebar */}
      <CollapsibleSidebar icon={HiOutlineAcademicCap} title="Knowledge">
        <ContextSidebar
          concepts={concepts}
          categories={categories}
          tags={tags}
          selectedId={selectedConcept?.id || null}
          filter={filter}
          onSelectConcept={handleSelectConcept}
          onFilterChange={handleFilterChange}
        />
      </CollapsibleSidebar>

      {/* Main Content */}
      <Box
        style={{
          flex: 1,
          backgroundColor: 'var(--mantine-color-body)',
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
            <Text size="xl" fw={600} c={isDark ? 'gray.1' : 'gray.8'} mb="xs">
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
