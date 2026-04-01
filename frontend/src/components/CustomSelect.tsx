import { useEffect, useId, useMemo, useRef, useState } from "react";

interface SelectOption {
  label: string;
  value: string;
}

interface CustomSelectProps {
  label: string;
  options: SelectOption[];
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export function CustomSelect({ label, options, value, onChange, disabled = false }: CustomSelectProps) {
  const [isOpen, setIsOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement | null>(null);
  const buttonRef = useRef<HTMLButtonElement | null>(null);
  const listboxId = useId();

  const selectedOption = useMemo(
    () => options.find((option) => option.value === value) ?? options[0],
    [options, value],
  );

  useEffect(() => {
    function handlePointerDown(event: MouseEvent) {
      if (!rootRef.current?.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setIsOpen(false);
        buttonRef.current?.focus();
      }
    }

    document.addEventListener("mousedown", handlePointerDown);
    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("mousedown", handlePointerDown);
      document.removeEventListener("keydown", handleEscape);
    };
  }, []);

  function handleButtonKeyDown(event: React.KeyboardEvent<HTMLButtonElement>) {
    if (disabled) {
      return;
    }

    if (event.key === "ArrowDown" || event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      setIsOpen(true);
    }
  }

  function handleOptionKeyDown(event: React.KeyboardEvent<HTMLButtonElement>, nextValue: string) {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      onChange(nextValue);
      setIsOpen(false);
      buttonRef.current?.focus();
    }
  }

  return (
    <div className="field-group" ref={rootRef}>
      <span className="field-label">{label}</span>
      <div className={`custom-select ${isOpen ? "open" : ""} ${disabled ? "disabled" : ""}`}>
        <button
          ref={buttonRef}
          className="custom-select-trigger"
          type="button"
          aria-haspopup="listbox"
          aria-expanded={isOpen}
          aria-controls={listboxId}
          onClick={() => {
            if (!disabled) {
              setIsOpen((open) => !open);
            }
          }}
          onKeyDown={handleButtonKeyDown}
          disabled={disabled}
        >
          <span>{selectedOption.label}</span>
          <span className="custom-select-chevron" aria-hidden="true" />
        </button>
        {isOpen ? (
          <div className="custom-select-popover">
            <div className="custom-select-list" id={listboxId} role="listbox" aria-label={label}>
              {options.map((option) => {
                const isSelected = option.value === value;

                return (
                  <button
                    key={option.value}
                    className={isSelected ? "custom-select-option selected" : "custom-select-option"}
                    role="option"
                    aria-selected={isSelected}
                    type="button"
                    onClick={() => {
                      onChange(option.value);
                      setIsOpen(false);
                      buttonRef.current?.focus();
                    }}
                    onKeyDown={(event) => handleOptionKeyDown(event, option.value)}
                    disabled={disabled}
                  >
                    <span>{option.label}</span>
                    {isSelected ? <span className="custom-select-check">Selected</span> : null}
                  </button>
                );
              })}
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}
