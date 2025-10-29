"use client";
import React from "react";
import useCreateNewGame from "@/hooks/useCreateNewGame";

export type Piece =
  | "wK"
  | "wQ"
  | "wR"
  | "wB"
  | "wN"
  | "wP"
  | "bK"
  | "bQ"
  | "bR"
  | "bB"
  | "bN"
  | "bP";
export type Position = Record<string, Piece>;
export interface GameState {
  gameId: string;
  rev: number;
  position: Position;
  turn: "WHITE" | "BLACK";
  status: string;
  lastFrom: string | null;
  lastTo: string | null;
}

export type GameContextValue = {
  game: GameState;
  setGame: React.Dispatch<React.SetStateAction<GameState>>;
  isLoading: boolean;
  error: unknown;
  refresh?: () => void;
};

const FALLBACK_GAME_STATE: GameState = {
  gameId: "",
  rev: 0,
  position: {},
  turn: "WHITE",
  status: "IN_PROGRESS",
  lastFrom: null,
  lastTo: null,
};

const defaultValue: GameContextValue = {
  game: FALLBACK_GAME_STATE,
  setGame: () => {},
  isLoading: true,
  error: null,
  refresh: undefined,
};

export const GameContext = React.createContext<GameContextValue>(defaultValue);

export default function GameContextProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const { data, isLoading, error } = useCreateNewGame();
  const [game, setGame] = React.useState<GameState>(FALLBACK_GAME_STATE);
  React.useEffect(() => {
    if (data) setGame(data as GameState);
  }, [data]);

  const value = React.useMemo<GameContextValue>(
    () => ({ game, setGame, isLoading, error }),
    [game, isLoading, error]
  );

  return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
}

// Convenience hook for consumers
export function useGameContext() {
  return React.useContext(GameContext);
}
