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
  targetPercentage: number;
}

export interface TargetAllocationRow {
  id: string;
  ticker: string;
  assetType: "STOCK" | "BOND";
  targetPercentInput: string;
}
