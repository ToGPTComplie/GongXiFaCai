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

  useEffect(() => {
    setTicker(item?.ticker ?? "");
    setAssetType(item?.assetType ?? "STOCK");
    setNotes(item?.notes ?? "");
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

            <div className="summary-tile">
              <span>{isEditMode ? "Immutable fields" : "Tracked asset"}</span>
              <strong>{ticker.trim() ? ticker.trim().toUpperCase() : "New item"}</strong>
              <p className="summary-detail">
                {isEditMode
                  ? "Ticker and asset type stay read-only in edit mode, but are still submitted to satisfy the backend contract."
                  : "Create a lightweight research card with notes and a direct path to market details."}
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
