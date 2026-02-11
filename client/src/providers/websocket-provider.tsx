import {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
  useCallback,
  type ReactNode,
} from "react";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useAuth } from "./auth-provider";

interface WebSocketContextValue {
  subscribe: (
    destination: string,
    callback: (message: IMessage) => void
  ) => string;
  unsubscribe: (id: string) => void;
  publish: (destination: string, body: Record<string, unknown>) => void;
  isConnected: boolean;
}

const WebSocketContext = createContext<WebSocketContextValue | null>(null);

const WS_URL = import.meta.env.VITE_WS_URL || "http://localhost:8080/ws";

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const { token } = useAuth();
  const clientRef = useRef<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const subscriptionsRef = useRef<Map<string, { id: string }>>(new Map());
  const subscriptionCounterRef = useRef(0);

  useEffect(() => {
    if (!token) {
      // Clean up any existing connection when token is removed (logout)
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
        setIsConnected(false);
      }
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setIsConnected(true);
      },
      onDisconnect: () => {
        setIsConnected(false);
      },
      onStompError: (frame) => {
        if (import.meta.env.DEV) {
          console.error("STOMP error:", frame.headers["message"]);
          console.error("Details:", frame.body);
        }
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setIsConnected(false);
    };
  }, [token]);

  const subscribe = useCallback(
    (destination: string, callback: (message: IMessage) => void): string => {
      const id = `sub-${++subscriptionCounterRef.current}`;
      const client = clientRef.current;

      if (client && client.connected) {
        const subscription = client.subscribe(destination, callback, { id });
        subscriptionsRef.current.set(id, subscription);
      } else {
        // Queue subscription for when connection is established
        const checkInterval = setInterval(() => {
          const c = clientRef.current;
          if (c && c.connected) {
            clearInterval(checkInterval);
            const subscription = c.subscribe(destination, callback, { id });
            subscriptionsRef.current.set(id, subscription);
          }
        }, 500);

        // Store the interval so we can clean it up
        subscriptionsRef.current.set(id, {
          id,
          unsubscribe: () => clearInterval(checkInterval),
        } as unknown as { id: string });
      }

      return id;
    },
    []
  );

  const publish = useCallback(
    (destination: string, body: Record<string, unknown>) => {
      const client = clientRef.current;
      if (client && client.connected) {
        client.publish({ destination, body: JSON.stringify(body) });
      }
    },
    []
  );

  const unsubscribe = useCallback((id: string) => {
    const subscription = subscriptionsRef.current.get(id);
    if (subscription) {
      try {
        const client = clientRef.current;
        if (client && client.connected) {
          client.unsubscribe(id);
        }
      } catch {
        // Ignore unsubscribe errors
      }
      subscriptionsRef.current.delete(id);
    }
  }, []);

  return (
    <WebSocketContext.Provider value={{ subscribe, unsubscribe, publish, isConnected }}>
      {children}
    </WebSocketContext.Provider>
  );
}

export function useWebSocket(): WebSocketContextValue {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error("useWebSocket must be used within a WebSocketProvider");
  }
  return context;
}
