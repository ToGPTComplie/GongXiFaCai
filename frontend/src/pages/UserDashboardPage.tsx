import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { AllocationChart } from "../components/AllocationChart";
import { createWatchlistItem, getUserHoldings, getUserInfo, getUserWatchlist } from "../lib/api";
import { formatCurrency } from "../lib/format";
import { subscribeToTopic } from "../lib/stomp";
import { readWatchlistCache, subscribeWatchlistCache, writeWatchlistCache } from "../lib/watchlist-cache";
import type { Holding, HoldingRow, LiveHoldingSnapshot, UserInfo, UserPortfolio, WatchlistItem } from "../lib/types";

const portfolioCache = new Map<string, UserPortfolio>();

function buildPortfolioModel(userInfo: UserInfo, holdings: Holding[], liveHoldings: LiveHoldingSnapshot[] = []): UserPortfolio {
  const liveHoldingsByTicker = new Map(liveHoldings.map((holding) => [holding.ticker, holding]));

  const holdingsRows: HoldingRow[] = holdings.map((holding) => {
    const liveHolding = liveHoldingsByTicker.get(holding.ticker);

    return {
      id: holding.id,
      ticker: holding.ticker,
      assetType: holding.assetType,
      quantity: holding.quantity,
      averageCost: holding.averageCost,
      positionCost: holding.quantity * holding.averageCost,
      marketValue: liveHolding?.marketValue ?? null,
      pl: liveHolding?.pl ?? null,
    };
  });

  const hasLiveValuation = holdingsRows.some((holding) => holding.marketValue !== null);

  const stockValue = holdingsRows
    .filter((holding) => holding.assetType === "STOCK")
    .reduce((sum, holding) => sum + (hasLiveValuation ? (holding.marketValue ?? 0) : holding.positionCost), 0);
  const bondValue = holdingsRows
    .filter((holding) => holding.assetType === "BOND")
    .reduce((sum, holding) => sum + (hasLiveValuation ? (holding.marketValue ?? 0) : holding.positionCost), 0);
  const trackedAssetValue = userInfo.availableCash + stockValue + bondValue;

  if (trackedAssetValue <= 0) {
    return {
      id: userInfo.id,
      name: userInfo.name,
      availableCash: userInfo.availableCash,
      trackedAssetValue: 0,
      holdingsCount: holdingsRows.length,
      valuationMode: hasLiveValuation ? "live" : "cost",
      allocation: {
        cash: 0,
        stock: 0,
        bond: 0,
      },
      holdings: holdingsRows,
    };
  }

  const cashAllocation = Math.round((userInfo.availableCash / trackedAssetValue) * 100);
  const stockAllocation = Math.round((stockValue / trackedAssetValue) * 100);
  const bondAllocation = Math.max(0, 100 - cashAllocation - stockAllocation);

  return {
    id: userInfo.id,
    name: userInfo.name,
    availableCash: userInfo.availableCash,
    trackedAssetValue,
    holdingsCount: holdingsRows.length,
    valuationMode: hasLiveValuation ? "live" : "cost",
    allocation: {
      cash: cashAllocation,
      stock: stockAllocation,
      bond: bondAllocation,
    },
    holdings: holdingsRows,
  };
}

export function UserDashboardPage() {
  const { id = "1" } = useParams();
  const navigate = useNavigate();
  const [portfolio, setPortfolio] = useState<UserPortfolio | null>(() => portfolioCache.get(id) ?? null);
  const [watchlistItems, setWatchlistItems] = useState<WatchlistItem[]>(() => readWatchlistCache(id) ?? []);
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);
  const [contentPhase, setContentPhase] = useState<"idle" | "updating">("idle");
  const [liveStatus, setLiveStatus] = useState<"offline" | "live">("offline");
  const [openingTicker, setOpeningTicker] = useState<string | null>(null);

  useEffect(() => {
    setWatchlistItems(readWatchlistCache(id) ?? []);

    return subscribeWatchlistCache(id, () => {
      setWatchlistItems(readWatchlistCache(id) ?? []);
    });
  }, [id]);

  useEffect(() => {
    function handlePortfolioRefresh() {
      setRefreshKey((current) => current + 1);
    }

    window.addEventListener("portfolio:refresh", handlePortfolioRefresh);
    return () => {
      window.removeEventListener("portfolio:refresh", handlePortfolioRefresh);
    };
  }, []);

  useEffect(() => {
    const cachedPortfolio = portfolioCache.get(id) ?? null;
    setPortfolio(cachedPortfolio);
    const controller = new AbortController();

    async function loadPortfolio() {
      setError(null);
      setContentPhase(cachedPortfolio ? "updating" : "idle");

      try {
        const [userInfo, holdings] = await Promise.all([
          getUserInfo(id, controller.signal),
          getUserHoldings(id, controller.signal),
        ]);

        const nextPortfolio = buildPortfolioModel(userInfo, holdings);
        portfolioCache.set(id, nextPortfolio);
        setPortfolio(nextPortfolio);
        setContentPhase("idle");

        if (readWatchlistCache(id) === null) {
          void getUserWatchlist(id, controller.signal)
            .then((watchlistItems) => {
              writeWatchlistCache(id, watchlistItems);
            })
            .catch(() => {
              // Silent prefetch failure should not affect the dashboard.
            });
        }
      } catch (loadError) {
        if (!controller.signal.aborted) {
          setError(loadError instanceof Error ? loadError.message : "Failed to load user portfolio");
          setContentPhase("idle");
        }
      }
    }

    void loadPortfolio();

    return () => {
      controller.abort();
    };
  }, [id, refreshKey]);

  useEffect(() => {
    setLiveStatus("offline");

    const unsubscribe = subscribeToTopic<LiveHoldingSnapshot[]>({
      destination: `/topic/users/${id}/holdings`,
      onMessage: (liveHoldings) => {
        setPortfolio((current) => {
          if (!current) {
            return current;
          }

          const baseHoldings: Holding[] = current.holdings.map((holding) => ({
            id: holding.id,
            ticker: holding.ticker,
            assetType: holding.assetType,
            quantity: holding.quantity,
            averageCost: holding.averageCost,
          }));

          const nextPortfolio = buildPortfolioModel(
            {
              id: current.id,
              name: current.name,
              availableCash: current.availableCash,
            },
            baseHoldings,
            liveHoldings,
          );

          portfolioCache.set(id, nextPortfolio);
          return nextPortfolio;
        });

        setLiveStatus("live");
      },
      onError: () => {
        setLiveStatus("offline");
      },
    });

    return () => {
      unsubscribe();
    };
  }, [id]);

  if (error && portfolio === null) {
    return <section className="page-state">Failed to load dashboard. {error}</section>;
  }

  if (portfolio === null) {
    return (
      <section className="page-layout">
        <header className="page-header">
          <div>
            <p className="eyebrow">User Portfolio</p>
            <h2 className="loading-title">Loading dashboard</h2>
            <p className="subtle-text">Fetching portfolio summary from the backend.</p>
          </div>
        </header>
      </section>
    );
  }

  const watchlistByTicker = new Map(
    watchlistItems.map((item) => [`${item.ticker.trim().toUpperCase()}-${item.assetType}`, item]),
  );

  async function handleOpenHoldingDetail(holding: HoldingRow) {
    const key = `${holding.ticker.trim().toUpperCase()}-${holding.assetType}`;
    const existingItem = watchlistByTicker.get(key);

    if (existingItem) {
      navigate(`/users/${id}/watchlist/${existingItem.id}`);
      return;
    }

    try {
      setOpeningTicker(key);
      const createdItem = await createWatchlistItem(id, {
        ticker: holding.ticker,
        assetType: holding.assetType,
        notes: "",
      });
      const nextItems = [createdItem, ...watchlistItems];
      writeWatchlistCache(id, nextItems);
      setWatchlistItems(nextItems);
      navigate(`/users/${id}/watchlist/${createdItem.id}`);
    } catch (openError) {
      setError(openError instanceof Error ? openError.message : "Failed to open holding detail");
    } finally {
      setOpeningTicker(null);
    }
  }

  return (
    <section className={`page-layout content-fade ${contentPhase === "updating" ? "is-updating" : ""}`}>
      <header className="page-header">
        <div>
          <p className="eyebrow">User Portfolio</p>
          <h2>{portfolio.name}</h2>
          <p className="subtle-text">User ID {portfolio.id} · Holdings and cash are live from the backend</p>
        </div>
        <Link className="primary-button" to={`/users/${id}?modal=transaction`}>
          New Transaction
        </Link>
      </header>

      {error ? <div className="form-error">{error}</div> : null}

      <section className="stats-grid" aria-label="Portfolio summary">
        <article className="stat-card">
          <span>Available Cash</span>
          <strong>{formatCurrency(portfolio.availableCash)}</strong>
        </article>
        <article className="stat-card">
          <span>{portfolio.valuationMode === "live" ? "Portfolio Value" : "Tracked Assets"}</span>
          <strong>{formatCurrency(portfolio.trackedAssetValue)}</strong>
          <p className="card-footnote">
            {portfolio.valuationMode === "live"
              ? `Live holdings valuation is connected via WebSocket. Status: ${liveStatus}.`
              : "Cost basis only. Market price feed pending."}
          </p>
        </article>
        <article className="stat-card">
          <span>Number of Holdings</span>
          <strong>{portfolio.holdingsCount}</strong>
          <p className="card-footnote">
            {portfolio.valuationMode === "live"
              ? "Market value and P/L columns are currently driven by the live holdings channel."
              : "Real-time market value will be added after broker API integration."}
          </p>
        </article>
      </section>

      <section className="dashboard-grid">
        <article className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Allocation</p>
              <h3>Asset allocation</h3>
              <p className="subtle-text">Grouped by cash and holding cost basis</p>
            </div>
          </div>
          <AllocationChart allocation={portfolio.allocation} />
        </article>

        <article className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Holdings</p>
              <h3>Current positions</h3>
              <p className="subtle-text">Live status: {liveStatus === "live" ? "Connected" : "Offline"}</p>
            </div>
            <Link className="text-link" to={`/users/${id}/transactions`}>
              View transactions
            </Link>
          </div>
          {portfolio.holdings.length === 0 ? (
            <div className="empty-state">
              <h4>No holdings yet</h4>
              <p>Record your first buy transaction to populate this table.</p>
            </div>
          ) : (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Ticker</th>
                    <th>Asset Type</th>
                    <th>Quantity</th>
                    <th>Average Cost</th>
                    <th>Position Cost</th>
                    <th>Market Value</th>
                    <th>P/L</th>
                  </tr>
                </thead>
                <tbody>
                  {portfolio.holdings.map((holding) => {
                    const watchlistItem = watchlistByTicker.get(`${holding.ticker.trim().toUpperCase()}-${holding.assetType}`);
                    const isOpening = openingTicker === `${holding.ticker.trim().toUpperCase()}-${holding.assetType}`;

                    return (
                    <tr key={holding.id}>
                      <td>
                        <div className="holding-ticker-cell">
                          <span>{holding.ticker}</span>
                          {watchlistItem ? (
                            <Link
                              className="holding-detail-link"
                              to={`/users/${id}/watchlist/${watchlistItem.id}`}
                              aria-label={`Open ${holding.ticker} detail page`}
                              title={`Open ${holding.ticker} detail page`}
                            >
                              <span aria-hidden="true">📈</span>
                            </Link>
                          ) : (
                            <button
                              className="holding-detail-link"
                              type="button"
                              onClick={() => {
                                void handleOpenHoldingDetail(holding);
                              }}
                              aria-label={`Create detail page for ${holding.ticker}`}
                              title={`Create detail page for ${holding.ticker}`}
                              disabled={isOpening}
                            >
                              <span aria-hidden="true">{isOpening ? "…" : "📈"}</span>
                            </button>
                          )}
                        </div>
                      </td>
                      <td>{holding.assetType}</td>
                      <td>{holding.quantity}</td>
                      <td>{formatCurrency(holding.averageCost)}</td>
                      <td>{formatCurrency(holding.positionCost)}</td>
                      <td>{holding.marketValue !== null && holding.marketValue !== undefined ? formatCurrency(holding.marketValue) : "—"}</td>
                      <td
                        className={
                          holding.pl !== null && holding.pl !== undefined
                            ? holding.pl >= 0
                              ? "profit-value"
                              : "loss-value"
                            : undefined
                        }
                        >
                        {holding.pl !== null && holding.pl !== undefined ? formatCurrency(holding.pl) : "—"}
                      </td>
                    </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </article>
      </section>

      <div className="dashboard-footer-link">
        <Link className="text-link" to={`/users/${id}/watchlist`}>
          Open watchlist
        </Link>
      </div>
    </section>
  );
}
