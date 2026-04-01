import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { TradingViewAdvancedChart } from "../components/TradingViewAdvancedChart";
import { createTradeTransaction, getMarketPrice, getUserHoldings, getUserWatchlist } from "../lib/api";
import { formatBackendDateTime, formatCurrency } from "../lib/format";
import { buildTradingViewSymbol } from "../lib/tradingview";
import { readWatchlistCache, writeWatchlistCache } from "../lib/watchlist-cache";
import type { Holding, WatchlistItem } from "../lib/types";

function getTradeButtonClass(currentMode: "BUY" | "SELL", buttonMode: "BUY" | "SELL") {
  if (currentMode !== buttonMode) {
    return "ghost-button";
  }

  return buttonMode === "BUY" ? "transaction-action-button positive" : "transaction-action-button negative";
}

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
  const [marketPrice, setMarketPrice] = useState<number | null>(null);
  const [priceError, setPriceError] = useState<string | null>(null);
  const [holdings, setHoldings] = useState<Holding[]>([]);
  const [tradeMode, setTradeMode] = useState<"BUY" | "SELL">("BUY");
  const [tradeQuantity, setTradeQuantity] = useState("");
  const [tradeError, setTradeError] = useState<string | null>(null);
  const [tradeMessage, setTradeMessage] = useState<string | null>(null);
  const [isSubmittingTrade, setIsSubmittingTrade] = useState(false);

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

  useEffect(() => {
    const controller = new AbortController();

    async function loadHoldings() {
      try {
        const nextHoldings = await getUserHoldings(id, controller.signal);
        setHoldings(nextHoldings);
      } catch {
        // Holdings are a helpful enhancement here; the trade API still validates on the backend.
      }
    }

    void loadHoldings();

    return () => {
      controller.abort();
    };
  }, [id]);

  useEffect(() => {
    if (!item) {
      return;
    }

    const controller = new AbortController();
    const ticker = item.ticker;

    async function loadMarketPrice() {
      try {
        setPriceError(null);
        const nextPrice = await getMarketPrice(id, ticker, controller.signal);
        setMarketPrice(nextPrice);
      } catch (loadError) {
        if (!controller.signal.aborted) {
          setPriceError(loadError instanceof Error ? loadError.message : "Failed to load market price");
        }
      }
    }

    void loadMarketPrice();
    const intervalId = window.setInterval(() => {
      void loadMarketPrice();
    }, 5000);

    return () => {
      controller.abort();
      window.clearInterval(intervalId);
    };
  }, [id, item]);

  const symbol = useMemo(() => (item ? buildTradingViewSymbol(item) : null), [item]);
  const currentHolding = useMemo(
    () => (item ? holdings.find((holding) => holding.ticker.toUpperCase() === item.ticker.toUpperCase()) ?? null : null),
    [holdings, item],
  );

  async function handleTradeSubmit() {
    if (!item) {
      return;
    }

    const parsedQuantity = Number(tradeQuantity);
    setTradeError(null);
    setTradeMessage(null);

    try {
      if (marketPrice === null) {
        throw new Error("Market price is not available yet");
      }

      if (Number.isNaN(parsedQuantity) || parsedQuantity <= 0) {
        throw new Error("Quantity must be greater than 0");
      }

      if (tradeMode === "SELL") {
        if (!currentHolding) {
          throw new Error("This ticker is not currently held and cannot be sold");
        }

        if (parsedQuantity > Number(currentHolding.quantity)) {
          throw new Error(`Sell quantity exceeds current holding (${currentHolding.quantity})`);
        }
      }

      setIsSubmittingTrade(true);

      await createTradeTransaction(id, {
        ticker: item.ticker,
        assetType: item.assetType,
        transactionType: tradeMode,
        quantity: parsedQuantity,
        price: marketPrice,
      });

      setTradeMessage(`${tradeMode === "BUY" ? "Buy" : "Sell"} order submitted.`);
      setTradeQuantity("");
      const nextHoldings = await getUserHoldings(id);
      setHoldings(nextHoldings);
      window.dispatchEvent(new Event("portfolio:refresh"));
      window.dispatchEvent(new CustomEvent("transactions:refresh", { detail: { userId: id } }));
    } catch (submitError) {
      setTradeError(submitError instanceof Error ? submitError.message : "Failed to submit trade");
    } finally {
      setIsSubmittingTrade(false);
    }
  }

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
            {item.assetType} · Market price refreshes every 5 seconds and the trade form uses the latest fetched price.
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
            <dt>Market Price</dt>
            <dd>{marketPrice !== null ? formatCurrency(marketPrice) : "—"}</dd>
          </div>
          <div>
            <dt>Holding Qty</dt>
            <dd>{currentHolding ? currentHolding.quantity : "—"}</dd>
          </div>
          <div>
            <dt>Refresh</dt>
            <dd>Every 5s</dd>
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

      <article className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Trade</p>
            <h3>Buy or sell {item.ticker}</h3>
            <p className="subtle-text">Ticker, asset type, and price are prefilled from the current watchlist context.</p>
          </div>
        </div>

        <div className="target-actions">
          <button
            className={getTradeButtonClass(tradeMode, "BUY")}
            type="button"
            onClick={() => {
              setTradeMode("BUY");
              setTradeError(null);
              setTradeMessage(null);
            }}
          >
            Buy
          </button>
          <button
            className={getTradeButtonClass(tradeMode, "SELL")}
            type="button"
            onClick={() => {
              setTradeMode("SELL");
              setTradeError(null);
              setTradeMessage(null);
            }}
          >
            Sell
          </button>
        </div>

        <div className="modal-grid">
          <div className="summary-tile">
            <span>Ticker</span>
            <strong>{item.ticker}</strong>
            <p className="summary-detail">Asset type: {item.assetType}</p>
          </div>
          <div className="summary-tile">
            <span>Execution Price</span>
            <strong>{marketPrice !== null ? formatCurrency(marketPrice) : "Loading..."}</strong>
            <p className="summary-detail">Current market price fetched from the backend.</p>
          </div>
          <label className="field-group modal-grid-span-2">
            <span className="field-label">Quantity</span>
            <input
              className="text-input"
              placeholder="10"
              type="number"
              min="0"
              step="0.0001"
              value={tradeQuantity}
              onChange={(event) => {
                setTradeError(null);
                setTradeMessage(null);
                setTradeQuantity(event.target.value);
              }}
              disabled={isSubmittingTrade}
            />
          </label>
        </div>

        {priceError ? <div className="form-error">{priceError}</div> : null}
        {tradeError ? <div className="form-error">{tradeError}</div> : null}
        {tradeMessage ? <div className="form-success">{tradeMessage}</div> : null}

        <div className="modal-footer">
          <p className="helper-text">
            {tradeMode === "SELL"
              ? currentHolding
                ? `Current holding: ${currentHolding.quantity} ${item.ticker}`
                : "This ticker is not currently held, so sell is unavailable."
              : "Buy orders use the latest fetched market price and only require quantity."}
          </p>
          <button
            className={`transaction-action-button ${tradeMode === "BUY" ? "positive" : "negative"}`}
            type="button"
            onClick={handleTradeSubmit}
            disabled={
              isSubmittingTrade ||
              marketPrice === null ||
              (tradeMode === "SELL" && !currentHolding)
            }
          >
            {isSubmittingTrade ? "Submitting..." : tradeMode === "BUY" ? "Confirm Buy" : "Confirm Sell"}
          </button>
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
