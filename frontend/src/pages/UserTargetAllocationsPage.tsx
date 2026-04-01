import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { CustomSelect } from "../components/CustomSelect";
import {
  executeRebalance,
  getTargetAllocations,
  getUserHoldings,
  previewRebalance,
  saveTargetAllocations,
} from "../lib/api";
import { formatCurrency } from "../lib/format";
import type {
  Holding,
  RebalancePreviewPlan,
  TargetAllocation,
  TargetAllocationRequest,
  TargetAllocationRow,
} from "../lib/types";

const ASSET_TYPE_OPTIONS = [
  { label: "Stock", value: "STOCK" },
  { label: "Bond", value: "BOND" },
] as const;

function buildInitialRows(holdings: Holding[]): TargetAllocationRow[] {
  const seenTickers = new Set<string>();

  return holdings
    .filter((holding) => {
      const ticker = holding.ticker.trim().toUpperCase();
      if (seenTickers.has(ticker)) {
        return false;
      }

      seenTickers.add(ticker);
      return true;
    })
    .map((holding, index) => ({
      id: `holding-${holding.id}-${index}`,
      ticker: holding.ticker,
      assetType: holding.assetType,
      targetPercentInput: "",
    }));
}

function buildRowsFromTargets(targets: TargetAllocation[], holdings: Holding[]): TargetAllocationRow[] {
  const holdingAssetTypeMap = new Map(holdings.map((holding) => [normalizeTicker(holding.ticker), holding.assetType]));

  return targets.map((target, index) => ({
    id: `target-${index}-${normalizeTicker(target.ticker)}`,
    ticker: target.ticker,
    assetType: target.assetType ?? holdingAssetTypeMap.get(normalizeTicker(target.ticker)) ?? "STOCK",
    targetPercentInput: String(Number((target.targetPercentage * 100).toFixed(2))),
  }));
}

function createEmptyRow(index: number): TargetAllocationRow {
  return {
    id: `manual-${index}-${Math.random().toString(36).slice(2, 8)}`,
    ticker: "",
    assetType: "STOCK",
    targetPercentInput: "",
  };
}

function normalizeTicker(value: string) {
  return value.trim().toUpperCase();
}

function parsePercentInput(value: string) {
  if (value.trim() === "") {
    return null;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : Number.NaN;
}

function getTransactionTone(transactionType: "BUY" | "SELL") {
  return transactionType === "BUY" ? "positive" : "negative";
}

export function UserTargetAllocationsPage() {
  const { id = "1" } = useParams();
  const [rows, setRows] = useState<TargetAllocationRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saveMessage, setSaveMessage] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [previewPlans, setPreviewPlans] = useState<RebalancePreviewPlan[] | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [isPreviewing, setIsPreviewing] = useState(false);
  const [isExecuting, setIsExecuting] = useState(false);

  useEffect(() => {
    const controller = new AbortController();

    async function loadTargetPage() {
      setError(null);
      setSaveError(null);
      setSaveMessage(null);
      setPreviewError(null);

      try {
        const [holdings, savedTargets] = await Promise.all([
          getUserHoldings(id, controller.signal),
          getTargetAllocations(id, controller.signal).catch(() => []),
        ]);

        const initialRows = savedTargets.length > 0
          ? buildRowsFromTargets(savedTargets, holdings)
          : buildInitialRows(holdings);

        setRows(initialRows.length > 0 ? initialRows : [createEmptyRow(0)]);
      } catch (loadError) {
        if (!controller.signal.aborted) {
          setError(loadError instanceof Error ? loadError.message : "Failed to load holdings");
          setRows([createEmptyRow(0)]);
        }
      }
    }

    void loadTargetPage();

    return () => {
      controller.abort();
    };
  }, [id]);

  const totalAssigned = useMemo(
    () =>
      rows.reduce((sum, row) => {
        const parsed = parsePercentInput(row.targetPercentInput);
        return parsed !== null && Number.isFinite(parsed) && parsed > 0 ? sum + parsed : sum;
      }, 0),
    [rows],
  );

  const remainingCash = Math.max(0, 100 - totalAssigned);
  const isOverAllocated = totalAssigned > 100;

  const validationMessage = useMemo(() => {
    const normalizedTickers = rows
      .map((row) => normalizeTicker(row.ticker))
      .filter((ticker) => ticker.length > 0);

    if (new Set(normalizedTickers).size !== normalizedTickers.length) {
      return "Each ticker can only appear once in the target allocation list.";
    }

    for (const row of rows) {
      if (row.ticker.trim() === "" && row.targetPercentInput.trim() === "") {
        continue;
      }

      if (row.ticker.trim() === "") {
        return "Ticker is required for every allocation row.";
      }

      const parsed = parsePercentInput(row.targetPercentInput);
      if (parsed === null) {
        return "Target percentage is required for every allocation row.";
      }

      if (!Number.isFinite(parsed) || parsed < 0 || parsed > 100) {
        return "Target percentage must be a valid number between 0 and 100.";
      }
    }

    if (isOverAllocated) {
      return "Assigned allocation cannot exceed 100%.";
    }

    return null;
  }, [isOverAllocated, rows]);

  const canSave = validationMessage === null && rows.some((row) => row.ticker.trim() && row.targetPercentInput.trim());

  function updateRow(rowId: string, patch: Partial<TargetAllocationRow>) {
    setRows((current) => current.map((row) => (row.id === rowId ? { ...row, ...patch } : row)));
    setSaveError(null);
    setSaveMessage(null);
    setPreviewPlans(null);
    setPreviewError(null);
  }

  function addRow() {
    setRows((current) => [...current, createEmptyRow(current.length)]);
    setSaveError(null);
    setSaveMessage(null);
    setPreviewPlans(null);
    setPreviewError(null);
  }

  function removeRow(rowId: string) {
    setRows((current) => {
      const nextRows = current.filter((row) => row.id !== rowId);
      return nextRows.length > 0 ? nextRows : [createEmptyRow(0)];
    });
    setSaveError(null);
    setSaveMessage(null);
    setPreviewPlans(null);
    setPreviewError(null);
  }

  function buildPayload(): TargetAllocationRequest[] {
    return rows
      .filter((row) => row.ticker.trim() !== "" && row.targetPercentInput.trim() !== "")
      .map((row) => ({
        ticker: normalizeTicker(row.ticker),
        assetType: row.assetType,
        targetPercentage: Number((Number(row.targetPercentInput) / 100).toFixed(4)),
      }));
  }

  async function handleSave() {
    if (!canSave || validationMessage) {
      return;
    }

    try {
      setIsSaving(true);
      setSaveError(null);
      setSaveMessage(null);
      await saveTargetAllocations(id, buildPayload());
      setSaveMessage("Target allocations saved.");
    } catch (submitError) {
      setSaveError(submitError instanceof Error ? submitError.message : "Failed to save target allocations");
    } finally {
      setIsSaving(false);
    }
  }

  async function handlePreview() {
    if (!canSave || validationMessage) {
      return;
    }

    try {
      setIsPreviewing(true);
      setSaveError(null);
      setSaveMessage(null);
      setPreviewError(null);
      await saveTargetAllocations(id, buildPayload());
      const nextPreviewPlans = await previewRebalance(id);
      setPreviewPlans(nextPreviewPlans);
      setSaveMessage("Target allocations saved and preview refreshed.");
    } catch (previewLoadError) {
      setPreviewError(previewLoadError instanceof Error ? previewLoadError.message : "Failed to preview rebalance");
    } finally {
      setIsPreviewing(false);
    }
  }

  async function handleExecute() {
    try {
      setIsExecuting(true);
      setPreviewError(null);
      const executedPlans = await executeRebalance(id);
      setPreviewPlans(executedPlans);
      setSaveMessage("Rebalance executed.");
      window.dispatchEvent(new Event("portfolio:refresh"));
      window.dispatchEvent(new CustomEvent("transactions:refresh", { detail: { userId: id } }));
    } catch (executeError) {
      setPreviewError(executeError instanceof Error ? executeError.message : "Failed to execute rebalance");
    } finally {
      setIsExecuting(false);
    }
  }

  return (
    <section className="page-layout">
      <header className="page-header">
        <div>
          <p className="eyebrow">Portfolio Planning</p>
          <h2>Target Allocation</h2>
          <p className="subtle-text">
            Set desired portfolio weights for stock and bond tickers. Any remaining allocation stays in cash.
          </p>
        </div>
        <div className="target-actions">
          <button className="ghost-button" type="button" onClick={handlePreview} disabled={!canSave || isPreviewing || isSaving}>
            {isPreviewing ? "Previewing..." : "Preview Rebalance"}
          </button>
          <button className="primary-button" type="button" onClick={handleSave} disabled={!canSave || isSaving || isPreviewing}>
            {isSaving ? "Saving..." : "Save Targets"}
          </button>
        </div>
      </header>

      <section className="stats-grid" aria-label="Allocation summary">
        <article className="stat-card">
          <span>Assigned Allocation</span>
          <strong>{totalAssigned.toFixed(2)}%</strong>
        </article>
        <article className="stat-card">
          <span>Cash Buffer</span>
          <strong>{remainingCash.toFixed(2)}%</strong>
          <p className="card-footnote">The backend treats any unassigned percentage as cash.</p>
        </article>
        <article className="stat-card">
          <span>Target Rows</span>
          <strong>{rows.filter((row) => row.ticker.trim() !== "").length}</strong>
          <p className="card-footnote">Saving replaces the previous target allocation set for this user.</p>
        </article>
      </section>

      <article className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Editor</p>
            <h3>Desired weights</h3>
            <p className="subtle-text">
              Percentages are entered as 0-100 here and converted to 0-1 before submission.
            </p>
          </div>
          <Link className="text-link" to={`/users/${id}`}>
            Back to dashboard
          </Link>
        </div>

        {error ? <div className="form-error">{error}</div> : null}
        {validationMessage ? <div className="form-error">{validationMessage}</div> : null}
        {saveError ? <div className="form-error">{saveError}</div> : null}
        {saveMessage ? <div className="form-success">{saveMessage}</div> : null}

        <div className="target-allocation-list">
          {rows.map((row, index) => (
            <div key={row.id} className="target-allocation-row">
              <label className="field-group">
                <span className="field-label">Ticker</span>
                <input
                  className="text-input"
                  type="text"
                  value={row.ticker}
                  onChange={(event) => updateRow(row.id, { ticker: event.target.value })}
                  placeholder="AAPL"
                  autoCapitalize="characters"
                  spellCheck={false}
                />
              </label>

              <CustomSelect
                label="Asset Type"
                options={[...ASSET_TYPE_OPTIONS]}
                value={row.assetType}
                onChange={(value) => updateRow(row.id, { assetType: value as "STOCK" | "BOND" })}
              />

              <label className="field-group">
                <span className="field-label">Target %</span>
                <div className="percent-input-shell">
                  <input
                    className="text-input percent-input"
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    value={row.targetPercentInput}
                    onChange={(event) => updateRow(row.id, { targetPercentInput: event.target.value })}
                    placeholder="25"
                  />
                  <span className="percent-suffix">%</span>
                </div>
              </label>

              <button
                className="ghost-button target-row-remove"
                type="button"
                onClick={() => removeRow(row.id)}
                disabled={rows.length === 1 && index === 0}
              >
                Remove
              </button>
            </div>
          ))}
        </div>

        <div className="target-allocation-footer">
          <button className="ghost-button" type="button" onClick={addRow}>
            Add Row
          </button>
          <p className="helper-text">You can define future stock or bond positions even if they are not held yet.</p>
        </div>
      </article>

      <article className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Rebalance</p>
            <h3>Preview</h3>
            <p className="subtle-text">Preview shows the trade plan generated from the saved target allocation set.</p>
          </div>
          <button
            className="primary-button"
            type="button"
            onClick={handleExecute}
            disabled={!previewPlans || previewPlans.length === 0 || isExecuting}
          >
            {isExecuting ? "Executing..." : "Execute Rebalance"}
          </button>
        </div>

        {previewError ? <div className="form-error">{previewError}</div> : null}

        {previewPlans === null ? (
          <div className="empty-state">
            <h4>No preview loaded yet</h4>
            <p>Save or preview your target allocation to generate a rebalance plan.</p>
          </div>
        ) : previewPlans.length === 0 ? (
          <div className="empty-state">
            <h4>No rebalance needed</h4>
            <p>Your current allocation is already close enough to the saved target weights.</p>
          </div>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Action</th>
                  <th>Ticker</th>
                  <th>Type</th>
                  <th>Price</th>
                  <th>Target</th>
                  <th>Current</th>
                  <th>Diff</th>
                  <th>Trade Qty</th>
                  <th>Trade Amount</th>
                </tr>
              </thead>
              <tbody>
                {previewPlans.map((plan) => (
                  <tr key={`${plan.transactionType}-${plan.ticker}`}>
                    <td>
                      <span className={`transaction-chip ${getTransactionTone(plan.transactionType)}`}>
                        {plan.transactionType}
                      </span>
                    </td>
                    <td>{plan.ticker}</td>
                    <td>{plan.assetType ?? "—"}</td>
                    <td>{formatCurrency(plan.marketPrice)}</td>
                    <td>{(plan.targetPercentage * 100).toFixed(2)}%</td>
                    <td>{(plan.currentPercentage * 100).toFixed(2)}%</td>
                    <td>{(plan.diffPercentage * 100).toFixed(2)}%</td>
                    <td>{plan.tradeQuantity.toFixed(4)}</td>
                    <td>{formatCurrency(plan.tradeAmount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </article>
    </section>
  );
}
