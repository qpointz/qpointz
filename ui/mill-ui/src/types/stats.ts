export interface DashboardStats {
  schemaCount: number;
  tableCount: number;
  conceptCount: number;
  queryCount: number;
}

export interface StatsService {
  getStats(): Promise<DashboardStats>;
}
