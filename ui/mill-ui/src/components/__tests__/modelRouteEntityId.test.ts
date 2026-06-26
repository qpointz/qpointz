import { describe, it, expect } from 'vitest';
import { entityIdFromModelRouteParams } from '../data-model/modelRouteEntityId';

describe('entityIdFromModelRouteParams', () => {
  it('should build dotted ids from route params', () => {
    expect(entityIdFromModelRouteParams({ schema: 'skymill' })).toBe('skymill');
    expect(entityIdFromModelRouteParams({ schema: 'skymill', table: 'passenger' })).toBe(
      'skymill.passenger',
    );
    expect(
      entityIdFromModelRouteParams({ schema: 'skymill', table: 'passenger', attribute: 'id' }),
    ).toBe('skymill.passenger.id');
  });
});
