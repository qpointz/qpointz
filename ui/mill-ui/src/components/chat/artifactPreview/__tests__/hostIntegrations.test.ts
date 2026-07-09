import { describe, it, expect, beforeEach } from 'vitest';
import {
  dispatchInlineHostAction,
  registerInlineHostHandler,
} from '../hostIntegrations';

describe('hostIntegrations', () => {
  beforeEach(() => {
    registerInlineHostHandler('inline-analysis', () => false);
  });

  it('should dispatch sql.apply to registered handler', () => {
    const calls: string[] = [];
    registerInlineHostHandler('inline-analysis', (action) => {
      if (action.type === 'sql.apply') {
        calls.push(action.artifact.sql);
        return true;
      }
      return false;
    });

    const handled = dispatchInlineHostAction('inline-analysis', {
      type: 'sql.apply',
      artifact: { kind: 'sql', sql: 'SELECT 1' },
    });

    expect(handled).toBe(true);
    expect(calls).toEqual(['SELECT 1']);
  });

  it('should return false when no handler is registered', () => {
    registerInlineHostHandler('inline-analysis', () => false);
    const handled = dispatchInlineHostAction('inline-analysis', {
      type: 'sql.copy',
      artifact: { kind: 'sql', sql: 'SELECT 1' },
    });
    expect(handled).toBe(false);
  });
});
