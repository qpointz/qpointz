export interface Concept {
  id: string;
  name: string;
  category: string;
  tags: string[];
  description: string;
  sql?: string;
  relatedEntities?: string[];
  source?: 'MANUAL' | 'INFERRED' | 'IMPORTED';
  createdAt?: number;
  updatedAt?: number;
}

export interface ConceptFilter {
  type: 'category' | 'tag' | null;
  value: string | null;
}

export interface ConceptService {
  getConcepts(filter?: ConceptFilter): Promise<Concept[]>;
  getConceptById(id: string): Promise<Concept | null>;
  getCategories(): Promise<{ name: string; count: number }[]>;
  getTags(): Promise<{ name: string; count: number }[]>;
}
