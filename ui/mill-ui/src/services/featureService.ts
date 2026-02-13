import type { FeatureFlags } from '../features/defaults';
import { defaultFeatureFlags } from '../features/defaults';

export interface FeatureFlagService {
  getFlags(): Promise<Partial<FeatureFlags>>;
}

const mockFeatureService: FeatureFlagService = {
  async getFlags() {
    // Mock returns defaults; swap with real backend call later:
    // const res = await fetch(`${API_BASE_URL}/api/v1/features`);
    // return res.json();
    return defaultFeatureFlags;
  },
};

// When real backend is ready, create realFeatureService and change the export below
export const featureService: FeatureFlagService = mockFeatureService;
