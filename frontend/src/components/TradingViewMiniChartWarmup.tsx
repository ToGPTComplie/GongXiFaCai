import { useEffect, useMemo, useState } from "react";
import { buildTradingViewSymbol } from "../lib/tradingview";
import { ensureMiniChartScript } from "../lib/tradingview-mini-chart";
import { readWatchlistCache, subscribeWatchlistCache } from "../lib/watchlist-cache";
import type { WatchlistItem } from "../lib/types";

interface TradingViewMiniChartWarmupProps {
  userId: string;
}

function scheduleIdleWork(callback: () => void) {
  const requestIdle = window.requestIdleCallback;

  if (typeof requestIdle === "function") {
    const handle = requestIdle(callback, { timeout: 1500 });
    return () => window.cancelIdleCallback?.(handle);
  }

  const timeoutId = window.setTimeout(callback, 180);
  return () => window.clearTimeout(timeoutId);
}

export function TradingViewMiniChartWarmup({ userId }: TradingViewMiniChartWarmupProps) {
  const [items, setItems] = useState<WatchlistItem[]>(() => readWatchlistCache(userId) ?? []);
  const [scriptReady, setScriptReady] = useState(false);

  useEffect(() => {
    setItems(readWatchlistCache(userId) ?? []);
    return subscribeWatchlistCache(userId, () => {
      setItems(readWatchlistCache(userId) ?? []);
    });
  }, [userId]);

  useEffect(() => {
    const cancelIdle = scheduleIdleWork(() => {
      void ensureMiniChartScript()
        .then(() => setScriptReady(true))
        .catch(() => {
          // A failed warmup should stay invisible and not affect the app.
        });
    });

    return cancelIdle;
  }, []);

  const symbols = useMemo(() => {
    const uniqueSymbols = new Set<string>();

    items.forEach((item) => {
      const symbol = buildTradingViewSymbol(item);

      if (symbol) {
        uniqueSymbols.add(symbol);
      }
    });

    return [...uniqueSymbols];
  }, [items]);

  if (!scriptReady || symbols.length === 0) {
    return null;
  }

  return (
    <div className="mini-chart-warmup-layer" aria-hidden="true">
      {symbols.map((symbol) => (
        <tv-mini-chart
          key={symbol}
          symbol={symbol}
          line-chart-type="Baseline"
          style={{ width: "220px", height: "120px" }}
        />
      ))}
    </div>
  );
}
