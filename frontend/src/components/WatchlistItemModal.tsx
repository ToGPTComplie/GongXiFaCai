import { FormEvent, useEffect, useMemo, useState } from "react";
import type { WatchlistItem, WatchlistItemRequest } from "../lib/types";
import { CustomSelect } from "./CustomSelect";

interface WatchlistItemModalProps {
  mode: "create" | "edit";
  item?: WatchlistItem;
  isSubmitting: boolean;
  error: string | null;
  onClose: () => void;
  onSubmit: (payload: WatchlistItemRequest) => Promise<void>;
}

function parseOptionalPositiveNumber(value: string, label: string) {
  if (!value.trim()) {
    return undefined;
  }

  const parsed = Number(value);

  if (Number.isNaN(parsed) || parsed <= 0) {
    throw new Error(`${label} must be greater than 0`);
  }

  return parsed;
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
  const [targetBuyPrice, setTargetBuyPrice] = useState(item?.targetBuyPrice?.toString() ?? "");
  const [alertPriceLow, setAlertPriceLow] = useState(item?.alertPriceLow?.toString() ?? "");
  const [alertPriceHigh, setAlertPriceHigh] = useState(item?.alertPriceHigh?.toString() ?? "");
  const [localError, setLocalError] = useState<string | null>(null);

  useEffect(() => {
    setTicker(item?.ticker ?? "");
    setAssetType(item?.assetType ?? "STOCK");
    setNotes(item?.notes ?? "");
    setTargetBuyPrice(item?.targetBuyPrice?.toString() ?? "");
    setAlertPriceLow(item?.alertPriceLow?.toString() ?? "");
    setAlertPriceHigh(item?.alertPriceHigh?.toString() ?? "");
    setLocalError(null);
  }, [item, mode]);

  const title = useMemo(() => (mode === "create" ? "Add watchlist item" : "Edit watchlist item"), [mode]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLocalError(null);

    try {
      const normalizedTicker = ticker.trim().toUpperCase();

      if (!normalizedTicker) {
        throw new Error("Ticker is required");
      }

      await onSubmit({
        ticker: normalizedTicker,
        assetType,
        notes: notes.trim() || undefined,
        targetBuyPrice: parseOptionalPositiveNumber(targetBuyPrice, "Target buy price"),
        alertPriceLow: parseOptionalPositiveNumber(alertPriceLow, "Alert low price"),
        alertPriceHigh: parseOptionalPositiveNumber(alertPriceHigh, "Alert high price"),
      });
    } catch (submitError) {
      setLocalError(submitError instanceof Error ? submitError.message : "Failed to save watchlist item");
    }
  }

  const displayError = localError ?? error;
  const isEditMode = mode === "edit";

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
                onChange={(event) => setTicker(event.target.value)}
                disabled={isSubmitting || isEditMode}
              />
            </label>

            <CustomSelect
              label="Asset type"
              value={assetType}
              disabled={isSubmitting || isEditMode}
              onChange={(nextValue) => setAssetType(nextValue as "STOCK" | "BOND")}
              options={[
                { value: "STOCK", label: "Stock" },
                { value: "BOND", label: "Bond" },
              ]}
            />

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

            <label className="field-group">
              <span className="field-label">Target Buy Price</span>
              <input
                placeholder="150.00"
                type="number"
                min="0"
                step="0.01"
                value={targetBuyPrice}
                onChange={(event) => setTargetBuyPrice(event.target.value)}
                disabled={isSubmitting}
              />
            </label>

            <label className="field-group">
              <span className="field-label">Alert Low</span>
              <input
                placeholder="130.00"
                type="number"
                min="0"
                step="0.01"
                value={alertPriceLow}
                onChange={(event) => setAlertPriceLow(event.target.value)}
                disabled={isSubmitting}
              />
            </label>

            <label className="field-group">
              <span className="field-label">Alert High</span>
              <input
                placeholder="175.00"
                type="number"
                min="0"
                step="0.01"
                value={alertPriceHigh}
                onChange={(event) => setAlertPriceHigh(event.target.value)}
                disabled={isSubmitting}
              />
            </label>

            <div className="summary-tile">
              <span>{isEditMode ? "Immutable fields" : "Tracked asset"}</span>
              <strong>{ticker.trim() ? ticker.trim().toUpperCase() : "New item"}</strong>
              <p className="summary-detail">
                {isEditMode
                  ? "Ticker and asset type stay read-only in edit mode, but are still submitted to satisfy the backend contract."
                  : "Create a lightweight research card with optional notes and price thresholds."}
              </p>
            </div>
          </div>

          {displayError ? <div className="form-error">{displayError}</div> : null}

          <div className="modal-footer">
            <p className="helper-text">Price fields are optional until live market data is integrated.</p>
            <button className="primary-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Saving..." : mode === "create" ? "Add Item" : "Save Changes"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
