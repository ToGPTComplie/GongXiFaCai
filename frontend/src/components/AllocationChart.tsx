import { useMemo, useState } from "react";

interface AllocationChartProps {
  allocation: {
    cash: number;
    stock: number;
    bond: number;
  };
}

interface AllocationSegment {
  key: "cash" | "stock" | "bond";
  label: string;
  value: number;
  color: string;
}

const chartSegments: Omit<AllocationSegment, "value">[] = [
  { key: "cash", label: "Cash", color: "#18222c" },
  { key: "stock", label: "Stock", color: "#d28f37" },
  { key: "bond", label: "Bond", color: "#7b6751" },
];

function polarToCartesian(cx: number, cy: number, radius: number, angleInDegrees: number) {
  const angleInRadians = ((angleInDegrees - 90) * Math.PI) / 180.0;
  return {
    x: cx + radius * Math.cos(angleInRadians),
    y: cy + radius * Math.sin(angleInRadians),
  };
}

function describeArc(cx: number, cy: number, radius: number, startAngle: number, endAngle: number) {
  const start = polarToCartesian(cx, cy, radius, endAngle);
  const end = polarToCartesian(cx, cy, radius, startAngle);
  const largeArcFlag = endAngle - startAngle <= 180 ? "0" : "1";

  return `M ${start.x} ${start.y} A ${radius} ${radius} 0 ${largeArcFlag} 0 ${end.x} ${end.y}`;
}

export function AllocationChart({ allocation }: AllocationChartProps) {
  const segments = useMemo<AllocationSegment[]>(
    () =>
      chartSegments.map((segment) => ({
        ...segment,
        value: allocation[segment.key],
      })),
    [allocation],
  );

  const [activeKey, setActiveKey] = useState<AllocationSegment["key"] | null>(null);
  const activeSegment = segments.find((segment) => segment.key === activeKey) ?? null;

  let cumulativeAngle = 0;
  const radius = 74;
  const strokeWidth = 58;
  const center = 110;

  return (
    <div className="allocation-card">
      <div className="allocation-visual" onMouseLeave={() => setActiveKey(null)}>
        <svg className="allocation-chart" viewBox="0 0 220 220" role="img" aria-label="Asset allocation chart">
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="none"
            stroke="rgba(24, 34, 44, 0.08)"
            strokeWidth={strokeWidth}
          />
          {segments.map((segment) => {
            const startAngle = cumulativeAngle;
            const sweepAngle = (segment.value / 100) * 360;
            const endAngle = cumulativeAngle + sweepAngle;
            cumulativeAngle = endAngle;

            return (
              <path
                key={segment.key}
                className={activeKey === segment.key ? "allocation-arc active" : "allocation-arc"}
                d={describeArc(center, center, radius, startAngle, endAngle)}
                fill="none"
                stroke={segment.color}
                strokeWidth={strokeWidth}
                strokeLinecap="butt"
                onMouseEnter={() => setActiveKey(segment.key)}
              />
            );
          })}
        </svg>
        <div className="allocation-ring-inner">
          <span>{activeSegment ? activeSegment.label : ""}</span>
          <strong>{activeSegment ? `${activeSegment.value}%` : ""}</strong>
        </div>
      </div>
      <ul className="allocation-list">
        {segments.map((segment) => (
          <li
            key={segment.key}
            className={activeKey === segment.key ? "allocation-list-item active" : "allocation-list-item"}
            onMouseEnter={() => setActiveKey(segment.key)}
            onMouseLeave={() => setActiveKey(null)}
          >
            <div className="allocation-label">
              <span className="allocation-swatch" style={{ backgroundColor: segment.color }} aria-hidden="true" />
              <span>{segment.label}</span>
            </div>
            <strong>{segment.value}%</strong>
          </li>
        ))}
      </ul>
    </div>
  );
}
