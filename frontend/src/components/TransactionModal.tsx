import { FormEvent, useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { createFundTransaction, createTradeTransaction } from "../lib/api";
import { formatCurrency } from "../lib/format";
import { CustomSelect } from "./CustomSelect";

interface TransactionModalProps {
  userId: string;
}

export function TransactionModal({ userId }: TransactionModalProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const [transactionType, setTransactionType] = useState("BUY");
  const [assetType, setAssetType] = useState("STOCK");
  const [ticker, setTicker] = useState("");
  const [quantity, setQuantity] = useState("");
  const [price, setPrice] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isCashAction = transactionType === "DEPOSIT" || transactionType === "WITHDRAW";
  const submitLabel = useMemo(() => {
    switch (transactionType) {
      case "BUY":
        return "Confirm Buy";
      case "SELL":
        return "Confirm Sell";
      case "DEPOSIT":
        return "Confirm Deposit";
      case "WITHDRAW":
        return "Confirm Withdraw";
      default:
        return "Submit";
    }
  }, [transactionType]);

  const calculatedTotal = !isCashAction && quantity && price ? Number(quantity) * Number(price) : null;
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
      } else {
        const parsedQuantity = Number(quantity);
        const parsedPrice = Number(price);

        if (!ticker.trim()) {
          throw new Error("Ticker is required");
        }

        if (Number.isNaN(parsedQuantity) || parsedQuantity <= 0) {
          throw new Error("Quantity must be greater than 0");
        }

        if (Number.isNaN(parsedPrice) || parsedPrice <= 0) {
          throw new Error("Price must be greater than 0");
        }

        await createTradeTransaction(userId, {
          ticker: ticker.trim().toUpperCase(),
          assetType: assetType as "STOCK" | "BOND",
          transactionType: transactionType as "BUY" | "SELL",
          quantity: parsedQuantity,
          price: parsedPrice,
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

                if (nextValue === "DEPOSIT" || nextValue === "WITHDRAW") {
                  setAssetType("STOCK");
                }
              }}
              options={[
                { value: "BUY", label: "Buy" },
                { value: "SELL", label: "Sell" },
                { value: "DEPOSIT", label: "Deposit" },
                { value: "WITHDRAW", label: "Withdraw" },
              ]}
            />

            {isCashAction ? (
              <>
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
              </>
            ) : (
              <>
                <label className="field-group">
                  <span className="field-label">Ticker</span>
                  <input
                    placeholder="AAPL"
                    value={ticker}
                    onChange={(event) => {
                      resetError();
                      setTicker(event.target.value);
                    }}
                    disabled={isSubmitting}
                  />
                </label>
                <CustomSelect
                  label="Asset type"
                  value={assetType}
                  disabled={isSubmitting}
                  onChange={(nextValue) => {
                    resetError();
                    setAssetType(nextValue);
                  }}
                  options={[
                    { value: "STOCK", label: "Stock" },
                    { value: "BOND", label: "Bond" },
                  ]}
                />
                <label className="field-group">
                  <span className="field-label">Quantity</span>
                  <input
                    placeholder="10"
                    type="number"
                    min="0"
                    step="0.0001"
                    value={quantity}
                    onChange={(event) => {
                      resetError();
                      setQuantity(event.target.value);
                    }}
                    disabled={isSubmitting}
                  />
                </label>
                <label className="field-group">
                  <span className="field-label">Price</span>
                  <input
                    placeholder="150.00"
                    type="number"
                    min="0"
                    step="0.01"
                    value={price}
                    onChange={(event) => {
                      resetError();
                      setPrice(event.target.value);
                    }}
                    disabled={isSubmitting}
                  />
                </label>
              </>
            )}

            <div className={isCashAction ? "summary-tile modal-grid-span-2" : "summary-tile"}>
              <span>{isCashAction ? "Cash movement" : "Estimated total"}</span>
              <strong>
                {isCashAction && cashAmount !== null
                  ? formatCurrency(cashAmount)
                  : !isCashAction && calculatedTotal !== null
                    ? formatCurrency(calculatedTotal)
                    : "Enter values"}
              </strong>
              <p className="summary-detail">
                {isCashAction
                  ? "Amount will adjust available cash after the backend validates the request."
                  : "Quantity multiplied by price. Holdings and cash will refresh after a successful trade."}
              </p>
            </div>
          </div>

          {formError ? <div className="form-error">{formError}</div> : null}

          <div className="modal-footer">
            <p className="helper-text">User ID: {userId}. Requests are now sent to the backend API.</p>
            <button className="primary-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Submitting..." : submitLabel}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
