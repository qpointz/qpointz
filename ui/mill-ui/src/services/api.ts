/**
 * Centralized service barrel.
 *
 * Every data access in the application flows through this module.
 * Each service is implemented in its own file and can be independently
 * swapped from mock to real backend by changing the re-export source.
 */

export {
  schemaService,
  buildEntityUrn,
  metadataEntityUrnForFacetApi,
  buildEntityFacetsFromResolvedList,
} from './schemaService';
export { conceptService } from './conceptService';
export { queryService } from './queryService';
export { statsService } from './statsService';
export {
  fetchExportFormats,
  downloadTableExport,
  downloadSqlExport,
} from './exportService';
export {
  pickDefaultExportFormatId,
  buildTableExportUrl,
  filenameFromContentDisposition,
} from './exportHelpers';
export type { ExportFormatInfo } from './exportHelpers';

import { chatService, mockChatService, realChatService } from './chatService';

export { chatService, mockChatService, realChatService };

/** Narrow helper for callers that only need profiles discovery (delegates to [chatService]). */
export function listAgentProfiles() {
  return chatService.listAgentProfiles();
}

export { chatReferencesService } from './chatReferencesService';
export { relatedContentService } from './relatedContentService';
export { featureService } from './featureService';
export { searchService } from './searchService';
export { facetTypeService } from './facetTypeService';

// Re-export the FeatureFlagService interface for consumers that need it
export type { FeatureFlagService } from './featureService';

export { login, logout, getMe } from './authService';
export type { AuthMeResponse } from './authService';
