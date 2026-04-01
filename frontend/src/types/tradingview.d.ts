import type { CSSProperties, DetailedHTMLProps, HTMLAttributes } from "react";

declare module "react" {
  namespace JSX {
    interface IntrinsicElements {
      "tv-mini-chart": DetailedHTMLProps<HTMLAttributes<HTMLElement>, HTMLElement> & {
        symbol: string;
        "line-chart-type"?: string;
        style?: CSSProperties;
      };
    }
  }
}

export {};
