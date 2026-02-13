import { describe, it, expect } from 'vitest';
import { defaultFeatureFlags, type FeatureFlags } from '../defaults';

describe('defaultFeatureFlags', () => {
  it('should export all flags as booleans', () => {
    for (const [key, value] of Object.entries(defaultFeatureFlags)) {
      expect(typeof value).toBe('boolean');
      // Verify the key exists in the interface (TypeScript already guarantees this,
      // but we double-check the runtime object)
      expect(key).toBeTruthy();
    }
  });

  it('should have all flags default to true', () => {
    for (const [key, value] of Object.entries(defaultFeatureFlags)) {
      expect(value).toBe(true);
      // unused var suppression
      void key;
    }
  });

  it('should include view flags', () => {
    const viewFlags: (keyof FeatureFlags)[] = [
      'viewHome',
      'viewModel',
      'viewKnowledge',
      'viewAnalysis',
      'viewChat',
      'viewAdmin',
      'viewProfile',
    ];
    for (const flag of viewFlags) {
      expect(flag in defaultFeatureFlags).toBe(true);
    }
  });

  it('should include inline chat flags', () => {
    const chatFlags: (keyof FeatureFlags)[] = [
      'inlineChatEnabled',
      'inlineChatModelContext',
      'inlineChatModelSchema',
      'inlineChatModelTable',
      'inlineChatModelColumn',
      'inlineChatKnowledgeContext',
      'inlineChatAnalysisContext',
      'inlineChatMultiSession',
      'inlineChatSessionGrouping',
      'inlineChatGreeting',
    ];
    for (const flag of chatFlags) {
      expect(flag in defaultFeatureFlags).toBe(true);
    }
  });

  it('should include login provider flags', () => {
    const loginFlags: (keyof FeatureFlags)[] = [
      'loginGithub',
      'loginGoogle',
      'loginMicrosoft',
      'loginAws',
      'loginAzure',
      'loginPassword',
    ];
    for (const flag of loginFlags) {
      expect(flag in defaultFeatureFlags).toBe(true);
    }
  });

  it('should include admin flags', () => {
    const adminFlags: (keyof FeatureFlags)[] = [
      'adminDataSources',
      'adminPolicies',
      'adminServices',
      'adminSettings',
    ];
    for (const flag of adminFlags) {
      expect(flag in defaultFeatureFlags).toBe(true);
    }
  });

  it('should include profile flags', () => {
    const profileFlags: (keyof FeatureFlags)[] = [
      'profileGeneral',
      'profileSettings',
      'profileAccess',
    ];
    for (const flag of profileFlags) {
      expect(flag in defaultFeatureFlags).toBe(true);
    }
  });

  it('should include related content flags', () => {
    const relatedContentFlags: (keyof FeatureFlags)[] = [
      'relatedContentEnabled',
      'relatedContentModelContext',
      'relatedContentModelSchema',
      'relatedContentModelTable',
      'relatedContentModelColumn',
      'relatedContentKnowledgeContext',
      'relatedContentAnalysisContext',
      'relatedContentInDrawer',
    ];
    for (const flag of relatedContentFlags) {
      expect(flag in defaultFeatureFlags).toBe(true);
    }
  });

  it('should be a plain object (not a class instance)', () => {
    expect(Object.getPrototypeOf(defaultFeatureFlags)).toBe(Object.prototype);
  });
});
