import { describe, expect, it } from 'vitest';
import { clampRightPaneWidth, clampStoredRightPx } from '../horizontalSplitPaneMath';

describe('horizontalSplitPaneMath', () => {
  describe('clampRightPaneWidth', () => {
    it('should clamp drag offset between min right and fraction-based max', () => {
      expect(clampRightPaneWidth(400, 1000, 280, 260, 0.5)).toBe(400);
      expect(clampRightPaneWidth(600, 1000, 280, 260, 0.5)).toBe(500);
      expect(clampRightPaneWidth(100, 1000, 280, 260, 0.5)).toBe(260);
    });

    it('should prefer min right when fraction max is below min right', () => {
      expect(clampRightPaneWidth(400, 500, 280, 260, 0.5)).toBe(260);
    });

    it('should return min right when usable width is zero', () => {
      expect(clampRightPaneWidth(400, 0, 280, 260, 0.5)).toBe(260);
    });
  });

  describe('clampStoredRightPx', () => {
    it('should fall back when stored value is not finite', () => {
      expect(clampStoredRightPx(Number.NaN, 1000, 280, 260, 0.5, 380)).toBe(380);
    });

    it('should clamp persisted width to current layout limits', () => {
      expect(clampStoredRightPx(900, 1000, 280, 260, 0.5, 380)).toBe(500);
    });
  });
});
