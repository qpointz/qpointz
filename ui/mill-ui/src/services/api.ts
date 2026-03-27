/**
 * Centralized service barrel.
 *
 * Every data access in the application flows through this module.
 * Each service is implemented in its own file and can be independently
 * swapped from mock to real backend by changing the re-export source.
 */

export { schemaService } from './schemaService';
export { conceptService } from './conceptService';
export { queryService } from './queryService';
export { statsService } from './statsService';
export { chatService } from './chatService';
export { chatReferencesService } from './chatReferencesService';
export { relatedContentService } from './relatedContentService';
export { featureService } from './featureService';
export { searchService } from './searchService';
export { facetTypeService } from './facetTypeService';

// Re-export the FeatureFlagService interface for consumers that need it
export type { FeatureFlagService } from './featureService';

export { login, logout, getMe } from './authService';
export type { AuthMeResponse } from './authService';
