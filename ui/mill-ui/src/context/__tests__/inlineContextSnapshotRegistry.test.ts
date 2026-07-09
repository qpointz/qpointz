import { afterEach, describe, expect, it } from 'vitest';
import {
  collectInlineContextSnapshot,
  registerInlineContextSnapshotProvider,
  unregisterInlineContextSnapshotProvider,
} from '../inlineContextSnapshotRegistry';

describe('inlineContextSnapshotRegistry', () => {
  afterEach(() => {
    unregisterInlineContextSnapshotProvider('analysis', '__analysis__');
    unregisterInlineContextSnapshotProvider('analysis', 'q-1');
  });

  it('should collect the latest snapshot from a registered provider', () => {
    let counter = 0;
    registerInlineContextSnapshotProvider('analysis', '__analysis__', () => {
      counter += 1;
      return { 'sql.current': `SELECT ${counter}` };
    });

    expect(collectInlineContextSnapshot('analysis', '__analysis__')).toEqual({
      'sql.current': 'SELECT 1',
    });
    expect(collectInlineContextSnapshot('analysis', '__analysis__')).toEqual({
      'sql.current': 'SELECT 2',
    });
  });

  it('should return undefined when no provider is registered', () => {
    expect(collectInlineContextSnapshot('analysis', 'missing')).toBeUndefined();
  });

  it('should stop serving stale providers after unregister', () => {
    registerInlineContextSnapshotProvider('analysis', 'q-1', () => ({ 'sql.current': 'old' }));
    unregisterInlineContextSnapshotProvider('analysis', 'q-1');
    expect(collectInlineContextSnapshot('analysis', 'q-1')).toBeUndefined();
  });

  it('should tolerate unknown context keys in provider output', () => {
    registerInlineContextSnapshotProvider('analysis', '__analysis__', () => ({
      'sql.current': 'SELECT 1',
      'custom.future.key': { nested: true },
    }));

    expect(collectInlineContextSnapshot('analysis', '__analysis__')).toEqual({
      'sql.current': 'SELECT 1',
      'custom.future.key': { nested: true },
    });
  });
});
