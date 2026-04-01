import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { TradingViewMiniChart } from "../components/TradingViewMiniChart";
import { WatchlistItemModal } from "../components/WatchlistItemModal";
import {
  createWatchlistItem,
  deleteWatchlistItem,
  getUserWatchlist,
  updateWatchlistItem,
} from "../lib/api";
import { formatBackendDateTime } from "../lib/format";
import { buildTradingViewSymbol } from "../lib/tradingview";
import { readWatchlistCache, writeWatchlistCache } from "../lib/watchlist-cache";
import type { WatchlistItem, WatchlistItemRequest } from "../lib/types";

function sortWatchlist(items: WatchlistItem[]) {
  return [...items].sort((left, right) => right.updatedAt.localeCompare(left.updatedAt));
}

export function UserWatchlistPage() {
  const { id = "1" } = useParams();
  const [items, setItems] = useState<WatchlistItem[]>(() => readWatchlistCache(id) ?? []);
  const [error, setError] = useState<string | null>(null);
  const [contentPhase, setContentPhase] = useState<"idle" | "updating">("idle");
  const [modalMode, setModalMode] = useState<"create" | "edit" | null>(null);
  const [selectedItem, setSelectedItem] = useState<WatchlistItem | undefined>(undefined);
  const [modalError, setModalError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  useEffect(() => {
    setItems(readWatchlistCache(id) ?? []);
    setError(null);
    setContentPhase("idle");
  }, [id]);

  useEffect(() => {
    const cachedItems = readWatchlistCache(id);
    const controller = new AbortController();

    async function loadWatchlist() {
      setError(null);
      setContentPhase(cachedItems ? "updating" : "idle");

      try {
        const result = sortWatchlist(await getUserWatchlist(id, controller.signal));
        writeWatchlistCache(id, result);
        setItems(result);
        setContentPhase("idle");
      } catch (loadError) {
        if (!controller.signal.aborted) {
          setError(loadError instanceof Error ? loadError.message : "Failed to load watchlist");
          setContentPhase("idle");
        }
      }
    }

    void loadWatchlist();

    return () => {
      controller.abort();
    };
  }, [id]);

  const isModalOpen = modalMode !== null;
  const pageDescription = useMemo(() => "Track assets you want to watch and open a dedicated detail page to trade.", []);

  function openCreateModal() {
    setSelectedItem(undefined);
    setModalError(null);
    setModalMode("create");
  }

  function openEditModal(item: WatchlistItem) {
    setSelectedItem(item);
    setModalError(null);
    setModalMode("edit");
  }

  function closeModal() {
    setModalMode(null);
    setSelectedItem(undefined);
    setModalError(null);
  }

  async function handleSave(payload: WatchlistItemRequest) {
    try {
      setIsSubmitting(true);
      setModalError(null);

      if (modalMode === "create") {
        const createdItem = await createWatchlistItem(id, payload);
        const nextItems = sortWatchlist([createdItem, ...items]);
        writeWatchlistCache(id, nextItems);
        setItems(nextItems);
      } else if (modalMode === "edit" && selectedItem) {
        const updatedItem = await updateWatchlistItem(id, selectedItem.id, payload);
        const nextItems = sortWatchlist(items.map((item) => (item.id === updatedItem.id ? updatedItem : item)));
        writeWatchlistCache(id, nextItems);
        setItems(nextItems);
      }

      closeModal();
    } catch (saveError) {
      setModalError(saveError instanceof Error ? saveError.message : "Failed to save watchlist item");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete(item: WatchlistItem) {
    const shouldDelete = window.confirm(`Remove ${item.ticker} from the watchlist?`);

    if (!shouldDelete) {
      return;
    }

    try {
      setDeletingId(item.id);
      await deleteWatchlistItem(id, item.id);
      const nextItems = items.filter((entry) => entry.id !== item.id);
      writeWatchlistCache(id, nextItems);
      setItems(nextItems);
    } catch (deleteError) {
      setError(deleteError instanceof Error ? deleteError.message : "Failed to delete watchlist item");
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <section className={`page-layout content-fade ${contentPhase === "updating" ? "is-updating" : ""}`}>
      <header className="page-header">
        <div>
          <p className="eyebrow">Research</p>
          <h2>Watchlist</h2>
          <p className="subtle-text">{pageDescription}</p>
        </div>
        <button className="primary-button" type="button" onClick={openCreateModal}>
          Add Item
        </button>
      </header>

      <article className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Overview</p>
            <h3>Tracked assets</h3>
          </div>
          <Link className="text-link" to={`/users/${id}`}>
            Back to dashboard
          </Link>
        </div>

        {error ? <div className="form-error">{error}</div> : null}

        {items.length === 0 ? (
          <div className="empty-state">
            <h4>No watchlist items yet</h4>
            <p>Add a ticker you want to monitor later.</p>
          </div>
        ) : (
          <div className="watchlist-grid">
            {items.map((item) => (
              <article key={item.id} className="watchlist-card">
                <div className="watchlist-card-header">
                  <div className="watchlist-identity">
                    <p className="watchlist-ticker">{item.ticker}</p>
                    <span className="watchlist-tag">{item.assetType}</span>
                  </div>
                  <div className="watchlist-actions">
                    <button className="ghost-button" type="button" onClick={() => openEditModal(item)}>
                      Edit
                    </button>
                    <button
                      className="ghost-button danger-button"
                      type="button"
                      onClick={() => handleDelete(item)}
                      disabled={deletingId === item.id}
                    >
                      {deletingId === item.id ? "Removing..." : "Remove"}
                    </button>
                  </div>
                </div>

                <p className="watchlist-notes">{item.notes || "—"}</p>

                <TradingViewMiniChart symbol={buildTradingViewSymbol(item)} title={item.ticker} />

                <div className="watchlist-card-footer">
                  <p className="watchlist-updated">Updated {formatBackendDateTime(item.updatedAt)}</p>
                  <Link className="primary-button watchlist-detail-button" to={`/users/${id}/watchlist/${item.id}`}>
                    View Details
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </article>

      {isModalOpen ? (
        <WatchlistItemModal
          mode={modalMode}
          item={selectedItem}
          isSubmitting={isSubmitting}
          error={modalError}
          onClose={closeModal}
          onSubmit={handleSave}
        />
      ) : null}
    </section>
  );
}
