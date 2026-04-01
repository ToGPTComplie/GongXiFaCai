import type { WatchlistItem } from "./types";

export function buildTradingViewSymbol(item: WatchlistItem): string | null {
  const ticker = item.ticker.trim().toUpperCase();

  if (!ticker) {
    return null;
  }

  if (item.assetType === "STOCK") {
    return `NASDAQ:${ticker}`;
  }

  return null;
}
