interface StompSubscriptionOptions<T> {
  destination: string;
  onMessage: (payload: T) => void;
  onError?: (message: string) => void;
}

function buildWebSocketUrl(path: string) {
  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";

  if (import.meta.env.DEV) {
    return `${protocol}//${window.location.hostname}:8080${path}`;
  }

  return `${protocol}//${window.location.host}${path}`;
}

export function subscribeToTopic<T>({
  destination,
  onMessage,
  onError,
}: StompSubscriptionOptions<T>) {
  const socket = new WebSocket(buildWebSocketUrl("/ws/market-price"));
  const subscriptionId = `sub-${Math.random().toString(36).slice(2, 8)}`;
  let heartbeatTimer: number | null = null;

  function sendFrame(command: string, headers: Record<string, string> = {}, body = "") {
    const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`);
    const frame = `${command}\n${headerLines.join("\n")}\n\n${body}\u0000`;
    socket.send(frame);
  }

  function clearHeartbeat() {
    if (heartbeatTimer !== null) {
      window.clearInterval(heartbeatTimer);
      heartbeatTimer = null;
    }
  }

  socket.addEventListener("open", () => {
    sendFrame("CONNECT", {
      "accept-version": "1.2",
      "heart-beat": "10000,10000",
    });
  });

  socket.addEventListener("message", (event) => {
    const data = typeof event.data === "string" ? event.data : "";
    const frames = data.split("\u0000").filter((frame) => frame.trim().length > 0);

    for (const frame of frames) {
      if (frame === "\n") {
        continue;
      }

      const [headerBlock, ...bodyParts] = frame.split("\n\n");
      const headerLines = headerBlock.split("\n");
      const command = headerLines[0]?.trim();
      const body = bodyParts.join("\n\n").trim();

      if (command === "CONNECTED") {
        sendFrame("SUBSCRIBE", {
          id: subscriptionId,
          destination,
        });

        heartbeatTimer = window.setInterval(() => {
          if (socket.readyState === WebSocket.OPEN) {
            socket.send("\n");
          }
        }, 10000);
        continue;
      }

      if (command === "MESSAGE" && body) {
        try {
          onMessage(JSON.parse(body) as T);
        } catch {
          onError?.("Failed to parse live holdings payload");
        }
      }

      if (command === "ERROR") {
        onError?.(body || "Live holdings channel returned an error");
      }
    }
  });

  socket.addEventListener("error", () => {
    onError?.("Live holdings channel is unavailable");
  });

  socket.addEventListener("close", () => {
    clearHeartbeat();
  });

  return () => {
    clearHeartbeat();

    if (socket.readyState === WebSocket.OPEN) {
      sendFrame("UNSUBSCRIBE", { id: subscriptionId });
      sendFrame("DISCONNECT");
    }

    socket.close();
  };
}
