export interface Holding {
  id: number;
  ticker: string;
  assetType: "STOCK" | "BOND";
  quantity: number;
  averageCost: number;
}

export interface UserInfo {
  id: number;
  name: string;
  availableCash: number;
}

export interface HoldingRow {
  id: number;
  ticker: string;
  assetType: "STOCK" | "BOND";
  quantity: number;
  averageCost: number;
  positionCost: number;
  marketValue?: number | null;
  pl?: number | null;
}

export interface UserPortfolio {
  id: number;
  name: string;
  availableCash: number;
  trackedAssetValue: number;
  holdingsCount: number;
  valuationMode: "cost" | "live";
  allocation: {
    cash: number;
    stock: number;
    bond: number;
  };
  holdings: HoldingRow[];
}

export interface LiveHoldingSnapshot {
  id: number;
  ticker: string;
  assetType: "STOCK" | "BOND";
  quantity: number;
  averageCost: number;
  marketValue: number;
  pl: number;
}

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface TradeTransaction {
  id: number;
  userId: number;
  ticker: string;
  assetType: "STOCK" | "BOND";
  transactionType: "BUY" | "SELL";
  quantity: number;
  price: number;
  totalAmount: number;
  createdAt: string;
}

export interface FundTransaction {
  id: number;
  userId: number;
  transactionType: "DEPOSIT" | "WITHDRAW";
  totalAmount: number;
  description: string | null;
  createdAt: string;
}

export interface PaginationParams {
  page?: number;
  size?: number;
}

export interface TradeTransactionRequest {
  ticker: string;
  assetType: "STOCK" | "BOND";
  transactionType: "BUY" | "SELL";
  quantity: number;
  price: number;
}

export interface FundTransactionRequest {
  transactionType: "DEPOSIT" | "WITHDRAW";
  amount: number;
  description?: string;
}

export interface WatchlistItem {
  id: number;
  userId: number;
  ticker: string;
  assetType: "STOCK" | "BOND";
  notes: string | null;
  targetBuyPrice: number | null;
  alertPriceHigh: number | null;
  alertPriceLow: number | null;
  currentPrice: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface WatchlistItemRequest {
  ticker: string;
  assetType: "STOCK" | "BOND";
  notes?: string;
  targetBuyPrice?: number;
  alertPriceHigh?: number;
  alertPriceLow?: number;
}

export interface TargetAllocationRequest {
  ticker: string;
  assetType: "STOCK" | "BOND";
  targetPercentage: number;
}

export interface TargetAllocationRow {
  id: string;
  ticker: string;
  assetType: "STOCK" | "BOND";
  targetPercentInput: string;
}

export interface TargetAllocation {
  ticker: string;
  assetType: "STOCK" | "BOND" | null;
  targetPercentage: number;
}

export interface RebalancePreviewPlan {
  ticker: string;
  assetType: "STOCK" | "BOND" | null;
  transactionType: "BUY" | "SELL";
  marketPrice: number;
  currentPercentage: number;
  targetPercentage: number;
  diffPercentage: number;
  tradeAmount: number;
  tradeQuantity: number;
}

export type PerformancePeriod = "H24" | "D7" | "D30" | "D90" | "YTD" | "ALL";

export interface PerformanceSummaryDTO {
  period: string;
  realizedPnl: number;
  totalTrades: number;
  winRate: number | null;
  profitFactor: number | null;
  avgHoldingDays: number | null;
}

export interface PortfolioPerformanceDTO {
  date: string;
  realizedPnl: number;
  unrealizedPnl: number;
  totalPnl: number;
}

export interface HoldingRiskDTO {
  ticker: string;
  assetType: string;
  marketValue: number;
  percentage: number;
}

export interface RiskDimensionDTO {
  riskLevel: "HIGH" | "MEDIUM" | "LOW";
  issues: string[];
  suggestion: string;
}

export interface SectorConcentrationDTO {
  riskLevel: "HIGH" | "MEDIUM" | "LOW";
  sectors: Record<string, number>;
  issues: string[];
  suggestion: string;
}

export interface RiskAnalysisResultDTO {
  totalAssets: number;
  cashAmount: number;
  cashPercentage: number;
  holdings: HoldingRiskDTO[];
  assetTypeBreakdown: Record<string, number>;
  overallRiskLevel: "HIGH" | "MEDIUM" | "LOW";
  overallSummary: string;
  singleAssetConcentration: RiskDimensionDTO;
  assetTypeConcentration: RiskDimensionDTO;
  sectorConcentration: SectorConcentrationDTO;
  cashRatio: RiskDimensionDTO;
}
