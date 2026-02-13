import type { ConceptService, ConceptFilter } from '../types/context';
import { getConceptById, getCategories, getTags, filterConcepts } from '../data/mockConcepts';

const mockConceptService: ConceptService = {
  async getConcepts(filter?: ConceptFilter) {
    return filterConcepts(filter?.type ?? null, filter?.value ?? null);
  },
  async getConceptById(id: string) {
    return getConceptById(id) ?? null;
  },
  async getCategories() {
    return getCategories();
  },
  async getTags() {
    return getTags();
  },
};

// When real backend is ready, create realConceptService and change the export below
export const conceptService: ConceptService = mockConceptService;
