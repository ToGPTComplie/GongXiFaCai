const widgetScriptId = "tradingview-mini-chart-script";

export function ensureMiniChartScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    const existingScript = document.getElementById(widgetScriptId) as HTMLScriptElement | null;

    if (existingScript?.dataset.ready === "true") {
      resolve();
      return;
    }

    if (existingScript) {
      existingScript.addEventListener("load", () => resolve(), { once: true });
      existingScript.addEventListener("error", () => reject(new Error("Failed to load TradingView widget")), {
        once: true,
      });
      return;
    }

    const script = document.createElement("script");
    script.id = widgetScriptId;
    script.type = "module";
    script.src = "https://widgets.tradingview-widget.com/w/en/tv-mini-chart.js";
    script.onload = () => {
      script.dataset.ready = "true";
      resolve();
    };
    script.onerror = () => {
      reject(new Error("Failed to load TradingView widget"));
    };
    document.head.appendChild(script);
  });
}
