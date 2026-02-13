import type { SchemaService } from '../types/schema';
import { mockSchemaTree, getEntityFacets, findEntityById } from '../data/mockSchema';

const mockSchemaService: SchemaService = {
  async getTree() {
    return mockSchemaTree;
  },
  async getEntityById(id: string) {
    return findEntityById(id);
  },
  async getEntityFacets(id: string) {
    return getEntityFacets(id);
  },
};

// When real backend is ready, create realSchemaService and change the export below
export const schemaService: SchemaService = mockSchemaService;
