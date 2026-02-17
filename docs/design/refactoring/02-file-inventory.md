# Mill Refactoring — Spring Contamination File Inventory

**Status:** Planning
**Date:** February 2026

Every source file in the four target modules classified as **PURE** (no Spring dependency) or **SPRING** (imports `org.springframework.*`). Spring files list the specific annotations/types used.

---

## Summary

| Module | Total | SPRING | PURE | Spring % |
|--------|-------|--------|------|----------|
| core/mill-security-core | 43 | 26 | 17 | 60% |
| core/mill-service-core | 56 | 20 | 36 | 36% |
| core/mill-metadata-core | 24 | 5 | 19 | 21% |
| data/mill-data-backends | 24 | 3 | 21 | 13% |
| **Total** | **147** | **54** | **93** | **37%** |

---

## core/mill-security-core

### SPRING files (26) — move to `core/mill-security-autoconfigure` (iteration 7)

| # | File | Spring annotations / types |
|---|------|---------------------------|
| 1 | `security/configuration/SecurityConfig.java` | `@Configuration`, `@EnableConfigurationProperties`, `@ConfigurationProperties`, `@EnableWebSecurity`, `@Bean`, `@ConditionalOnSecurity` |
| 2 | `security/configuration/WellKnownSecurityConfiguration.java` | `@Configuration`, `@EnableWebSecurity`, `@Bean`, `@Order`, `@ConditionalOnMissingBean` |
| 3 | `security/configuration/AuthRoutesSecurityConfiguration.java` | `@Configuration`, `@EnableWebSecurity`, `@Bean`, `@ConditionalOnSecurity`, `@Order`, `@ConditionalOnMissingBean` |
| 4 | `security/configuration/PolicyActionsConfiguration.java` | `@Configuration`, `@ConfigurationProperties`, `@ConditionalOnSecurity` |
| 5 | `security/configuration/ServicesSecurityConfiguration.java` | `@Configuration`, `@EnableWebSecurity`, `@Bean`, `@ConditionalOnSecurity`, `@ConditionalOnMissingBean` |
| 6 | `security/configuration/AppSecurityConfiguration.java` | `@Configuration`, `@EnableWebSecurity`, `@Bean`, `@Order`, `@ConditionalOnSecurity`, `@ConditionalOnMissingBean` |
| 7 | `security/configuration/SwaggerSecurityConfig.java` | `@Configuration`, `@EnableWebSecurity`, `@Bean`, `@ConditionalOnSecurity`, `@Order`, `@ConditionalOnMissingBean` |
| 8 | `security/configuration/ApiSecurityConfiguration.java` | `@Configuration`, `@EnableWebSecurity`, `@Bean`, `@Order`, `@ConditionalOnSecurity`, `@ConditionalOnMissingBean` |
| 9 | `security/authentication/token/EntraIdAuthenticationConfiguration.java` | `@Configuration`, `@ConditionalOnSecurity`, `@Bean`, `@ConditionalOnProperty` |
| 10 | `security/authentication/token/EntraIdTokenAuthenticationProvider.java` | implements Spring `AuthenticationProvider` |
| 11 | `security/authentication/token/EntraIdAuthenticationMethod.java` | uses `AuthenticationProvider`, `HttpSecurity` |
| 12 | `security/authentication/token/EntraIdProfileLoader.java` | uses `GrantedAuthority` |
| 13 | `security/authentication/token/BearerTokenAuthenticationReader.java` | uses Spring Security types |
| 14 | `security/authentication/oauth2/OAuth2AuthenticationConfiguration.java` | `@EnableConfigurationProperties`, `@ConfigurationProperties`, `@Configuration`, `@ConditionalOnSecurity`, `@Bean`, `@ConditionalOnProperty`, `@Autowired` |
| 15 | `security/authentication/oauth2/OAuth2ResourceServiceAuthenticationMethod.java` | uses `AuthenticationProvider`, `HttpSecurity`, OAuth2 types |
| 16 | `security/authentication/basic/PasswordAuthenticationConfiguration.java` | `@Configuration`, `@ConditionalOnSecurity`, `@Bean`, `@ConditionalOnProperty`, `@Value` |
| 17 | `security/authentication/basic/BasicAuthenticationMethod.java` | uses `AuthenticationProvider`, `HttpSecurity` |
| 18 | `security/authentication/basic/BasicAuthenticationReader.java` | uses Spring Security types |
| 19 | `security/authentication/basic/providers/UserRepoAuthenticationProvider.java` | implements Spring `AuthenticationProvider` |
| 20 | `security/authentication/basic/providers/User.java` | implements `UserDetails` |
| 21 | `security/authentication/AuthenticationMethod.java` | uses `AuthenticationProvider`, `HttpSecurity` |
| 22 | `security/authentication/AuthenticationReader.java` | uses `Authentication` |
| 23 | `security/authentication/CompositeAuthenticationReader.java` | uses `Authentication` |
| 24 | `security/annotations/ConditionalOnSecurity.java` | `@Conditional` |
| 25 | `security/annotations/OnSecurityEnabledCondition.java` | implements `Condition`, uses `ConditionContext` |
| 26 | `utils/SpringUtils.java` | uses `Environment`, `Profiles` |

### PURE files (17) — move to `core/mill-security` (iteration 1)

| # | File |
|---|------|
| 1 | `security/authorization/policy/Action.java` |
| 2 | `security/authorization/policy/ActionVerb.java` |
| 3 | `security/authorization/policy/PolicyAction.java` |
| 4 | `security/authorization/policy/PolicyActionDescriptor.java` |
| 5 | `security/authorization/policy/PolicyEvaluator.java` |
| 6 | `security/authorization/policy/PolicyEvaluatorImpl.java` |
| 7 | `security/authorization/policy/PolicyRepository.java` |
| 8 | `security/authorization/policy/PolicySelector.java` |
| 9 | `security/authorization/policy/actions/ExpressionFilterAction.java` |
| 10 | `security/authorization/policy/actions/TableReadAction.java` |
| 11 | `security/authorization/policy/repositories/InMemoryPolicyRepository.java` |
| 12 | `security/authorization/policy/repositories/PolicyActionDescriptorRepository.java` |
| 13 | `security/authentication/AuthenticationType.java` |
| 14 | `security/authentication/AuthenticationMethodDescriptor.java` |
| 15 | `security/authentication/AuthenticationContext.java` |
| 16 | `security/authentication/AuthenticationMethods.java` |
| 17 | `security/authentication/basic/providers/UserRepo.java` |

---

## core/mill-service-core

### SPRING files (20) — destinations vary by iteration

**To `data/mill-data-autoconfigure` (iteration 6):**

| # | File | Spring annotations / types |
|---|------|---------------------------|
| 1 | `services/configuration/DefaultServiceConfiguration.java` | `@Configuration`, `@Bean`, `@Autowired` |
| 2 | `services/configuration/PolicyConfiguration.java` | `@Component`, `@ConfigurationProperties`, `@EnableConfigurationProperties`, `@ConditionalOnProperty` |
| 3 | `services/configuration/DefaultFilterChainConfiguration.java` | `@Configuration`, `@EnableConfigurationProperties`, `@ConditionalOnSecurity`, `@Bean` |
| 4 | `services/SecurityContextSecurityProvider.java` | uses `SecurityContextHolder`, `GrantedAuthority` |
| 5 | `security/authorization/policy/GrantedAuthoritiesPolicySelector.java` | uses `GrantedAuthority` |
| 6 | `services/metadata/configuration/MetadataConfiguration.java` | `@Configuration`, `@Bean`, `@ConditionalOnMissingBean`, `@Lazy`, `@ConditionalOnProperty`, `@Value` |

**To `core/mill-security-autoconfigure` (iteration 7):**

| # | File | Spring annotations / types |
|---|------|---------------------------|
| 7 | `services/annotations/ConditionalOnService.java` | `@Conditional` |
| 8 | `services/annotations/OnServiceEnabledCondition.java` | implements `Condition` |
| 9 | `services/descriptors/ApplicationDescriptor.java` | `@Component`, `@Bean`, `@ConditionalOnSecurity`, `@Autowired` |

**Refactored in-place (iteration 3) — Spring type replaced with `Collection<String>`:**

| # | File | Spring type to remove |
|---|------|-----------------------|
| 10 | `services/dispatchers/SecurityDispatcher.java` | `GrantedAuthority` |
| 11 | `services/dispatchers/SecurityDispatcherImpl.java` | `GrantedAuthority` |
| 12 | `services/SecurityProvider.java` | `GrantedAuthority` |

**To `metadata/mill-metadata-provider` (iteration 5) — strip annotations first:**

| # | File | Spring annotations / types |
|---|------|---------------------------|
| 13 | `services/metadata/impl/MetadataProviderImpl.java` | `@Service`, `@ConditionalOnMissingBean` |
| 14 | `services/metadata/impl/file/FileRepository.java` | `Resource`, `ResourceLoader` |
| 15 | `services/metadata/impl/file/FileAnnotationsRepository.java` | `@Component`, `@Lazy`, `@ConditionalOnProperty` |
| 16 | `services/metadata/impl/file/FileRelationsProvider.java` | `@Component`, `@Lazy`, `@ConditionalOnProperty`, `@ConditionalOnMissingBean` |
| 17 | `services/metadata/impl/NoneAnnotationsRepository.java` | `@Component`, `@Lazy`, `@ConditionalOnProperty` |
| 18 | `services/metadata/impl/NoneRelationsProvider.java` | `@Component`, `@Lazy`, `@ConditionalOnProperty` |
| 19 | `services/metadata/impl/v2/MetadataV2AnniotationsProvider.java` | `@Component`, `@Lazy`, `@ConditionalOnProperty` |
| 20 | `services/metadata/impl/v2/MetadataV2RelationsProvider.java` | `@Component`, `@Lazy`, `@ConditionalOnProperty` |

### PURE files (36) — destinations vary

**To `data/mill-data-service` (iteration 4):**

| # | File |
|---|------|
| 1 | `services/dispatchers/DataOperationDispatcher.java` |
| 2 | `services/dispatchers/DataOperationDispatcherImpl.java` |
| 3 | `services/dispatchers/PlanDispatcher.java` |
| 4 | `services/dispatchers/PlanDispatcherImpl.java` |
| 5 | `services/dispatchers/PlanHelper.java` |
| 6 | `services/dispatchers/SecurityDispatcher.java` (after iteration 3 purification) |
| 7 | `services/dispatchers/SecurityDispatcherImpl.java` (after iteration 3 purification) |
| 8 | `services/dispatchers/ResultAllocator.java` |
| 9 | `services/dispatchers/ResultAllocatorImpl.java` |
| 10 | `services/dispatchers/SubstraitDispatcher.java` |
| 11 | `services/SecurityProvider.java` (after iteration 3 purification) |
| 12 | `services/PlanRewriter.java` |
| 13 | `services/PlanRewriteChain.java` |
| 14 | `services/PlanRewriteContext.java` |
| 15 | `services/ServiceHandler.java` |
| 16 | `services/ServiceHandlerPlanRewriteContext.java` |
| 17 | `services/SchemaProvider.java` |
| 18 | `services/SqlProvider.java` |
| 19 | `services/ExecutionProvider.java` |
| 20 | `services/rewriters/AttributeFacet.java` |
| 21 | `services/rewriters/RecordFacet.java` |
| 22 | `services/rewriters/TableFacet.java` |
| 23 | `services/rewriters/TableFacetFactory.java` |
| 24 | `services/rewriters/TableFacetFactoryImpl.java` |
| 25 | `services/rewriters/TableFacetFactories.java` |
| 26 | `services/rewriters/TableFacetPlanRewriter.java` |
| 27 | `services/rewriters/TableFacetsCollection.java` |
| 28 | `services/rewriters/TableFacetVisitor.java` |
| 29 | `services/descriptors/SecurityDescriptor.java` |
| 30 | `services/descriptors/ServiceDescriptor.java` |

**To `metadata/mill-metadata-provider` (iteration 5):**

| # | File |
|---|------|
| 31 | `services/metadata/MetadataProvider.java` |
| 32 | `services/metadata/AnnotationsRepository.java` |
| 33 | `services/metadata/RelationsProvider.java` |
| 34 | `services/metadata/model/Model.java` |
| 35 | `services/metadata/model/Schema.java` |
| 36 | `services/metadata/model/Table.java` |
| 37 | `services/metadata/model/Attribute.java` |
| 38 | `services/metadata/model/Relation.java` |
| 39 | `services/metadata/model/ValueMapping.java` |

---

## core/mill-metadata-core

### SPRING files (5)

**To `metadata/mill-metadata-autoconfigure` (iteration 2):**

| # | File | Spring annotations / types |
|---|------|---------------------------|
| 1 | `metadata/configuration/MetadataRepositoryAutoConfiguration.java` | `@AutoConfiguration`, `@EnableConfigurationProperties`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, `@Bean` |
| 2 | `metadata/configuration/MetadataCoreConfiguration.java` | `@Configuration` |
| 3 | `metadata/configuration/MetadataProperties.java` | `@ConfigurationProperties` |

**Refactored in-place (iteration 2) — strip annotations:**

| # | File | Spring annotations / types |
|---|------|---------------------------|
| 4 | `metadata/service/MetadataService.java` | `@Service`, `@ConditionalOnBean`, `@Autowired` |

**Deferred — FileMetadataRepository (stays until later iteration):**

| # | File | Spring types |
|---|------|-------------|
| 5 | `metadata/repository/file/FileMetadataRepository.java` | `Resource`, `ResourceLoader` |

### PURE files (19)

| # | File |
|---|------|
| 1 | `metadata/repository/MetadataRepository.java` |
| 2 | `metadata/domain/MetadataFacet.java` |
| 3 | `metadata/domain/MetadataEntity.java` |
| 4 | `metadata/domain/MetadataType.java` |
| 5 | `metadata/domain/FacetRegistry.java` |
| 6 | `metadata/domain/AbstractFacet.java` |
| 7 | `metadata/domain/ValidationException.java` |
| 8 | `metadata/domain/DataClassification.java` |
| 9 | `metadata/domain/ConceptSource.java` |
| 10 | `metadata/domain/RelationType.java` |
| 11 | `metadata/domain/RelationCardinality.java` |
| 12 | `metadata/domain/core/RelationFacet.java` |
| 13 | `metadata/domain/core/DescriptiveFacet.java` |
| 14 | `metadata/domain/core/ConceptTarget.java` |
| 15 | `metadata/domain/core/EntityReference.java` |
| 16 | `metadata/domain/core/ValueMappingFacet.java` |
| 17 | `metadata/domain/core/TableType.java` |
| 18 | `metadata/domain/core/StructuralFacet.java` |
| 19 | `metadata/domain/core/ConceptFacet.java` |

---

## data/mill-data-backends

### SPRING files (3) — move to `data/mill-data-autoconfigure` (iteration 6)

| # | File | Spring annotations / types |
|---|------|---------------------------|
| 1 | `services/configuration/BackendConfiguration.java` | `@Component`, `@EnableConfigurationProperties`, `@ConfigurationProperties`, `@Configuration`, `@Bean`, `@Value`, `@ConstructorBinding` |
| 2 | `services/jdbc/configuration/JdbcCalciteConfiguration.java` | `@Component`, `@ConditionalOnProperty`, `@EnableConfigurationProperties`, `@ConfigurationProperties`, `@Value`, `@Bean`, `@ConditionalOnMissingBean` |
| 3 | `services/calcite/configuration/CalciteServiceConfiguration.java` | `@Component`, `@ConditionalOnProperty`, `@EnableConfigurationProperties`, `@ConfigurationProperties`, `@Bean` |

### PURE files (21) — stay in `data/mill-data-backends`

| # | File |
|---|------|
| 1 | `services/jdbc/providers/JdbcExecutionProvider.java` |
| 2 | `services/jdbc/providers/JdbcContext.java` |
| 3 | `services/jdbc/providers/JdbcConnectionProvider.java` |
| 4 | `services/jdbc/providers/JdbcContextFactory.java` |
| 5 | `services/jdbc/providers/JdbcPermissiveOperatorTable.java` |
| 6 | `services/jdbc/providers/impl/JdbcContextFactoryImpl.java` |
| 7 | `services/jdbc/providers/impl/JdbcContextImpl.java` |
| 8 | `services/jdbc/providers/impl/JdbcConnectionCustomizerImpl.java` |
| 9 | `services/calcite/providers/CalciteSchemaProvider.java` |
| 10 | `services/calcite/providers/CalcitePlanConverter.java` |
| 11 | `services/calcite/providers/CalciteSqlProvider.java` |
| 12 | `services/calcite/providers/CalciteExecutionProvider.java` |
| 13 | `services/calcite/providers/PlanConverter.java` |
| 14 | `services/calcite/ConnectionContextFactory.java` |
| 15 | `services/calcite/StaticConnectionContextFactory.java` |
| 16 | `services/calcite/RelDataTypeConverter.java` |
| 17 | `services/calcite/RelToDatabaseTypeConverter.java` |
| 18 | `services/calcite/CalciteContext.java` |
| 19 | `services/calcite/CalciteContextFactory.java` |
| 20 | `services/calcite/CalciteConnectionContextBase.java` |
| 21 | *(verify if any additional utility files exist)* |
