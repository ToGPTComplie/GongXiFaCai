import { useEffect, useState } from "react";
import { Link, Outlet, useLocation, useParams, useSearchParams } from "react-router-dom";
import { TradingViewMiniChartWarmup } from "./components/TradingViewMiniChartWarmup";
import { TransactionModal } from "./components/TransactionModal";
import { AIRiskAnalysisModal } from "./components/AIRiskAnalysisModal";

function getSidebarStorageKey(userId: string) {
  return `sidebar:collapsed:${userId}`;
}

export function AppShell() {
  const location = useLocation();
  const { id = "1" } = useParams();
  const [searchParams] = useSearchParams();
  const showModal = searchParams.get("modal") === "transaction";
  const [showRiskModal, setShowRiskModal] = useState(false);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(() => {
    if (typeof window === "undefined") {
      return false;
    }

    return window.sessionStorage.getItem(getSidebarStorageKey(id)) === "true";
  });

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }

    setIsSidebarCollapsed(window.sessionStorage.getItem(getSidebarStorageKey(id)) === "true");
  }, [id]);

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }

    window.sessionStorage.setItem(getSidebarStorageKey(id), String(isSidebarCollapsed));
  }, [id, isSidebarCollapsed]);

  return (
    <div className={`app-shell ${isSidebarCollapsed ? "sidebar-collapsed" : ""}`}>
      <aside className="side-nav">
        <div className="side-nav-header">
          <div>
            <p className="eyebrow">Investment System</p>
            <h1>GXFC</h1>
          </div>
          <button
            className="sidebar-toggle-button sidebar-toggle-button-edge"
            type="button"
            aria-label="Collapse sidebar"
            onClick={() => setIsSidebarCollapsed(true)}
          >
            <span aria-hidden="true">‹</span>
          </button>
        </div>
        <nav className="nav-links" aria-label="Primary">
          <Link
            className={location.pathname === `/users/${id}` ? "nav-link active" : "nav-link"}
            to={`/users/${id}`}
          >
            Dashboard
          </Link>
          <Link
            className={
              location.pathname === `/users/${id}/transactions` ? "nav-link active" : "nav-link"
            }
            to={`/users/${id}/transactions`}
          >
            Transactions
          </Link>
          <Link
            className={
              location.pathname === `/users/${id}/watchlist` ? "nav-link active" : "nav-link"
            }
            to={`/users/${id}/watchlist`}
          >
            Watchlist
          </Link>
          <Link
            className={
              location.pathname === `/users/${id}/targets` ? "nav-link active" : "nav-link"
            }
            to={`/users/${id}/targets`}
          >
            Rebalancing
          </Link>
          <Link
            className={
              location.pathname === `/users/${id}/performance` ? "nav-link active" : "nav-link"
            }
            to={`/users/${id}/performance`}
          >
            Performance
          </Link>
        </nav>
        <div style={{ marginTop: "auto", padding: "1rem 0.5rem", borderTop: "1px solid rgba(24,34,44,0.08)" }}>
          {!isSidebarCollapsed ? (
            <button
              onClick={() => setShowRiskModal(true)}
              style={{
                width: "100%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: "0.5rem",
                padding: "0.625rem 1rem",
                background: "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)",
                color: "#fff",
                border: "none",
                borderRadius: "10px",
                cursor: "pointer",
                fontSize: "0.875rem",
                fontWeight: 600,
                boxShadow: "0 2px 8px rgba(99,102,241,0.35)",
                transition: "opacity 0.15s ease",
              }}
            >
              <span>✨</span> AI Analysis
            </button>
          ) : (
            <button
              onClick={() => setShowRiskModal(true)}
              title="AI Risk Analysis"
              style={{
                width: "2.5rem",
                height: "2.5rem",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                margin: "0 auto",
                background: "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)",
                color: "#fff",
                border: "none",
                borderRadius: "10px",
                cursor: "pointer",
                fontSize: "1.1rem",
                boxShadow: "0 2px 8px rgba(99,102,241,0.35)",
              }}
            >
              ✨
            </button>
          )}
        </div>
      </aside>
      {isSidebarCollapsed ? (
        <button
          className="sidebar-toggle-button sidebar-toggle-button-float"
          type="button"
          aria-label="Expand sidebar"
          onClick={() => setIsSidebarCollapsed(false)}
        >
          <span aria-hidden="true">›</span>
        </button>
      ) : null}
      <main className="content">
        <Outlet />
      </main>
      <TradingViewMiniChartWarmup userId={id} />
      {showModal ? <TransactionModal userId={id} /> : null}
      {showRiskModal ? <AIRiskAnalysisModal userId={id} onClose={() => setShowRiskModal(false)} /> : null}
    </div>
  );
}
