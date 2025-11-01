import React, { useMemo } from "react";

const FullscreenSkeleton: React.FC = () => {
  const squares = useMemo(() => Array.from({ length: 64 }, (_, i) => i), []);
  const container: React.CSSProperties = {
    position: "fixed",
    inset: 0,
    zIndex: 9999,
    display: "grid",
    gridTemplateColumns: "repeat(8, 1fr)",
    gridTemplateRows: "repeat(8, 1fr)",
    background: "#0f172a",
  };
  return (
    <div style={container} aria-label="Loading game">
      <style>{`
        @keyframes shimmer { 0% { opacity: 0.6; } 50% { opacity: 1; } 100% { opacity: 0.6; } }
        .sk { animation: shimmer 1.6s ease-in-out infinite; }
        .sk.light { background:#1f2937; }
        .sk.dark  { background:#111827; }
      `}</style>
      {squares.map((i) => {
        const row = Math.floor(i / 8);
        const col = i % 8;
        const dark = (row + col) % 2 === 1;
        return <div key={i} className={`sk ${dark ? "dark" : "light"}`} />;
      })}
    </div>
  );
};

export default FullscreenSkeleton;
