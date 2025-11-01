import React, { useEffect, useState } from "react";
import { GameState } from "@/context/GameContext";

function useGetSavedGame(id: string) {
  const [data, setData] = useState<GameState | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const URL = "http://localhost:8080/api/game";

  const getSavedGameDetails = async () => {
    if (id === "") return;
    try {
      setIsLoading(true);
      setError(null);
      const res = await fetch(`${URL}/${encodeURIComponent(id)}`, {
        method: "GET",
        headers: { "content-type": "application/json" },
      });

      const json = await res.json();
      if (!res.ok) {
        const message = "Could not get game with that Id";
        setError(message);
      }
      setData(json);
    } catch (e: unknown) {
      const message =
        e instanceof Error
          ? e.message
          : "Something went wrong while getting game with that Id";
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getSavedGameDetails();
  }, [id]);

  return { data, error, isLoading };
}

export default useGetSavedGame;
