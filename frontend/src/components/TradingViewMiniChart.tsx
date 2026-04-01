import { useEffect, useId, useMemo, useState } from "react";
import { ensureMiniChartScript } from "../lib/tradingview-mini-chart";

interface TradingViewMiniChartProps {
  symbol: string | null;
  title: string;
}

const miniChartHeight = 200;

export function TradingViewMiniChart({ symbol, title }: TradingViewMiniChartProps) {
  const [chartError, setChartError] = useState<string | null>(null);
  const widgetId = useId().replace(/:/g, "-");
  const chartLabel = useMemo(() => `${title} mini chart`, [title]);

  useEffect(() => {
    let cancelled = false;

    if (!symbol) {
      setChartError(null);
      return;
    }

    setChartError(null);

    void ensureMiniChartScript().catch((error: unknown) => {
      if (!cancelled) {
        setChartError(error instanceof Error ? error.message : "Failed to load chart");
      }
    });

    return () => {
      cancelled = true;
    };
  }, [symbol]);

  if (!symbol) {
    return (
      <div className="watchlist-mini-chart watchlist-mini-chart-empty" aria-label={`${title} chart unavailable`}>
        <span>Chart unavailable</span>
      </div>
    );
  }

  if (chartError) {
    return (
      <div className="watchlist-mini-chart watchlist-mini-chart-fallback" aria-label={`${title} chart unavailable`}>
        <span>{chartError}</span>
      </div>
    );
  }

  return (
    <div className="watchlist-mini-chart" aria-label={chartLabel}>
      <div className="tradingview-widget-container">
        <div className="tradingview-widget-container__widget">
          <tv-mini-chart
            id={widgetId}
            symbol={symbol}
            line-chart-type="Baseline"
            style={{ width: "100%", height: `${miniChartHeight}px` }}
          />
        </div>
      </div>
    </div>
  );
}
