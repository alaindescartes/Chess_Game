import { handleGameIdSave } from "@/context/GameContext";
import { useEffect, useState } from "react";

export interface initialGameState {
  gameId: string;
  rev: number;
  position: Record<string, string>; // e.g. { a1: 'wR', b1: 'wN', ... }
  turn: "WHITE" | "BLACK";
  status: "IN_PROGRESS" | "CHECKMATE" | "STALEMATE" | string;
  lastFrom: string | null;
  lastTo: string | null;
}

function useCreateNewGame() {
  const URL = "http://localhost:8080/api/game";
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<initialGameState | null>(null);

  const getInitialBoardDetails = async () => {
    try {
      setIsLoading(true);
      setError(null);

      const res = await fetch(`${URL}`, {
        method: "POST",
        headers: { Accept: "application/json" },
      });

      const json = await res.json();
      if (!res.ok) {
        const message =
          (json && typeof json === "object" && (json.message || json.error)) ||
          "Could not get initial game setup";
        setError(message);
        return;
      }

      handleGameIdSave((json as initialGameState).gameId ?? "");
      setData(json as initialGameState);
    } catch (e: unknown) {
      const message =
        e instanceof Error
          ? e.message
          : "Something went wrong while getting initial board setup";
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getInitialBoardDetails();
  }, []);

  return { data, isLoading, error };
}

export default useCreateNewGame;
