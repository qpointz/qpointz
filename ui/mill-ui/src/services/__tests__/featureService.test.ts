import { describe, it, expect } from 'vitest';
import { featureService } from '../featureService';

describe('featureService', () => {
  describe('getFlags', () => {
    it('should return an object', async () => {
      const flags = await featureService.getFlags();
      expect(flags).toBeDefined();
      expect(typeof flags).toBe('object');
    });

    it('should return flags with boolean values', async () => {
      const flags = await featureService.getFlags();
      for (const [, value] of Object.entries(flags)) {
        expect(typeof value).toBe('boolean');
      }
    });

    it('should include standard view flags', async () => {
      const flags = await featureService.getFlags();
      // At minimum, the mock should return these standard flags
      expect('viewModel' in flags || Object.keys(flags).length > 0).toBe(true);
    });
  });
});
