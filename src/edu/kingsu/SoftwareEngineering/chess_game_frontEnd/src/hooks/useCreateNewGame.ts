import { handleGameIdSave } from "@/context/GameContext";
import { useEffect, useState } from "react";

export interface initialGameState {
  gameId: string;
  rev: number;
  position: Record<string, string>; 
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

  const getInitialBoardDetails = async (): Promise<initialGameState> => {
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
        throw new Error(String(message));
      }

      const created = json as initialGameState;
      handleGameIdSave(created.gameId ?? "");
      setData(created);
      return created;
    } catch (e: unknown) {
      const message =
        e instanceof Error
          ? e.message
          : "Something went wrong while getting initial board setup";
      setError(message);
      throw e instanceof Error ? e : new Error(message);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getInitialBoardDetails().then();
  }, []);

  return { data, isLoading, error, create: getInitialBoardDetails };
}

export default useCreateNewGame;
