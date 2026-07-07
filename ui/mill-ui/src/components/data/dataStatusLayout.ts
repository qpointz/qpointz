export function getDataStatusLayout(compact: boolean) {
  return {
    minHeight: compact ? 140 : 220,
    iconSize: compact ? 36 : 48,
    labelSize: compact ? 'lg' as const : 'xl' as const,
    gap: compact ? 8 : 10,
  };
}
