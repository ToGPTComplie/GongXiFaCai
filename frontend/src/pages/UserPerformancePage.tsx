import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getPerformanceSummary } from "../lib/api";
import type { PerformancePeriod, PerformanceSummaryDTO } from "../lib/types";

export function UserPerformancePage() {
  const { id } = useParams<{ id: string }>();
  const [period, setPeriod] = useState<PerformancePeriod>("ALL");
  const [data, setData] = useState<PerformanceSummaryDTO | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (!id) return;
    
    let isMounted = true;
    const abortController = new AbortController();

    async function loadData() {
      setIsLoading(true);
      setError(null);
      try {
        const result = await getPerformanceSummary(id as string, period, abortController.signal);
        if (isMounted) {
          setData(result);
        }
      } catch (err: any) {
        if (err.name === "AbortError") return;
        if (isMounted) {
          setError(err);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadData();

    return () => {
      isMounted = false;
      abortController.abort();
    };
  }, [id, period]);

  const periods: { label: string; value: PerformancePeriod }[] = [
    { label: "24H", value: "H24" },
    { label: "7D", value: "D7" },
    { label: "30D", value: "D30" },
    { label: "90D", value: "D90" },
    { label: "YTD", value: "YTD" },
    { label: "ALL", value: "ALL" },
  ];

  return (
    <div className="page-container">
      <header className="page-header">
        <h1>Trading Performance</h1>
      </header>
      
      <div className="performance-controls" style={{ marginBottom: "2rem", display: "flex", gap: "0.5rem" }}>
        {periods.map((p) => (
          <button
            key={p.value}
            onClick={() => setPeriod(p.value)}
            className={`btn ${period === p.value ? "btn-primary" : "btn-secondary"}`}
          >
            {p.label}
          </button>
        ))}
      </div>

      {isLoading && <p>Loading performance data...</p>}
      
      {error && (
        <div className="alert alert-error">
          <p>Failed to load performance data: {error.message}</p>
        </div>
      )}

      {!isLoading && !error && data && (
        <div className="dashboard-grid" style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))", gap: "1rem" }}>
          
          <div className="card stat-card">
            <h3>P/L (Profit/Loss)</h3>
            <p className={`stat-value ${data.realizedPnl >= 0 ? "text-success" : "text-danger"}`} style={{ color: data.realizedPnl >= 0 ? "green" : "red", fontSize: "2rem", fontWeight: "bold" }}>
              ${data.realizedPnl ? data.realizedPnl.toFixed(2) : "0.00"}
            </p>
          </div>

          <div className="card stat-card" style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
            <h3>Win Rate</h3>
            <div style={{ position: "relative", width: "120px", height: "120px", marginTop: "1rem" }}>
              <svg viewBox="0 0 100 100" width="120" height="120">
                <circle cx="50" cy="50" r="40" fill="transparent" stroke="#eee" strokeWidth="8" />
                <circle
                  cx="50"
                  cy="50"
                  r="40"
                  fill="transparent"
                  stroke={data.winRate !== null ? (data.winRate >= 0.5 ? "green" : "red") : "#ccc"}
                  strokeWidth="8"
                  strokeDasharray={`${(data.winRate ?? 0) * 2 * Math.PI * 40} 251.2`}
                  strokeDashoffset="0"
                  transform="rotate(-90 50 50)"
                  strokeLinecap="round"
                />
              </svg>
              <div style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", display: "flex", alignItems: "center", justifyContent: "center", fontWeight: "bold", fontSize: "1.2rem" }}>
                {data.winRate !== null ? `${(data.winRate * 100).toFixed(1)}%` : "N/A"}
              </div>
            </div>
          </div>

          <div className="card stat-card">
            <h3>Total Trades</h3>
            <p className="stat-value" style={{ fontSize: "2rem", fontWeight: "bold", marginTop: "1rem" }}>
              {data.totalTrades}
            </p>
          </div>

          <div className="card stat-card">
            <h3>Profit Factor</h3>
            <p className="stat-value" style={{ fontSize: "2rem", fontWeight: "bold", marginTop: "1rem" }}>
              {data.profitFactor !== null ? data.profitFactor.toFixed(2) : "N/A"}
            </p>
          </div>

          <div className="card stat-card">
            <h3>Average Holding Time</h3>
            <p className="stat-value" style={{ fontSize: "2rem", fontWeight: "bold", marginTop: "1rem" }}>
              {data.avgHoldingDays !== null ? `${data.avgHoldingDays.toFixed(1)} Days` : "N/A"}
            </p>
          </div>
          
        </div>
      )}
    </div>
  );
}

