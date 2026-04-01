import { useEffect, useMemo, useRef, useState } from "react";

interface TradingViewAdvancedChartProps {
  symbol: string;
  title: string;
}

const chartHeight = 500;

export function TradingViewAdvancedChart({ symbol, title }: TradingViewAdvancedChartProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [chartError, setChartError] = useState<string | null>(null);
  const chartTitle = useMemo(() => `${title} (${symbol})`, [symbol, title]);

  useEffect(() => {
    const container = containerRef.current;

    if (!container) {
      return;
    }

    setChartError(null);
    container.innerHTML = "";

    const script = document.createElement("script");
    script.src = "https://s3.tradingview.com/external-embedding/embed-widget-advanced-chart.js";
    script.async = true;
    script.type = "text/javascript";
    script.text = JSON.stringify({
      width: "100%",
      height: chartHeight,
      symbol,
      interval: "D",
      timezone: "Asia/Shanghai",
      theme: "light",
      style: "1",
      locale: "en",
      allow_symbol_change: false,
      calendar: false,
      support_host: "https://www.tradingview.com",
    });

    script.onerror = () => {
      setChartError("Failed to load TradingView widget");
    };

    container.appendChild(script);

    return () => {
      container.innerHTML = "";
    };
  }, [symbol]);

  return (
    <section className="panel tradingview-panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Market View</p>
          <h3>{title}</h3>
        </div>
      </div>

      {chartError ? (
        <div className="empty-state">
          <h4>Chart unavailable</h4>
          <p>{chartError}</p>
        </div>
      ) : null}

      <div
        className="tradingview-widget-shell"
        ref={containerRef}
        aria-label={chartTitle}
        style={{ height: `${chartHeight}px` }}
      />
    </section>
  );
}
