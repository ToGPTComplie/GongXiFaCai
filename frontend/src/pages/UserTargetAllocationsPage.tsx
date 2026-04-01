import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { CustomSelect } from "../components/CustomSelect";
import { getUserHoldings, saveTargetAllocations } from "../lib/api";
import type { Holding, TargetAllocationRequest, TargetAllocationRow } from "../lib/types";

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

export function UserTargetAllocationsPage() {
  const { id = "1" } = useParams();
  const [rows, setRows] = useState<TargetAllocationRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saveMessage, setSaveMessage] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const controller = new AbortController();

    async function loadHoldings() {
      setError(null);
      setSaveError(null);
      setSaveMessage(null);

      try {
        const holdings = await getUserHoldings(id, controller.signal);
        const initialRows = buildInitialRows(holdings);
        setRows(initialRows.length > 0 ? initialRows : [createEmptyRow(0)]);
      } catch (loadError) {
        if (!controller.signal.aborted) {
          setError(loadError instanceof Error ? loadError.message : "Failed to load holdings");
          setRows([createEmptyRow(0)]);
        }
      }
    }

    void loadHoldings();

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

      if (!Number.isFinite(parsed) || parsed < 0) {
        return "Target percentage must be a valid non-negative number.";
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
  }

  function addRow() {
    setRows((current) => [...current, createEmptyRow(current.length)]);
    setSaveError(null);
    setSaveMessage(null);
  }

  function removeRow(rowId: string) {
    setRows((current) => {
      const nextRows = current.filter((row) => row.id !== rowId);
      return nextRows.length > 0 ? nextRows : [createEmptyRow(0)];
    });
    setSaveError(null);
    setSaveMessage(null);
  }

  async function handleSave() {
    if (!canSave || validationMessage) {
      return;
    }

    const payload: TargetAllocationRequest[] = rows
      .filter((row) => row.ticker.trim() !== "" && row.targetPercentInput.trim() !== "")
      .map((row) => ({
        ticker: normalizeTicker(row.ticker),
        targetPercentage: Number((Number(row.targetPercentInput) / 100).toFixed(4)),
      }));

    try {
      setIsSaving(true);
      setSaveError(null);
      setSaveMessage(null);
      await saveTargetAllocations(id, payload);
      setSaveMessage("Target allocations saved.");
    } catch (submitError) {
      setSaveError(submitError instanceof Error ? submitError.message : "Failed to save target allocations");
    } finally {
      setIsSaving(false);
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
        <button className="primary-button" type="button" onClick={handleSave} disabled={!canSave || isSaving}>
          {isSaving ? "Saving..." : "Save Targets"}
        </button>
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
    </section>
  );
}
