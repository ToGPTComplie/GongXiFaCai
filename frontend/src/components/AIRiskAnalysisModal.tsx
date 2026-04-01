import { useEffect, useRef, useState } from "react";
import { getRiskAnalysis } from "../lib/api";
import type { RiskAnalysisResultDTO } from "../lib/types";

interface AIRiskAnalysisModalProps {
  userId: string;
  onClose: () => void;
}

function RiskBadge({ level }: { level: string }) {
  const color =
    level === "HIGH" ? "#ef4444" : level === "MEDIUM" ? "#f59e0b" : "#22c55e";
  return (
    <span
      style={{
        display: "inline-block",
        padding: "0.15rem 0.55rem",
        borderRadius: "999px",
        fontSize: "0.75rem",
        fontWeight: 700,
        background: `${color}18`,
        color,
        border: `1px solid ${color}40`,
      }}
    >
      {level}
    </span>
  );
}

function RiskSection({
  title,
  level,
  issues,
  suggestion,
}: {
  title: string;
  level: string;
  issues: string[];
  suggestion: string;
}) {
  return (
    <div
      style={{
        padding: "0.875rem",
        borderRadius: "10px",
        background: "#f8fafc",
        border: "1px solid #e2e8f0",
        display: "flex",
        flexDirection: "column",
        gap: "0.5rem",
      }}
    >
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <span style={{ fontWeight: 600, fontSize: "0.875rem" }}>{title}</span>
        <RiskBadge level={level} />
      </div>
      {issues.length > 0 && (
        <ul style={{ margin: 0, paddingLeft: "1.2rem", fontSize: "0.8rem", color: "#475569" }}>
          {issues.map((issue, i) => (
            <li key={i}>{issue}</li>
          ))}
        </ul>
      )}
      <p style={{ margin: 0, fontSize: "0.8rem", color: "#64748b", borderTop: "1px solid #e2e8f0", paddingTop: "0.4rem" }}>
        💡 {suggestion}
      </p>
    </div>
  );
}

export function AIRiskAnalysisModal({ userId, onClose }: AIRiskAnalysisModalProps) {
  const [data, setData] = useState<RiskAnalysisResultDTO | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const panelRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let isMounted = true;
    const controller = new AbortController();

    async function load() {
      try {
        const result = await getRiskAnalysis(userId, controller.signal);
        if (isMounted) setData(result);
      } catch (err: any) {
        if (err.name === "AbortError") return;
        if (isMounted) setError(err);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    }

    load();

    return () => {
      isMounted = false;
      controller.abort();
    };
  }, [userId]);

  // Close on outside click
  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (panelRef.current && !panelRef.current.contains(e.target as Node)) {
        onClose();
      }
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [onClose]);

  const overallColor =
    data?.overallRiskLevel === "HIGH"
      ? "#ef4444"
      : data?.overallRiskLevel === "MEDIUM"
      ? "#f59e0b"
      : "#22c55e";

  return (
    <div
      ref={panelRef}
      style={{
        position: "fixed",
        bottom: "4.5rem",
        left: "1.5rem",
        width: "360px",
        maxHeight: "70vh",
        background: "#ffffff",
        borderRadius: "16px",
        boxShadow: "0 20px 60px rgba(0,0,0,0.15), 0 4px 16px rgba(0,0,0,0.08)",
        border: "1px solid #e2e8f0",
        display: "flex",
        flexDirection: "column",
        overflow: "hidden",
        zIndex: 200,
        animation: "slideUp 0.2s ease",
      }}
    >
      <style>{`
        @keyframes slideUp {
          from { opacity: 0; transform: translateY(16px); }
          to   { opacity: 1; transform: translateY(0); }
        }
        @keyframes pulse {
          0%, 100% { opacity: 1; }
          50%       { opacity: 0.4; }
        }
      `}</style>

      {/* Header */}
      <div
        style={{
          padding: "1rem 1.25rem",
          background: "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexShrink: 0,
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
          <span style={{ fontSize: "1.1rem" }}>✨</span>
          <span style={{ color: "#fff", fontWeight: 700, fontSize: "0.95rem" }}>AI Risk Analysis</span>
        </div>
        <button
          onClick={onClose}
          style={{
            background: "rgba(255,255,255,0.2)",
            border: "none",
            borderRadius: "6px",
            color: "#fff",
            width: "1.75rem",
            height: "1.75rem",
            cursor: "pointer",
            fontSize: "1rem",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          ×
        </button>
      </div>

      {/* Body */}
      <div style={{ overflowY: "auto", padding: "1rem", display: "flex", flexDirection: "column", gap: "0.75rem" }}>

        {isLoading && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", padding: "2.5rem 0", gap: "0.75rem" }}>
            <span style={{ fontSize: "2rem", animation: "pulse 1.2s ease-in-out infinite" }}>✨</span>
            <p style={{ margin: 0, color: "#64748b", fontSize: "0.875rem" }}>
              AI is analyzing your portfolio...
            </p>
          </div>
        )}

        {error && (
          <div style={{ padding: "1rem", background: "#fef2f2", borderRadius: "8px", color: "#dc2626", fontSize: "0.875rem" }}>
            Failed to analyze: {error.message}
          </div>
        )}

        {!isLoading && !error && data && (
          <>
            {/* Overall banner */}
            <div
              style={{
                padding: "0.875rem",
                borderRadius: "10px",
                background: `${overallColor}10`,
                border: `1px solid ${overallColor}30`,
              }}
            >
              <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "0.4rem" }}>
                <span style={{ fontWeight: 700, fontSize: "0.9rem" }}>Overall Risk</span>
                <RiskBadge level={data.overallRiskLevel} />
              </div>
              <p style={{ margin: 0, fontSize: "0.82rem", color: "#475569", lineHeight: 1.6 }}>
                {data.overallSummary}
              </p>
            </div>

            <RiskSection
              title="Single Asset Concentration"
              level={data.singleAssetConcentration.riskLevel}
              issues={data.singleAssetConcentration.issues}
              suggestion={data.singleAssetConcentration.suggestion}
            />
            <RiskSection
              title="Asset Type Concentration"
              level={data.assetTypeConcentration.riskLevel}
              issues={data.assetTypeConcentration.issues}
              suggestion={data.assetTypeConcentration.suggestion}
            />
            <RiskSection
              title="Sector Concentration"
              level={data.sectorConcentration.riskLevel}
              issues={data.sectorConcentration.issues}
              suggestion={data.sectorConcentration.suggestion}
            />
            <RiskSection
              title="Cash Proportion"
              level={data.cashRatio.riskLevel}
              issues={data.cashRatio.issues}
              suggestion={data.cashRatio.suggestion}
            />
          </>
        )}
      </div>
    </div>
  );
}

