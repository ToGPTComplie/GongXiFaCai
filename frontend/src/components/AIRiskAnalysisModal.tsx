import { useEffect, useState } from "react";
import { getRiskAnalysis } from "../lib/api";
import type { RiskAnalysisResultDTO } from "../lib/types";

interface AIRiskAnalysisModalProps {
  userId: string;
  onClose: () => void;
}

export function AIRiskAnalysisModal({ userId, onClose }: AIRiskAnalysisModalProps) {
  const [data, setData] = useState<RiskAnalysisResultDTO | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    let isMounted = true;
    const controller = new AbortController();

    async function loadRiskAnalysis() {
      try {
        const result = await getRiskAnalysis(userId, controller.signal);
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

    loadRiskAnalysis();

    return () => {
      isMounted = false;
      controller.abort();
    };
  }, [userId]);

  function getRiskColor(level: string) {
    if (level === "HIGH") return "#ef4444";
    if (level === "MEDIUM") return "#f59e0b";
    if (level === "LOW") return "#22c55e";
    return "#64748b";
  }

  return (
    <div className="modal-overlay" onClick={onClose} style={{ zIndex: 100 }}>
      <div 
        className="modal-content" 
        onClick={(e) => e.stopPropagation()} 
        style={{ maxWidth: "600px", width: "90%", maxHeight: "90vh", overflowY: "auto" }}
      >
        <header className="modal-header" style={{ borderBottom: "1px solid #e2e8f0", paddingBottom: "1rem", marginBottom: "1rem" }}>
          <h2 style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
            <span role="img" aria-label="AI">✨</span> AI Risk Analysis
          </h2>
          <button className="btn-close" onClick={onClose} aria-label="Close" style={{ border: "none", background: "none", fontSize: "1.5rem", cursor: "pointer" }}>
            &times;
          </button>
        </header>

        {isLoading && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", padding: "3rem 0" }}>
            <div className="spinner" style={{ fontSize: "2rem", animation: "spin 1s linear infinite" }}>✨</div>
            <p style={{ marginTop: "1rem", color: "#64748b" }}>AI is analyzing your portfolio...</p>
          </div>
        )}

        {error && (
          <div className="alert alert-error">
            <p>Failed to analyze risk: {error.message}</p>
          </div>
        )}

        {!isLoading && !error && data && (
          <div className="risk-analysis-results">
            <div style={{ padding: "1rem", background: `${getRiskColor(data.overallRiskLevel)}15`, borderLeft: `4px solid ${getRiskColor(data.overallRiskLevel)}`, borderRadius: "4px", marginBottom: "1.5rem" }}>
              <h3 style={{ color: getRiskColor(data.overallRiskLevel), marginBottom: "0.5rem" }}>
                Overall Risk Level: {data.overallRiskLevel}
              </h3>
              <p>{data.overallSummary}</p>
            </div>

            <div className="risk-dimensions" style={{ display: "grid", gap: "1.5rem" }}>
              <div className="risk-card" style={{ padding: "1rem", border: "1px solid #e2e8f0", borderRadius: "8px" }}>
                <h4>Single Asset Concentration</h4>
                <span className="badge" style={{ backgroundColor: `${getRiskColor(data.singleAssetConcentration.riskLevel)}20`, color: getRiskColor(data.singleAssetConcentration.riskLevel), padding: "0.25rem 0.5rem", borderRadius: "4px", fontSize: "0.8rem", fontWeight: "bold" }}>
                  {data.singleAssetConcentration.riskLevel}
                </span>
                <ul style={{ marginTop: "0.5rem", paddingLeft: "1.5rem" }}>
                  {data.singleAssetConcentration.issues.map((issue, i) => <li key={i}>{issue}</li>)}
                </ul>
                <p style={{ marginTop: "0.5rem", fontSize: "0.9rem", color: "#475569" }}><strong>Suggestion:</strong> {data.singleAssetConcentration.suggestion}</p>
              </div>

              <div className="risk-card" style={{ padding: "1rem", border: "1px solid #e2e8f0", borderRadius: "8px" }}>
                <h4>Asset Type Concentration</h4>
                <span className="badge" style={{ backgroundColor: `${getRiskColor(data.assetTypeConcentration.riskLevel)}20`, color: getRiskColor(data.assetTypeConcentration.riskLevel), padding: "0.25rem 0.5rem", borderRadius: "4px", fontSize: "0.8rem", fontWeight: "bold" }}>
                  {data.assetTypeConcentration.riskLevel}
                </span>
                <ul style={{ marginTop: "0.5rem", paddingLeft: "1.5rem" }}>
                  {data.assetTypeConcentration.issues.map((issue, i) => <li key={i}>{issue}</li>)}
                </ul>
                <p style={{ marginTop: "0.5rem", fontSize: "0.9rem", color: "#475569" }}><strong>Suggestion:</strong> {data.assetTypeConcentration.suggestion}</p>
              </div>

              <div className="risk-card" style={{ padding: "1rem", border: "1px solid #e2e8f0", borderRadius: "8px" }}>
                <h4>Sector Concentration</h4>
                <span className="badge" style={{ backgroundColor: `${getRiskColor(data.sectorConcentration.riskLevel)}20`, color: getRiskColor(data.sectorConcentration.riskLevel), padding: "0.25rem 0.5rem", borderRadius: "4px", fontSize: "0.8rem", fontWeight: "bold" }}>
                  {data.sectorConcentration.riskLevel}
                </span>
                <ul style={{ marginTop: "0.5rem", paddingLeft: "1.5rem" }}>
                  {data.sectorConcentration.issues.map((issue, i) => <li key={i}>{issue}</li>)}
                </ul>
                <p style={{ marginTop: "0.5rem", fontSize: "0.9rem", color: "#475569" }}><strong>Suggestion:</strong> {data.sectorConcentration.suggestion}</p>
              </div>

              <div className="risk-card" style={{ padding: "1rem", border: "1px solid #e2e8f0", borderRadius: "8px" }}>
                <h4>Cash Proportion</h4>
                <span className="badge" style={{ backgroundColor: `${getRiskColor(data.cashRatio.riskLevel)}20`, color: getRiskColor(data.cashRatio.riskLevel), padding: "0.25rem 0.5rem", borderRadius: "4px", fontSize: "0.8rem", fontWeight: "bold" }}>
                  {data.cashRatio.riskLevel}
                </span>
                <ul style={{ marginTop: "0.5rem", paddingLeft: "1.5rem" }}>
                  {data.cashRatio.issues.map((issue, i) => <li key={i}>{issue}</li>)}
                </ul>
                <p style={{ marginTop: "0.5rem", fontSize: "0.9rem", color: "#475569" }}><strong>Suggestion:</strong> {data.cashRatio.suggestion}</p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

