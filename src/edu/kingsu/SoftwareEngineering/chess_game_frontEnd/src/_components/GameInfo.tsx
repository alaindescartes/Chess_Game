import React, { useEffect, useState } from "react";
import {
  GameState,
  getSavedIds,
  savedId,
  useGameContext,
} from "@/context/GameContext";
import useCreateNewGame from "@/hooks/useCreateNewGame";
import { toast } from "sonner";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import useGetSavedGame from "@/hooks/useGetSavedGame";

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
  const { game, isLoading, setGame } = useGameContext();
  const { isLoading: gameLoading, create } = useCreateNewGame();
  const [gameIds, setGameIds] = useState<savedId[]>([]);
  const [tempSavedId, setTempSavedId] = useState("");
  const { data: savedGame, error: resumeError } = useGetSavedGame(tempSavedId);

  const turnSide = game.turn === "WHITE" ? "white" : "black";
  const turnLabel = game.turn === "WHITE" ? "White to move" : "Black to move";
  const lastMove =
    game.lastFrom && game.lastTo ? `${game.lastFrom} → ${game.lastTo}` : "—";
  const statusLabel = String(game.status ?? "IN_PROGRESS").replace(/_/g, " ");

  const handleCreateNewGame = async () => {
    try {
      const fresh = await create();
      setGame(fresh as GameState);
      toast("New game has been started, enjoy.", {
        style: { backgroundColor: "green", color: "white" },
      });
    } catch (e: unknown) {
      const msg =
        e instanceof Error
          ? e?.message
          : "There was a problem when creating new Game";
      toast(msg, { style: { backgroundColor: "red", color: "white" } });
    }
  };

  const handleResume = (id: string) => {
    if (id === game.gameId) {
      toast("game is currently loaded");
    }
    setTempSavedId(id);
  };

  useEffect(() => {
    if (savedGame) {
      setGame(savedGame as GameState);
      toast("Game loaded.", {
        style: { backgroundColor: "#10b981", color: "white" },
      });
    }
  }, [savedGame, setGame]);

  useEffect(() => {
    if (resumeError) {
      toast(
        typeof resumeError === "string"
          ? resumeError
          : "Failed to load saved game",
        { style: { backgroundColor: "#ef4444", color: "white" } }
      );
    }
  }, [resumeError]);

  useEffect(() => {
    setGameIds(getSavedIds());
  }, [game.gameId]);

  return (
    <div
      className="w-full h-full min-h-dvh box-border p-3.5 rounded-xl bg-linear-to-br from-sky-500/20 to-violet-500/20 backdrop-saturate-150 text-sm leading-tight"
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
          <span className="text-xs px-2.5 py-1 rounded-full bg-linear-to-br from-emerald-500/30 to-blue-500/30 text-slate-900 dark:text-slate-100 font-bold">
            wP×2
          </span>
          <span className="text-xs px-2.5 py-1 rounded-full bg-linear-to-br from-emerald-500/30 to-blue-500/30 text-slate-900 dark:text-slate-100 font-bold">
            bN
          </span>
          <span className="text-xs px-2.5 py-1 rounded-full bg-linear-to-br from-emerald-500/30 to-blue-500/30 text-slate-900 dark:text-slate-100 font-bold">
            wB
          </span>
          <span className="text-xs px-2.5 py-1 rounded-full bg-linear-to-br from-emerald-500/30 to-blue-500/30 text-slate-900 dark:text-slate-100 font-bold">
            bP
          </span>
        </div>
      </div>

      <div className="flex gap-2 mt-3">
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <button
              className="px-3 py-2 rounded-lg bg-linear-to-br from-cyan-500 to-violet-500 text-white font-semibold shadow-md hover:shadow-lg transition active:scale-[.98] focus:outline-none focus:ring-2 focus:ring-violet-400/60 disabled:opacity-60 disabled:cursor-not-allowed"
              disabled={gameLoading}
              title={
                gameLoading ? "Creating a new game..." : "Start a new game"
              }
            >
              New game
            </button>
          </AlertDialogTrigger>
          <AlertDialogContent className="max-w-md w-[92vw] rounded-xl border border-slate-200/60 dark:border-slate-700/60 bg-white/90 dark:bg-slate-900/90 backdrop-blur-md shadow-2xl">
            <AlertDialogHeader>
              <AlertDialogTitle className="text-lg font-bold">
                Are you absolutely sure?
              </AlertDialogTitle>
              <AlertDialogDescription className="text-sm text-slate-600 dark:text-slate-300">
                Starting a new game will affect your current session. Review the
                changes below.
              </AlertDialogDescription>
              <div className="text-sm text-slate-600 dark:text-slate-300">
                <ul className="list-disc pl-5 space-y-1 mt-2">
                  <li>
                    Create a fresh board in the standard starting position
                    (White to move, revision #0).
                  </li>
                  <li>
                    Keep your current game available in the Game panel so you
                    can resume it later.
                  </li>
                  <li>Clear any pending piece selections on the board.</li>
                </ul>
                <p className="mt-2">
                  This action cannot be undone from this dialog. A new game will
                  receive a new ID.
                </p>
              </div>
            </AlertDialogHeader>
            <AlertDialogFooter className="gap-2">
              <AlertDialogCancel className="px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-transparent hover:bg-slate-100/60 dark:hover:bg-slate-800/60 transition">
                Cancel
              </AlertDialogCancel>
              <AlertDialogAction
                className="px-3 py-2 rounded-lg bg-linear-to-br from-cyan-500 to-violet-500 text-white font-semibold shadow-md hover:shadow-lg transition active:scale-[.98] focus:outline-none focus:ring-2 focus:ring-violet-400/60 disabled:opacity-60 disabled:cursor-not-allowed"
                disabled={gameLoading}
                onClick={handleCreateNewGame}
              >
                {gameLoading ? "Creating new game..." : "Continue"}
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>
      <section className="mt-4">
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-sm font-bold tracking-wide uppercase text-sky-600 dark:text-sky-400">
            Saved Games
          </h2>
          <span className="text-xs text-slate-500 dark:text-slate-400">
            {gameIds?.length ?? 0} total
          </span>
        </div>

        {!gameIds || gameIds.length === 0 ? (
          <div className="rounded-lg border border-dashed border-slate-300 dark:border-slate-700 p-4 text-sm text-slate-600 dark:text-slate-300">
            No saved games yet. Start a new game to see it listed here.
          </div>
        ) : (
          <ul className="divide-y divide-slate-200 dark:divide-slate-800 rounded-xl overflow-hidden border border-slate-200/70 dark:border-slate-700/70 bg-white/60 dark:bg-slate-900/60 backdrop-blur">
            {gameIds.map((o, index) => (
              <li
                key={index}
                className="flex items-center gap-3 p-3 hover:bg-sky-50/80 dark:hover:bg-slate-800/60 transition"
              >
                <div className="flex-1 min-w-0">
                  <div className="text-xs text-slate-500 dark:text-slate-400">
                    {o.date}
                  </div>
                  <div
                    className="text-sm font-mono truncate text-slate-900 dark:text-slate-100"
                    title={o.id}
                  >
                    {o.id}
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleResume(o.id)}
                    className="px-2.5 py-1.5 rounded-md bg-linear-to-br from-emerald-500 to-teal-500 text-white text-xs font-semibold shadow hover:shadow-md active:scale-[.98] focus:outline-none focus:ring-2 focus:ring-emerald-400/60"
                    title="Copy ID"
                  >
                    play
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}

export default GameInfo;
