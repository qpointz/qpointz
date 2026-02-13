import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { defaultFeatureFlags, type FeatureFlags } from './defaults';
import { featureService } from '../services/api';

const FeatureFlagContext = createContext<FeatureFlags>(defaultFeatureFlags);

export function FeatureFlagProvider({ children }: { children: ReactNode }) {
  const [flags, setFlags] = useState<FeatureFlags>(defaultFeatureFlags);

  useEffect(() => {
    let cancelled = false;

    featureService.getFlags().then((remote) => {
      if (!cancelled) {
        // Merge: backend overrides defaults; unknown keys are ignored
        setFlags((prev) => ({ ...prev, ...remote }));
      }
    }).catch(() => {
      // Silently fall back to defaults
    });

    return () => { cancelled = true; };
  }, []);

  return (
    <FeatureFlagContext.Provider value={flags}>
      {children}
    </FeatureFlagContext.Provider>
  );
}

export function useFeatureFlags(): FeatureFlags {
  return useContext(FeatureFlagContext);
}
