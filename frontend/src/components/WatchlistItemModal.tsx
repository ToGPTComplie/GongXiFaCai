import { FormEvent, useEffect, useMemo, useState } from "react";
import { findNasdaqSecurityBySymbol, inferAssetTypeFromSecurityName, searchNasdaqSecurities } from "../lib/nasdaq";
import type { WatchlistItem, WatchlistItemRequest } from "../lib/types";
interface WatchlistItemModalProps {
  mode: "create" | "edit";
  item?: WatchlistItem;
  isSubmitting: boolean;
  error: string | null;
  onClose: () => void;
  onSubmit: (payload: WatchlistItemRequest) => Promise<void>;
}

export function WatchlistItemModal({
  mode,
  item,
  isSubmitting,
  error,
  onClose,
  onSubmit,
}: WatchlistItemModalProps) {
  const [ticker, setTicker] = useState(item?.ticker ?? "");
  const [assetType, setAssetType] = useState<"STOCK" | "BOND">(item?.assetType ?? "STOCK");
  const [notes, setNotes] = useState(item?.notes ?? "");
  const [localError, setLocalError] = useState<string | null>(null);
  const [selectedSymbol, setSelectedSymbol] = useState(item?.ticker.trim().toUpperCase() ?? "");

  useEffect(() => {
    setTicker(item?.ticker ?? "");
    setAssetType(item?.assetType ?? "STOCK");
    setNotes(item?.notes ?? "");
    setLocalError(null);
    setSelectedSymbol(item?.ticker.trim().toUpperCase() ?? "");
  }, [item, mode]);

  const title = useMemo(() => (mode === "create" ? "Add watchlist item" : "Edit watchlist item"), [mode]);
  const isEditMode = mode === "edit";
  const suggestions = useMemo(() => (isEditMode ? [] : searchNasdaqSecurities(ticker)), [isEditMode, ticker]);
  const selectedSecurity = useMemo(() => {
    if (selectedSymbol) {
      return findNasdaqSecurityBySymbol(selectedSymbol);
    }

    if (isEditMode && item?.ticker) {
      return findNasdaqSecurityBySymbol(item.ticker);
    }

    return null;
  }, [isEditMode, item?.ticker, selectedSymbol]);

  function handleTickerInput(nextValue: string) {
    setTicker(nextValue);
    setLocalError(null);

    const normalized = nextValue.trim().toUpperCase();
    const exactMatch = findNasdaqSecurityBySymbol(normalized);

    if (exactMatch) {
      setSelectedSymbol(exactMatch.symbol);
      setAssetType(inferAssetTypeFromSecurityName(exactMatch.name));
      return;
    }

    setSelectedSymbol("");
  }

  function handleSelectSecurity(symbol: string, name: string) {
    setTicker(symbol);
    setSelectedSymbol(symbol);
    setAssetType(inferAssetTypeFromSecurityName(name));
    setLocalError(null);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLocalError(null);

    try {
      const normalizedTicker = ticker.trim().toUpperCase();

      if (!normalizedTicker) {
        throw new Error("Ticker is required");
      }

      const matchedSecurity = findNasdaqSecurityBySymbol(normalizedTicker);

      if (!matchedSecurity || selectedSymbol !== matchedSecurity.symbol) {
        throw new Error("Please choose a valid ticker from the matching list.");
      }

      await onSubmit({
        ticker: normalizedTicker,
        assetType: inferAssetTypeFromSecurityName(matchedSecurity.name),
        notes: notes.trim() || undefined,
      });
    } catch (submitError) {
      setLocalError(submitError instanceof Error ? submitError.message : "Failed to save watchlist item");
    }
  }

  const displayError = localError ?? error;

  return (
    <div className="modal-backdrop" role="presentation">
      <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="watchlist-title">
        <div className="modal-header">
          <div>
            <p className="eyebrow">Watchlist</p>
            <h2 id="watchlist-title">{title}</h2>
          </div>
          <button className="ghost-button" type="button" onClick={onClose}>
            Close
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-grid">
            <label className="field-group">
              <span className="field-label">Ticker</span>
              <input
                placeholder="AAPL"
                value={ticker}
                onChange={(event) => handleTickerInput(event.target.value)}
                disabled={isSubmitting || isEditMode}
                autoComplete="off"
              />
              {!isEditMode && suggestions.length > 0 ? (
                <div className="ticker-suggestion-list" role="listbox" aria-label="Matching tickers">
                  {suggestions.map((security) => (
                    <button
                      key={security.symbol}
                      className={`ticker-suggestion-item ${
                        security.symbol === selectedSymbol ? "is-selected" : ""
                      }`}
                      type="button"
                      onClick={() => handleSelectSecurity(security.symbol, security.name)}
                    >
                      <img
                        className="ticker-suggestion-icon"
                        src={security.logoUrl}
                        alt=""
                        loading="lazy"
                        referrerPolicy="no-referrer"
                      />
                      <span className="ticker-suggestion-symbol">{security.symbol}</span>
                      <span className="ticker-suggestion-name">{security.name}</span>
                    </button>
                  ))}
                </div>
              ) : null}
            </label>

            <div className="summary-tile">
              <span>Asset type</span>
              <strong>{assetType === "BOND" ? "Bond" : "Stock"}</strong>
              <p className="summary-detail">
                {selectedSecurity
                  ? `${selectedSecurity.name} automatically maps to ${assetType === "BOND" ? "Bond" : "Stock"}.`
                  : "Asset type is inferred from the selected market entry."}
              </p>
            </div>

            <label className="field-group modal-grid-span-2">
              <span className="field-label">Notes</span>
              <textarea
                placeholder="Why are you tracking this asset?"
                value={notes}
                onChange={(event) => setNotes(event.target.value)}
                disabled={isSubmitting}
                rows={4}
              />
            </label>

            <div className="summary-tile">
              <span>{isEditMode ? "Immutable fields" : "Tracked asset"}</span>
              <strong>{ticker.trim() ? ticker.trim().toUpperCase() : "New item"}</strong>
              <p className="summary-detail">
                {isEditMode
                  ? "Ticker and asset type stay read-only in edit mode, but are still submitted to satisfy the backend contract."
                  : selectedSecurity
                    ? selectedSecurity.name
                    : "Choose a valid ticker from the matching list to create a research card."}
              </p>
            </div>
          </div>

          {displayError ? <div className="form-error">{displayError}</div> : null}

          <div className="modal-footer">
            <p className="helper-text">Ticker and asset type define the market detail page and trade entry.</p>
            <button className="primary-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Saving..." : mode === "create" ? "Add Item" : "Save Changes"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
