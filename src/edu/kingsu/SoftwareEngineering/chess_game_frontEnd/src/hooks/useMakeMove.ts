import React from "react";
import type { GameState } from "@/context/GameContext";

export interface MovePayload {
  from: string;
  to: string;
  clientRev: number;
  promotion?: "Q" | "R" | "B" | "N" | null;
}

function useMakeMove(gameId: string) {
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [data, setData] = React.useState<GameState | null>(null);

  const sendMove = React.useCallback(
    async (payload: MovePayload) => {
      try {
        setIsLoading(true);
        setError(null);

        if (
          !payload?.from ||
          !payload?.to ||
          payload.clientRev === undefined ||
          !gameId
        ) {
          throw new Error("Incomplete move payload");
        }

        const res = await fetch(
          `http://localhost:8080/api/game/${encodeURIComponent(gameId)}/move`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              from: payload.from,
              to: payload.to,
              clientRev: payload.clientRev,
              promotion: payload.promotion ?? null,
            }),
          }
        );

        const json = await res.json();
        if (!res.ok) {
          const msg =
            (json &&
              typeof json === "object" &&
              (json.message || json.error)) ||
            `HTTP ${res.status}`;
          throw new Error(String(msg));
        }

        setData(json as GameState);
        return json as GameState;
      } catch (e: unknown) {
        const message =
          e instanceof Error ? e.message : "Failed to submit move";
        setError(message);
        throw e;
      } finally {
        setIsLoading(false);
      }
    },
    [gameId]
  );

  return { sendMove, data, error, isLoading };
}

export default useMakeMove;
