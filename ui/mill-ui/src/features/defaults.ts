export interface FeatureFlags {
  // Views — top-level routes; disabling hides the route and its nav button
  viewHome: boolean; // Home / Overview dashboard
  viewModel: boolean; // Data Model explorer
  viewKnowledge: boolean; // Knowledge / Concepts explorer
  viewAnalysis: boolean; // Analysis / Query Playground
  viewChat: boolean; // General Chat

  // Chat References — badges/popovers linking context objects to existing conversations
  chatReferencesEnabled: boolean; // Master toggle for all chat reference features
  chatReferencesModelContext: boolean; // Chat references on Data Model entities
  chatReferencesKnowledgeContext: boolean; // Chat references on Knowledge concepts
  chatReferencesAnalysisContext: boolean; // Chat references on Analysis queries
  chatReferencesSidebarIndicator: boolean; // Reference count badges in sidebar tree items

  // Inline Chat — context-aware chat drawer on detail pages
  inlineChatEnabled: boolean; // Master toggle — hides the entire InlineChatDrawer
  inlineChatModelContext: boolean; // Allow inline chat on Data Model entities
  inlineChatModelSchema: boolean; // Allow inline chat on SCHEMA-level entities
  inlineChatModelTable: boolean; // Allow inline chat on TABLE-level entities
  inlineChatModelColumn: boolean; // Allow inline chat on ATTRIBUTE-level entities
  inlineChatKnowledgeContext: boolean; // Allow inline chat on Knowledge concepts
  inlineChatAnalysisContext: boolean; // Allow inline chat on Analysis queries
  inlineChatMultiSession: boolean; // Allow multiple simultaneous inline chat sessions
  inlineChatSessionGrouping: boolean; // Group inline sessions by route context type in drawer
  inlineChatGreeting: boolean; // Show welcome message when a new inline session starts

  // Model View Details — Entity Details panel controls
  modelStructuralFacet: boolean; // Use tailored StructuralFacet read view when data exists
  modelQuickBadges: boolean; // Quick info badges below entity header (PK, FK, nullable, unique)
  modelPhysicalType: boolean; // Physical type badge on attribute detail

  // Knowledge View Details — Concept Details panel sections
  knowledgeDescription: boolean; // Description section
  knowledgeTags: boolean; // Tags badge list
  knowledgeSqlDefinition: boolean; // SQL definition code block
  knowledgeRelatedEntities: boolean; // Related schema entities list with navigation links
  knowledgeMetadata: boolean; // Created / updated timestamps
  knowledgeSourceBadge: boolean; // Source badge (MANUAL / INFERRED / IMPORTED)

  // Analysis View Features — SQL editor toolbar and results
  analysisFormatSql: boolean; // "Format SQL" toolbar button
  analysisCopySql: boolean; // "Copy SQL" toolbar button
  analysisClearSql: boolean; // "Clear" toolbar button
  analysisExecuteQuery: boolean; // "Execute" button and Ctrl+Enter shortcut
  analysisQueryResults: boolean; // Query results table below the editor

  // Sidebar Features
  sidebarCollapsible: boolean; // Allow sidebar collapse / expand toggle
  sidebarKnowledgeCategories: boolean; // Category filter section in Knowledge sidebar
  sidebarKnowledgeTags: boolean; // Tag filter section in Knowledge sidebar
  sidebarAnalysisBadge: boolean; // Query count badge in Analysis sidebar

  // Connect View
  viewConnect: boolean; // Connect route and nav button
  connectServices: boolean; // Services connection guide section
  connectPython: boolean; // Python connection guide section
  connectJava: boolean; // Java connection guide section

  // Admin View
  viewAdmin: boolean; // Admin route and nav button
  adminDataSources: boolean; // Data Sources management section
  adminPolicies: boolean; // Policies management section
  adminServices: boolean; // Services management section
  adminSettings: boolean; // Settings management section
  adminModelNavEnabled: boolean; // Show Model group in Admin sidebar
  adminFacetTypesEnabled: boolean; // Show Facet Types subitem and route in Admin Model group
  facetTypesReadOnly: boolean; // Force read-only facet type UI (disables create / edit / delete)

  // Profile View
  viewProfile: boolean; // Profile route and user dropdown menu item
  profileGeneral: boolean; // General profile section
  profileSettings: boolean; // Settings section
  profileAccess: boolean; // Access / API keys section

  // Login Providers — authentication methods on the login page
  loginGithub: boolean; // "Continue with GitHub" button
  loginGoogle: boolean; // "Continue with Google" button
  loginMicrosoft: boolean; // "Continue with Microsoft" button
  loginAws: boolean; // "Continue with AWS" button
  loginAzure: boolean; // "Continue with Azure AD" button
  loginPassword: boolean; // Email / password form
  loginRegistration: boolean; // Self-service registration link

  // Related Content — pills/popovers linking to related schema entities, concepts, and queries
  relatedContentEnabled: boolean; // Master toggle for all related content features
  relatedContentModelContext: boolean; // Related content on Data Model entities
  relatedContentModelSchema: boolean; // Related content on SCHEMA-level entities
  relatedContentModelTable: boolean; // Related content on TABLE-level entities
  relatedContentModelColumn: boolean; // Related content on ATTRIBUTE-level entities
  relatedContentKnowledgeContext: boolean; // Related content on Knowledge concepts
  relatedContentAnalysisContext: boolean; // Related content on Analysis queries
  relatedContentInDrawer: boolean; // Show related content section inside the inline chat drawer

  // Chat Input Controls — optional buttons in ChatInputBox
  chatAttachButton: boolean; // "+" attach button
  chatDictateButton: boolean; // Microphone / dictate button

  // Header / Chrome
  headerGlobalSearch: boolean; // Search icon + floating search overlay in header nav
  headerThemeSwitcher: boolean; // Color theme and dark/light mode switcher in user menu
  headerUserProfile: boolean; // User avatar and dropdown menu in header
}

/** All features enabled by default */
export const defaultFeatureFlags: FeatureFlags = {
  viewHome: true,
  viewModel: true,
  viewKnowledge: false,
  viewAnalysis: false,
  viewChat: false,

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

  modelStructuralFacet: true,
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

  viewConnect: false,
  connectServices: true,
  connectPython: true,
  connectJava: true,

  viewAdmin: true,
  adminDataSources: true,
  adminPolicies: true,
  adminServices: true,
  adminSettings: true,
  adminModelNavEnabled: true,
  adminFacetTypesEnabled: true,
  facetTypesReadOnly: false,

  viewProfile: true,
  profileGeneral: true,
  profileSettings: true,
  profileAccess: true,

  loginGithub: false,
  loginGoogle: false,
  loginMicrosoft: false,
  loginAws: false,
  loginAzure: false,
  loginPassword: true,

  loginRegistration: true,

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

  headerGlobalSearch: false,
  headerThemeSwitcher: true,
  headerUserProfile: true,
};
