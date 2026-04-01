import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getUserFundTransactions, getUserTradeTransactions } from "../lib/api";
import { formatBackendDateTime, formatCurrency } from "../lib/format";
import type { FundTransaction, PageResponse, TradeTransaction } from "../lib/types";

type TransactionTab = "trade" | "fund";

const pageSize = 10;
const defaultViewState = {
  activeTab: "trade" as TransactionTab,
  tradePage: 0,
  fundPage: 0,
};

type TransactionsCacheEntry = {
  trade: Record<number, PageResponse<TradeTransaction>>;
  fund: Record<number, PageResponse<FundTransaction>>;
};

const transactionsCache = new Map<string, TransactionsCacheEntry>();

function getTransactionsViewStorageKey(userId: string) {
  return `transactions:view:${userId}`;
}

function readStoredViewState(userId: string) {
  if (typeof window === "undefined") {
    return defaultViewState;
  }

  const rawValue = window.sessionStorage.getItem(getTransactionsViewStorageKey(userId));

  if (!rawValue) {
    return defaultViewState;
  }

  try {
    const parsed = JSON.parse(rawValue) as Partial<typeof defaultViewState>;
    return {
      activeTab: parsed.activeTab === "fund" ? "fund" : "trade",
      tradePage: Number.isInteger(parsed.tradePage) && parsed.tradePage! >= 0 ? parsed.tradePage! : 0,
      fundPage: Number.isInteger(parsed.fundPage) && parsed.fundPage! >= 0 ? parsed.fundPage! : 0,
    };
  } catch {
    return defaultViewState;
  }
}

function getCachedPage<T>(
  cache: Record<number, PageResponse<T>>,
  page: number,
): PageResponse<T> | null {
  return cache[page] ?? null;
}

function readCachedTransactions(userId: string): TransactionsCacheEntry {
  return (
    transactionsCache.get(userId) ?? {
      trade: {},
      fund: {},
    }
  );
}

export function UserTransactionsPage() {
  const { id = "1" } = useParams();
  const cachedTransactions = readCachedTransactions(id);
  const [viewState, setViewState] = useState(() => readStoredViewState(id));
  const [tradeTransactions, setTradeTransactions] = useState<Record<number, PageResponse<TradeTransaction>>>(
    () => cachedTransactions.trade,
  );
  const [fundTransactions, setFundTransactions] = useState<Record<number, PageResponse<FundTransaction>>>(
    () => cachedTransactions.fund,
  );
  const [error, setError] = useState<string | null>(null);
  const [contentPhase, setContentPhase] = useState<"idle" | "updating">("idle");
  const [refreshKey, setRefreshKey] = useState(0);
  const activeTab = viewState.activeTab;
  const page = activeTab === "trade" ? viewState.tradePage : viewState.fundPage;

  useEffect(() => {
    setViewState(readStoredViewState(id));
    const nextCachedTransactions = readCachedTransactions(id);
    setTradeTransactions(nextCachedTransactions.trade);
    setFundTransactions(nextCachedTransactions.fund);
    setError(null);
    setContentPhase("idle");
  }, [id]);

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }

    window.sessionStorage.setItem(getTransactionsViewStorageKey(id), JSON.stringify(viewState));
  }, [id, viewState]);

  useEffect(() => {
    function handleTransactionsRefresh(event: Event) {
      const refreshEvent = event as CustomEvent<{ userId?: string }>;

      if (refreshEvent.detail?.userId && refreshEvent.detail.userId !== id) {
        return;
      }

      transactionsCache.delete(id);
      setTradeTransactions({});
      setFundTransactions({});
      setError(null);
      setContentPhase("idle");
      setRefreshKey((current) => current + 1);
    }

    window.addEventListener("transactions:refresh", handleTransactionsRefresh as EventListener);

    return () => {
      window.removeEventListener("transactions:refresh", handleTransactionsRefresh as EventListener);
    };
  }, [id]);

  useEffect(() => {
    const controller = new AbortController();
    const hasCachedPage =
      activeTab === "trade"
        ? Boolean(getCachedPage(tradeTransactions, page))
        : Boolean(getCachedPage(fundTransactions, page));

    async function loadTransactions() {
      setError(null);
      setContentPhase(hasCachedPage ? "updating" : "idle");

      try {
        if (activeTab === "trade") {
          const result = await getUserTradeTransactions(id, { page, size: pageSize }, controller.signal);
          setTradeTransactions((current) => {
            const next = {
              ...current,
              [page]: result,
            };
            transactionsCache.set(id, {
              trade: next,
              fund: readCachedTransactions(id).fund,
            });
            return next;
          });
        } else {
          const result = await getUserFundTransactions(id, { page, size: pageSize }, controller.signal);
          setFundTransactions((current) => {
            const next = {
              ...current,
              [page]: result,
            };
            transactionsCache.set(id, {
              trade: readCachedTransactions(id).trade,
              fund: next,
            });
            return next;
          });
        }
        setContentPhase("idle");
      } catch (loadError) {
        if (!controller.signal.aborted) {
          setError(loadError instanceof Error ? loadError.message : "Failed to load transactions");
          setContentPhase("idle");
        }
      }
    }

    void loadTransactions();

    return () => {
      controller.abort();
    };
  }, [activeTab, id, page, refreshKey]);

  useEffect(() => {
    const controller = new AbortController();

    async function prefetchInactiveTab() {
      try {
        if (activeTab === "trade") {
          const targetPage = viewState.fundPage;

          if (!getCachedPage(fundTransactions, targetPage)) {
            const result = await getUserFundTransactions(id, { page: targetPage, size: pageSize }, controller.signal);
            setFundTransactions((current) => {
              const next = {
                ...current,
                [targetPage]: result,
              };
              transactionsCache.set(id, {
                trade: readCachedTransactions(id).trade,
                fund: next,
              });
              return next;
            });
          }
        } else {
          const targetPage = viewState.tradePage;

          if (!getCachedPage(tradeTransactions, targetPage)) {
            const result = await getUserTradeTransactions(id, { page: targetPage, size: pageSize }, controller.signal);
            setTradeTransactions((current) => {
              const next = {
                ...current,
                [targetPage]: result,
              };
              transactionsCache.set(id, {
                trade: next,
                fund: readCachedTransactions(id).fund,
              });
              return next;
            });
          }
        }
      } catch {
        // Silent prefetch failure keeps the current tab stable and can retry on demand.
      }
    }

    void prefetchInactiveTab();

    return () => {
      controller.abort();
    };
  }, [activeTab, fundTransactions, id, tradeTransactions, viewState.fundPage, viewState.tradePage]);

  const currentPageData =
    activeTab === "trade"
      ? getCachedPage(tradeTransactions, viewState.tradePage)
      : getCachedPage(fundTransactions, viewState.fundPage);
  const currentTradePage = activeTab === "trade" ? getCachedPage(tradeTransactions, viewState.tradePage) : null;
  const currentFundPage = activeTab === "fund" ? getCachedPage(fundTransactions, viewState.fundPage) : null;

  function handleTabChange(nextTab: TransactionTab) {
    setViewState((current) => ({
      ...current,
      activeTab: nextTab,
    }));
    setError(null);
  }

  function updateCurrentPage(nextPage: number) {
    setViewState((current) =>
      current.activeTab === "trade"
        ? {
            ...current,
            tradePage: nextPage,
          }
        : {
            ...current,
            fundPage: nextPage,
          },
    );
  }

  return (
    <section className="page-layout">
      <header className="page-header">
        <div>
          <p className="eyebrow">History</p>
          <h2>Transactions</h2>
        </div>
        <Link className="text-link" to={`/users/${id}`}>
          Back to dashboard
        </Link>
      </header>

      <article className="panel">
        <div className="panel-toolbar">
          <div
            className="segmented-control"
            role="tablist"
            aria-label="Transaction categories"
            data-active-tab={activeTab}
          >
            <button
              className={activeTab === "trade" ? "segmented-option active" : "segmented-option"}
              role="tab"
              aria-selected={activeTab === "trade"}
              type="button"
              onClick={() => handleTabChange("trade")}
            >
              Trade Transactions
            </button>
            <button
              className={activeTab === "fund" ? "segmented-option active" : "segmented-option"}
              role="tab"
              aria-selected={activeTab === "fund"}
              type="button"
              onClick={() => handleTabChange("fund")}
            >
              Fund Transactions
            </button>
          </div>
          {currentPageData ? (
            <p className="subtle-text">
              Page {currentPageData.page + 1} of {Math.max(currentPageData.totalPages, 1)} · {currentPageData.totalElements} records
            </p>
          ) : null}
        </div>
        {error ? (
          <div className="page-state">Failed to load transactions. {error}</div>
        ) : !currentPageData || currentPageData.content.length === 0 ? (
          <div className="empty-state">
            <h4>No transactions yet</h4>
            <p>
              {activeTab === "trade"
                ? "Trade transactions will appear here after you record a buy or sell."
                : "Fund transactions will appear here after you record a deposit or withdraw."}
            </p>
          </div>
        ) : (
          <>
            <div className={`table-shell content-fade ${contentPhase === "updating" ? "is-updating" : ""}`}>
              <div className="table-scroll">
              {activeTab === "trade" ? (
                <table>
                  <thead>
                    <tr>
                      <th>Time</th>
                      <th>Type</th>
                      <th>Ticker</th>
                      <th>Asset Type</th>
                      <th>Quantity</th>
                      <th>Price</th>
                      <th>Total Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentTradePage?.content.map((transaction) => (
                      <tr key={transaction.id}>
                        <td>{formatBackendDateTime(transaction.createdAt)}</td>
                        <td>{transaction.transactionType}</td>
                        <td>{transaction.ticker}</td>
                        <td>{transaction.assetType}</td>
                        <td>{transaction.quantity}</td>
                        <td>{formatCurrency(transaction.price)}</td>
                        <td>{formatCurrency(transaction.totalAmount)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Time</th>
                      <th>Type</th>
                      <th>Total Amount</th>
                      <th>Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentFundPage?.content.map((transaction) => (
                      <tr key={transaction.id}>
                        <td>{formatBackendDateTime(transaction.createdAt)}</td>
                        <td>{transaction.transactionType}</td>
                        <td>{formatCurrency(transaction.totalAmount)}</td>
                        <td>{transaction.description || "-"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
              </div>
            </div>
            <div className="pagination-bar">
              <button
                className="ghost-button pagination-button"
                type="button"
                onClick={() => updateCurrentPage(Math.max(page - 1, 0))}
                disabled={currentPageData.page === 0}
              >
                Previous
              </button>
              <span className="pagination-status">
                {currentPageData.page + 1} / {Math.max(currentPageData.totalPages, 1)}
              </span>
              <button
                className="ghost-button pagination-button"
                type="button"
                onClick={() =>
                  updateCurrentPage(
                    currentPageData.totalPages === 0 ? page : Math.min(page + 1, currentPageData.totalPages - 1),
                  )
                }
                disabled={currentPageData.totalPages === 0 || currentPageData.page >= currentPageData.totalPages - 1}
              >
                Next
              </button>
            </div>
          </>
        )}
      </article>
    </section>
  );
}
