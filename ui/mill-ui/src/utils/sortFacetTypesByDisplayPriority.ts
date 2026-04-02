/**
 * Reorders facet type URNs for display: apply {@code priorityList} as a prefix (only types
 * that appear in {@code arrivalOrder}), then append the rest in {@code arrivalOrder}.
 *
 * @param arrivalOrder facet types in server / constellation order (first occurrence wins)
 * @param priorityList preferred leading order; unrelated types are unchanged relative to {@code arrivalOrder}
 */
export function sortFacetTypesByDisplayPriority(
  arrivalOrder: readonly string[],
  priorityList: readonly string[],
): string[] {
  const inArrival = new Set(arrivalOrder);
  const placed = new Set<string>();
  const result: string[] = [];
  for (const p of priorityList) {
    if (inArrival.has(p) && !placed.has(p)) {
      result.push(p);
      placed.add(p);
    }
  }
  for (const k of arrivalOrder) {
    if (!placed.has(k)) {
      result.push(k);
      placed.add(k);
    }
  }
  return result;
}
