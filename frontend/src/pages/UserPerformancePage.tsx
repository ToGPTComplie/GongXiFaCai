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

  // Load summary data when period changes
  useEffect(() => {
    if (!id) return;
    
    let isMounted = true;
    const abortController = new AbortController();

    async function loadSummaryData() {
      setIsLoading(true);
      setError(null);
      try {
        const summaryResult = await getPerformanceSummary(id as string, period, abortController.signal);
        if (isMounted) {
          setData(summaryResult);
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

    loadSummaryData();

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
      
      <div 
        className="performance-controls" 
        style={{ 
          marginBottom: "2rem", 
          display: "inline-flex", 
          gap: "0.25rem",
          background: "#f1f5f9",
          padding: "0.375rem",
          borderRadius: "0.75rem",
          boxShadow: "inset 0 1px 2px rgba(0, 0, 0, 0.05)"
        }}
      >
        {periods.map((p) => (
          <button
            key={p.value}
            onClick={() => setPeriod(p.value)}
            style={{
              border: "none",
              background: period === p.value ? "#ffffff" : "transparent",
              color: period === p.value ? "#0f172a" : "#64748b",
              padding: "0.5rem 1.25rem",
              borderRadius: "0.5rem",
              cursor: "pointer",
              fontSize: "0.875rem",
              fontWeight: period === p.value ? "600" : "500",
              boxShadow: period === p.value ? "0 1px 3px rgba(0,0,0,0.1), 0 1px 2px rgba(0,0,0,0.06)" : "none",
              transition: "all 0.15s ease",
            }}
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
