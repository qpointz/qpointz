export interface FeatureFlags {
  // Views
  viewHome: boolean;
  viewModel: boolean;
  viewKnowledge: boolean;
  viewAnalysis: boolean;
  viewChat: boolean;

  // Chat References (related general conversations)
  chatReferencesEnabled: boolean;
  chatReferencesModelContext: boolean;
  chatReferencesKnowledgeContext: boolean;
  chatReferencesAnalysisContext: boolean;
  chatReferencesSidebarIndicator: boolean;

  // Inline Chat
  inlineChatEnabled: boolean;
  inlineChatModelContext: boolean;
  inlineChatModelSchema: boolean;
  inlineChatModelTable: boolean;
  inlineChatModelColumn: boolean;
  inlineChatKnowledgeContext: boolean;
  inlineChatAnalysisContext: boolean;
  inlineChatMultiSession: boolean;
  inlineChatSessionGrouping: boolean;
  inlineChatGreeting: boolean;

  // Model View Details
  modelDescriptiveFacet: boolean;
  modelStructuralFacet: boolean;
  modelRelationsFacet: boolean;
  modelQuickBadges: boolean;
  modelPhysicalType: boolean;

  // Knowledge View Details
  knowledgeDescription: boolean;
  knowledgeTags: boolean;
  knowledgeSqlDefinition: boolean;
  knowledgeRelatedEntities: boolean;
  knowledgeMetadata: boolean;
  knowledgeSourceBadge: boolean;

  // Analysis View Features
  analysisFormatSql: boolean;
  analysisCopySql: boolean;
  analysisClearSql: boolean;
  analysisExecuteQuery: boolean;
  analysisQueryResults: boolean;

  // Sidebar Features
  sidebarCollapsible: boolean;
  sidebarKnowledgeCategories: boolean;
  sidebarKnowledgeTags: boolean;
  sidebarAnalysisBadge: boolean;

  // Connect View
  viewConnect: boolean;
  connectServices: boolean;
  connectPython: boolean;
  connectJava: boolean;

  // Admin View
  viewAdmin: boolean;
  adminDataSources: boolean;
  adminPolicies: boolean;
  adminServices: boolean;
  adminSettings: boolean;

  // Profile View
  viewProfile: boolean;
  profileGeneral: boolean;
  profileSettings: boolean;
  profileAccess: boolean;

  // Login Providers
  loginGithub: boolean;
  loginGoogle: boolean;
  loginMicrosoft: boolean;
  loginAws: boolean;
  loginAzure: boolean;
  loginPassword: boolean;

  // Related Content (cross-object relationships)
  relatedContentEnabled: boolean;
  relatedContentModelContext: boolean;
  relatedContentModelSchema: boolean;
  relatedContentModelTable: boolean;
  relatedContentModelColumn: boolean;
  relatedContentKnowledgeContext: boolean;
  relatedContentAnalysisContext: boolean;
  relatedContentInDrawer: boolean;

  // Chat Input Controls
  chatAttachButton: boolean;
  chatDictateButton: boolean;

  // Header / Chrome
  headerGlobalSearch: boolean;
  headerThemeSwitcher: boolean;
  headerUserProfile: boolean;
}

/** All features enabled by default */
export const defaultFeatureFlags: FeatureFlags = {
  viewHome: true,
  viewModel: true,
  viewKnowledge: true,
  viewAnalysis: true,
  viewChat: true,

  chatReferencesEnabled: true,
  chatReferencesModelContext: true,
  chatReferencesKnowledgeContext: true,
  chatReferencesAnalysisContext: true,
  chatReferencesSidebarIndicator: true,

  inlineChatEnabled: true,
  inlineChatModelContext: true,
  inlineChatModelSchema: true,
  inlineChatModelTable: true,
  inlineChatModelColumn: true,
  inlineChatKnowledgeContext: true,
  inlineChatAnalysisContext: true,
  inlineChatMultiSession: true,
  inlineChatSessionGrouping: true,
  inlineChatGreeting: true,

  modelDescriptiveFacet: true,
  modelStructuralFacet: true,
  modelRelationsFacet: true,
  modelQuickBadges: true,
  modelPhysicalType: true,

  knowledgeDescription: true,
  knowledgeTags: true,
  knowledgeSqlDefinition: true,
  knowledgeRelatedEntities: true,
  knowledgeMetadata: true,
  knowledgeSourceBadge: true,

  analysisFormatSql: true,
  analysisCopySql: true,
  analysisClearSql: true,
  analysisExecuteQuery: true,
  analysisQueryResults: true,

  sidebarCollapsible: true,
  sidebarKnowledgeCategories: true,
  sidebarKnowledgeTags: true,
  sidebarAnalysisBadge: true,

  viewConnect: true,
  connectServices: true,
  connectPython: true,
  connectJava: true,

  viewAdmin: true,
  adminDataSources: true,
  adminPolicies: true,
  adminServices: true,
  adminSettings: true,

  viewProfile: true,
  profileGeneral: true,
  profileSettings: true,
  profileAccess: true,

  loginGithub: true,
  loginGoogle: true,
  loginMicrosoft: true,
  loginAws: true,
  loginAzure: true,
  loginPassword: true,

  relatedContentEnabled: true,
  relatedContentModelContext: true,
  relatedContentModelSchema: true,
  relatedContentModelTable: true,
  relatedContentModelColumn: true,
  relatedContentKnowledgeContext: true,
  relatedContentAnalysisContext: true,
  relatedContentInDrawer: true,

  chatAttachButton: true,
  chatDictateButton: true,

  headerGlobalSearch: true,
  headerThemeSwitcher: true,
  headerUserProfile: true,
};
