import { useEffect, useState } from "react";
import { Link, Outlet, useLocation, useParams, useSearchParams } from "react-router-dom";
import { TradingViewMiniChartWarmup } from "./components/TradingViewMiniChartWarmup";
import { TransactionModal } from "./components/TransactionModal";

function getSidebarStorageKey(userId: string) {
  return `sidebar:collapsed:${userId}`;
}

export function AppShell() {
  const location = useLocation();
  const { id = "1" } = useParams();
  const [searchParams] = useSearchParams();
  const showModal = searchParams.get("modal") === "transaction";
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
            Targets
          </Link>
        </nav>
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
    </div>
  );
}
