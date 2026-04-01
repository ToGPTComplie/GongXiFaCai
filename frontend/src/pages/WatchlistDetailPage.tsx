import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { TradingViewAdvancedChart } from "../components/TradingViewAdvancedChart";
import { getUserWatchlist } from "../lib/api";
import { formatBackendDateTime, formatCurrency } from "../lib/format";
import { buildTradingViewSymbol } from "../lib/tradingview";
import { readWatchlistCache, writeWatchlistCache } from "../lib/watchlist-cache";
import type { WatchlistItem } from "../lib/types";

function sortWatchlist(items: WatchlistItem[]) {
  return [...items].sort((left, right) => right.updatedAt.localeCompare(left.updatedAt));
}

export function WatchlistDetailPage() {
  const { id = "1", watchlistItemId = "" } = useParams();
  const parsedItemId = Number(watchlistItemId);
  const [item, setItem] = useState<WatchlistItem | null>(() => {
    const cachedItems = readWatchlistCache(id);
    return cachedItems?.find((entry) => entry.id === parsedItemId) ?? null;
  });
  const [error, setError] = useState<string | null>(null);
  const [contentPhase, setContentPhase] = useState<"idle" | "updating">("idle");

  useEffect(() => {
    const cachedItems = readWatchlistCache(id);
    const cachedItem = cachedItems?.find((entry) => entry.id === parsedItemId) ?? null;
    const controller = new AbortController();

    setItem(cachedItem);
    setError(null);

    async function loadWatchlistItem() {
      if (!Number.isInteger(parsedItemId) || parsedItemId <= 0) {
        setError("Invalid watchlist item");
        return;
      }

      setContentPhase(cachedItem ? "updating" : "idle");

      try {
        const result = sortWatchlist(await getUserWatchlist(id, controller.signal));
        writeWatchlistCache(id, result);
        const matchedItem = result.find((entry) => entry.id === parsedItemId) ?? null;
        setItem(matchedItem);
        setContentPhase("idle");

        if (!matchedItem) {
          setError("Watchlist item not found");
        }
      } catch (loadError) {
        if (!controller.signal.aborted) {
          setError(loadError instanceof Error ? loadError.message : "Failed to load watchlist item");
          setContentPhase("idle");
        }
      }
    }

    void loadWatchlistItem();

    return () => {
      controller.abort();
    };
  }, [id, parsedItemId]);

  const symbol = useMemo(() => (item ? buildTradingViewSymbol(item) : null), [item]);

  if (error && item === null) {
    return (
      <section className="page-layout">
        <header className="page-header">
          <div>
            <p className="eyebrow">Watchlist</p>
            <h2>Asset detail</h2>
          </div>
          <Link className="text-link" to={`/users/${id}/watchlist`}>
            Back to watchlist
          </Link>
        </header>
        <div className="page-state">{error}</div>
      </section>
    );
  }

  if (item === null) {
    return (
      <section className="page-layout">
        <header className="page-header">
          <div>
            <p className="eyebrow">Watchlist</p>
            <h2>Asset detail</h2>
          </div>
          <Link className="text-link" to={`/users/${id}/watchlist`}>
            Back to watchlist
          </Link>
        </header>
      </section>
    );
  }

  return (
    <section className={`page-layout content-fade ${contentPhase === "updating" ? "is-updating" : ""}`}>
      <header className="page-header">
        <div>
          <p className="eyebrow">Watchlist</p>
          <h2>{item.ticker}</h2>
          <p className="subtle-text">
            {item.assetType} · Advanced chart uses a temporary symbol mapping until exchange metadata is added.
          </p>
        </div>
        <Link className="text-link" to={`/users/${id}/watchlist`}>
          Back to watchlist
        </Link>
      </header>

      {error ? <div className="form-error">{error}</div> : null}

      <article className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Summary</p>
            <h3>Watchlist context</h3>
          </div>
        </div>

        <dl className="watchlist-detail-summary">
          <div>
            <dt>Asset Type</dt>
            <dd>{item.assetType}</dd>
          </div>
          <div>
            <dt>Target Buy</dt>
            <dd>{item.targetBuyPrice !== null ? formatCurrency(item.targetBuyPrice) : "—"}</dd>
          </div>
          <div>
            <dt>Alert Low</dt>
            <dd>{item.alertPriceLow !== null ? formatCurrency(item.alertPriceLow) : "—"}</dd>
          </div>
          <div>
            <dt>Alert High</dt>
            <dd>{item.alertPriceHigh !== null ? formatCurrency(item.alertPriceHigh) : "—"}</dd>
          </div>
          <div>
            <dt>Updated</dt>
            <dd>{formatBackendDateTime(item.updatedAt)}</dd>
          </div>
        </dl>

        <div className="watchlist-detail-notes">
          <p className="eyebrow">Notes</p>
          <p>{item.notes || "No notes yet."}</p>
        </div>
      </article>

      {symbol ? (
        <TradingViewAdvancedChart symbol={symbol} title={`${item.ticker} advanced chart`} />
      ) : (
        <article className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Market View</p>
              <h3>Advanced chart</h3>
            </div>
          </div>
          <div className="empty-state">
            <h4>Chart unavailable</h4>
            <p>Advanced chart is not available for this asset type yet.</p>
          </div>
        </article>
      )}
    </section>
  );
}
