import { describe, expect, it } from 'vitest';
import { isQuerySessionNotFound, isRecoverableQuerySessionError } from '../queryErrors';

describe('queryErrors', () => {
  it('shouldTreatStaleEpochAsRecoverable', () => {
    const error = new Error('Query page failed (409): {"code":"stale_epoch"}');
    expect(isRecoverableQuerySessionError(error)).toBe(true);
    expect(isQuerySessionNotFound(error)).toBe(false);
  });

  it('shouldTreatNotFoundAsRecoverable', () => {
    const error = new Error('Query page failed (404): {"code":"not_found"}');
    expect(isRecoverableQuerySessionError(error)).toBe(true);
    expect(isQuerySessionNotFound(error)).toBe(true);
  });
});
