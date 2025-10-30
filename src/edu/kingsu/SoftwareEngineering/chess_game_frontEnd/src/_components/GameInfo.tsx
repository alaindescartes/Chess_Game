import React from "react";
import { useGameContext } from "@/context/GameContext";

function statusBadgeClass(status: string | undefined) {
  const key = (status || "IN_PROGRESS").toLowerCase();
  switch (key) {
    case "check":
      return "bg-orange-500/25 text-orange-600 dark:text-orange-400";
    case "checkmate":
      return "bg-red-500/25 text-red-700 dark:text-red-400";
    case "draw":
      return "bg-violet-600/25 text-violet-600 dark:text-violet-400";
    default:
      return "bg-blue-500/25 text-blue-700 dark:text-blue-400";
  }
}

function GameInfo() {
  const { game, isLoading } = useGameContext();

  const turnSide = game.turn === "WHITE" ? "white" : "black";
  const turnLabel = game.turn === "WHITE" ? "White to move" : "Black to move";
  const lastMove =
    game.lastFrom && game.lastTo ? `${game.lastFrom} → ${game.lastTo}` : "—";
  const statusLabel = game.status?.replace(/_/g, " ") || "IN PROGRESS";

  return (
    <div
      className="w-full h-full min-h-[100dvh] box-border p-3.5 rounded-xl bg-gradient-to-br from-sky-500/20 to-violet-500/20 backdrop-saturate-150 text-sm leading-tight"
      aria-busy={isLoading}
    >
      <div className="flex items-center justify-between mb-2.5">
        <h3 className="m-0 text-base font-bold tracking-[0.2px]">Game</h3>
        <span
          className={`px-2.5 py-0.5 rounded-full text-xs capitalize font-semibold ${statusBadgeClass(
            game.status
          )}`}
        >
          {statusLabel}
        </span>
      </div>

      <div className="flex items-center justify-between py-2">
        <span className="text-gray-700 dark:text-blue-300 font-semibold">
          Turn
        </span>
        <span className="font-semibold inline-flex items-center gap-2.5">
          <i
            className={`inline-block w-3 h-3 rounded-full ring-2 ring-black/25 ${
              turnSide === "white" ? "bg-white" : "bg-slate-900"
            }`}
          />
          {turnLabel}
        </span>
      </div>

      <div className="flex items-center justify-between py-2">
        <span className="text-gray-700 dark:text-blue-300 font-semibold">
          Revision
        </span>
        <code className="text-xs px-2 py-0.5 rounded-md bg-indigo-500/20 text-indigo-800 dark:text-indigo-200">
          #{game.rev}
        </code>
      </div>

      <div className="flex items-center justify-between py-2">
        <span className="text-gray-700 dark:text-blue-300 font-semibold">
          Last move
        </span>
        <span className="font-semibold">{lastMove}</span>
      </div>

      <hr className="border-0 border-t-2 border-black/10 my-2.5" />

      <div>
        <div className="text-xs uppercase tracking-wide text-sky-500 mb-2 font-extrabold">
          Captured (dummy data)
        </div>
        <div className="flex flex-wrap gap-2">
          <span className="text-xs px-2.5 py-1 rounded-full bg-gradient-to-br from-emerald-500/30 to-blue-500/30 text-slate-900 dark:text-slate-100 font-bold">
            wP×2
          </span>
          <span className="text-xs px-2.5 py-1 rounded-full bg-gradient-to-br from-emerald-500/30 to-blue-500/30 text-slate-900 dark:text-slate-100 font-bold">
            bN
          </span>
          <span className="text-xs px-2.5 py-1 rounded-full bg-gradient-to-br from-emerald-500/30 to-blue-500/30 text-slate-900 dark:text-slate-100 font-bold">
            wB
          </span>
          <span className="text-xs px-2.5 py-1 rounded-full bg-gradient-to-br from-emerald-500/30 to-blue-500/30 text-slate-900 dark:text-slate-100 font-bold">
            bP
          </span>
        </div>
      </div>

      <div className="flex gap-2 mt-3">
        <button
          className="px-3 py-2 rounded-lg bg-gradient-to-br from-cyan-500 to-violet-500 text-white font-bold tracking-wide cursor-not-allowed opacity-90"
          disabled
          title="Coming soon"
        >
          New game
        </button>
        <button
          className="px-3 py-2 rounded-lg bg-gradient-to-br from-cyan-500 to-violet-500 text-white font-bold tracking-wide cursor-not-allowed opacity-90"
          disabled
          title="Coming soon"
        >
          Resync
        </button>
      </div>
    </div>
  );
}

export default GameInfo;
