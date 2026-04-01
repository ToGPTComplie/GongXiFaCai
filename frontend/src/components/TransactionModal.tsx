import { FormEvent, useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { createFundTransaction, createTradeTransaction, getUserHoldings } from "../lib/api";
import { formatCurrency } from "../lib/format";
import type { Holding } from "../lib/types";
import { CustomSelect } from "./CustomSelect";

interface TransactionModalProps {
  userId: string;
}

export function TransactionModal({ userId }: TransactionModalProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const [transactionType, setTransactionType] = useState("DEPOSIT");
  const [assetType, setAssetType] = useState("STOCK");
  const [ticker, setTicker] = useState("");
  const [quantity, setQuantity] = useState("");
  const [price, setPrice] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [holdingsError, setHoldingsError] = useState<string | null>(null);

  const isCashAction = transactionType === "DEPOSIT" || transactionType === "WITHDRAW";
  const submitLabel = useMemo(() => {
    switch (transactionType) {
      case "DEPOSIT":
        return "Confirm Deposit";
      case "WITHDRAW":
        return "Confirm Withdraw";
      default:
      return "Submit";
    }
  }, [transactionType]);

  const submitToneClass = transactionType === "DEPOSIT" ? "transaction-action-button positive" : "transaction-action-button negative";

  const cashAmount = isCashAction && amount ? Number(amount) : null;

  function closeModal() {
    navigate(location.pathname, { replace: true });
  }

  function resetError() {
    if (formError) {
      setFormError(null);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError(null);

    try {
      setIsSubmitting(true);

      if (isCashAction) {
        const parsedAmount = Number(amount);

        if (!amount || Number.isNaN(parsedAmount) || parsedAmount <= 0) {
          throw new Error("Amount must be greater than 0");
        }

        await createFundTransaction(userId, {
          transactionType: transactionType as "DEPOSIT" | "WITHDRAW",
          amount: parsedAmount,
          description: description.trim() || undefined,
        });
      }

      window.dispatchEvent(new Event("portfolio:refresh"));
      window.dispatchEvent(new CustomEvent("transactions:refresh", { detail: { userId } }));
      closeModal();
    } catch (submitError) {
      setFormError(submitError instanceof Error ? submitError.message : "Failed to submit transaction");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="transaction-title">
        <div className="modal-header">
          <div>
            <p className="eyebrow">Action</p>
            <h2 id="transaction-title">New transaction</h2>
          </div>
          <Link className="ghost-button" to={location.pathname}>
            Close
          </Link>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-grid">
            <CustomSelect
              label="Transaction type"
              value={transactionType}
              disabled={isSubmitting}
              onChange={(nextValue) => {
                resetError();
                setTransactionType(nextValue);
              }}
              options={[
                { value: "DEPOSIT", label: "Deposit" },
                { value: "WITHDRAW", label: "Withdraw" },
              ]}
            />

            <label className="field-group">
              <span className="field-label">Amount</span>
              <input
                placeholder="10000.00"
                type="number"
                min="0"
                step="0.01"
                value={amount}
                onChange={(event) => {
                  resetError();
                  setAmount(event.target.value);
                }}
                disabled={isSubmitting}
              />
            </label>
            <label className="field-group modal-grid-span-2">
              <span className="field-label">Description</span>
              <input
                placeholder="Salary transfer or cash adjustment"
                value={description}
                onChange={(event) => {
                  resetError();
                  setDescription(event.target.value);
                }}
                disabled={isSubmitting}
              />
            </label>

            <div className="summary-tile modal-grid-span-2">
              <span>Cash movement</span>
              <strong>
                {cashAmount !== null ? formatCurrency(cashAmount) : "Enter values"}
              </strong>
              <p className="summary-detail">Amount will adjust available cash after the backend validates the request.</p>
            </div>
          </div>

          {holdingsError ? <div className="form-error">{holdingsError}</div> : null}
          {formError ? <div className="form-error">{formError}</div> : null}

          <div className="modal-footer">
            <p className="helper-text">User ID: {userId}. Deposit and withdraw stay on the dashboard.</p>
            <button className={submitToneClass} type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Submitting..." : submitLabel}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
