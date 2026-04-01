import type {
  ApiResult,
  FundTransactionRequest,
  FundTransaction,
  Holding,
  PageResponse,
  PaginationParams,
  TradeTransactionRequest,
  TradeTransaction,
  UserInfo,
  UserPortfolio,
  WatchlistItem,
  WatchlistItemRequest,
  TargetAllocation,
  TargetAllocationRequest,
  RebalancePreviewPlan,
} from "./types";

const mockPortfolio: UserPortfolio = {
  id: 1,
  name: "Primary Investment Account",
  availableCash: 128500,
  trackedAssetValue: 356900,
  holdingsCount: 3,
  valuationMode: "cost",
  allocation: {
    cash: 36,
    stock: 52,
    bond: 12,
  },
  holdings: [
    { id: 1, ticker: "AAPL", assetType: "STOCK", quantity: 100, averageCost: 150, positionCost: 15000 },
    { id: 2, ticker: "TSLA", assetType: "STOCK", quantity: 40, averageCost: 210, positionCost: 8400 },
    { id: 3, ticker: "T-BOND", assetType: "BOND", quantity: 20, averageCost: 100, positionCost: 2000 },
  ],
};

function delay(ms = 200) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms);
  });
}

async function parseApiResult<T>(response: Response): Promise<ApiResult<T> | null> {
  const text = await response.text();

  if (!text.trim()) {
    return null;
  }

  return JSON.parse(text) as ApiResult<T>;
}

async function apiGet<T>(path: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(path, {
    method: "GET",
    headers: {
      Accept: "application/json",
    },
    signal,
  });

  const result = await parseApiResult<T>(response);

  if (result === null) {
    throw new Error("Request returned an empty response");
  }

  if (result.code !== 200) {
    throw new Error(result.message || "Request failed");
  }

  return result.data;
}

async function apiPost<TResponse, TRequest>(path: string, payload: TRequest): Promise<TResponse> {
  const response = await fetch(path, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const result = await parseApiResult<TResponse>(response);

  if (result === null) {
    if (!response.ok) {
      throw new Error("Request failed");
    }

    return undefined as TResponse;
  }

  if (result.code !== 200) {
    throw new Error(result.message || "Request failed");
  }

  return result.data;
}

async function apiPut<TResponse, TRequest>(path: string, payload: TRequest): Promise<TResponse> {
  const response = await fetch(path, {
    method: "PUT",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const result = await parseApiResult<TResponse>(response);

  if (result === null) {
    if (!response.ok) {
      throw new Error("Request failed");
    }

    return undefined as TResponse;
  }

  if (result.code !== 200) {
    throw new Error(result.message || "Request failed");
  }

  return result.data;
}

async function apiDelete(path: string): Promise<void> {
  const response = await fetch(path, {
    method: "DELETE",
    headers: {
      Accept: "application/json",
    },
  });

  const result = await parseApiResult<void>(response);

  if (result === null) {
    if (!response.ok) {
      throw new Error("Request failed");
    }

    return;
  }

  if (result.code !== 200) {
    throw new Error(result.message || "Request failed");
  }
}

function buildQueryString(params: PaginationParams) {
  const searchParams = new URLSearchParams();

  if (params.page !== undefined) {
    searchParams.set("page", String(params.page));
  }

  if (params.size !== undefined) {
    searchParams.set("size", String(params.size));
  }

  const queryString = searchParams.toString();
  return queryString ? `?${queryString}` : "";
}

export async function getUserPortfolio(_userId: string): Promise<UserPortfolio> {
  await delay();
  return mockPortfolio;
}

export async function getUserInfo(userId: string, signal?: AbortSignal): Promise<UserInfo> {
  return apiGet<UserInfo>(`/api/v1/users/${userId}`, signal);
}

export async function getUserHoldings(userId: string, signal?: AbortSignal): Promise<Holding[]> {
  return apiGet<Holding[]>(`/api/v1/users/${userId}/holdings`, signal);
}

export async function getMarketPrice(userId: string, ticker: string, signal?: AbortSignal): Promise<number> {
  return apiGet<number>(`/api/v1/users/${userId}/market-price/${ticker}`, signal);
}

export async function getUserTradeTransactions(
  userId: string,
  params: PaginationParams = {},
  signal?: AbortSignal,
): Promise<PageResponse<TradeTransaction>> {
  return apiGet<PageResponse<TradeTransaction>>(
    `/api/v1/users/${userId}/trade-transactions${buildQueryString(params)}`,
    signal,
  );
}

export async function getUserFundTransactions(
  userId: string,
  params: PaginationParams = {},
  signal?: AbortSignal,
): Promise<PageResponse<FundTransaction>> {
  return apiGet<PageResponse<FundTransaction>>(
    `/api/v1/users/${userId}/fund-transactions${buildQueryString(params)}`,
    signal,
  );
}

export async function createTradeTransaction(
  userId: string,
  payload: TradeTransactionRequest,
): Promise<TradeTransaction> {
  return apiPost<TradeTransaction, TradeTransactionRequest>(
    `/api/v1/users/${userId}/trade-transactions`,
    payload,
  );
}

export async function createFundTransaction(
  userId: string,
  payload: FundTransactionRequest,
): Promise<FundTransaction> {
  return apiPost<FundTransaction, FundTransactionRequest>(
    `/api/v1/users/${userId}/fund-transactions`,
    payload,
  );
}

export async function getUserWatchlist(userId: string, signal?: AbortSignal): Promise<WatchlistItem[]> {
  return apiGet<WatchlistItem[]>(`/api/v1/users/${userId}/watchlist`, signal);
}

export async function createWatchlistItem(
  userId: string,
  payload: WatchlistItemRequest,
): Promise<WatchlistItem> {
  return apiPost<WatchlistItem, WatchlistItemRequest>(`/api/v1/users/${userId}/watchlist`, payload);
}

export async function updateWatchlistItem(
  userId: string,
  itemId: number,
  payload: WatchlistItemRequest,
): Promise<WatchlistItem> {
  return apiPut<WatchlistItem, WatchlistItemRequest>(`/api/v1/users/${userId}/watchlist/${itemId}`, payload);
}

export async function deleteWatchlistItem(userId: string, itemId: number): Promise<void> {
  return apiDelete(`/api/v1/users/${userId}/watchlist/${itemId}`);
}

export async function saveTargetAllocations(
  userId: string,
  payload: TargetAllocationRequest[],
): Promise<void> {
  await apiPost<void, TargetAllocationRequest[]>(`/api/v1/users/${userId}/portfolio/target`, payload);
}

export async function getTargetAllocations(userId: string, signal?: AbortSignal): Promise<TargetAllocation[]> {
  return apiGet<TargetAllocation[]>(`/api/v1/users/${userId}/portfolio/target`, signal);
}

export async function previewRebalance(userId: string): Promise<RebalancePreviewPlan[]> {
  return apiGet<RebalancePreviewPlan[]>(`/api/v1/users/${userId}/portfolio/rebalance-preview`);
}

export async function executeRebalance(userId: string): Promise<RebalancePreviewPlan[]> {
  return apiPost<RebalancePreviewPlan[], undefined>(`/api/v1/users/${userId}/portfolio/rebalance-execute`, undefined);
}
