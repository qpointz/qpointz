export type SearchResultType = 'view' | 'schema' | 'table' | 'attribute' | 'concept' | 'query';

export interface SearchResult {
  /** Unique identifier */
  id: string;
  /** Display name */
  name: string;
  /** Result category — drives the icon and group label */
  type: SearchResultType;
  /** Optional short description or subtitle */
  description?: string;
  /** Optional breadcrumb trail (e.g. "sales > customers") */
  breadcrumb?: string;
  /** Route to navigate to on selection */
  route: string;
}

export interface SearchService {
  /** Search across all object types. Returns an empty array when query is too short. */
  search(query: string): Promise<SearchResult[]>;
}
