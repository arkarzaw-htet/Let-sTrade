// ─────────────────────────────────────────────
//  WebSocket hooks
//  • usePricesWS    — STOMP/SockJS → /topic/prices  (public, no auth)
//  • usePortfolioWS — native WebSocket → /ws/portfolio?token=JWT (authenticated)
//    The backend PortfolioWebSocketHandler registers the session in userSessions
//    on connect and pushes a recalculated portfolio on every Binance price tick.
// ─────────────────────────────────────────────
import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const STOMP_URL    = 'http://localhost:8080/ws/market';
const PORTFOLIO_WS = 'ws://localhost:8080/ws/portfolio';

function getToken() {
  return localStorage.getItem('let_trade_token');
}

// ── usePricesWS ────────────────────────────────────────────────────────────
// Subscribes to STOMP /topic/prices.  onPrice is called for every tick.
export function usePricesWS({ onPrice, enabled = true }) {
  // Store callback in a ref so the effect never needs to re-run when it changes
  const onPriceRef = useRef(onPrice);
  onPriceRef.current = onPrice;

  useEffect(() => {
    if (!enabled) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(STOMP_URL),
      reconnectDelay: 4000,
      onConnect: () => {
        client.subscribe('/topic/prices', (msg) => {
          try { onPriceRef.current?.(JSON.parse(msg.body)); } catch {}
        });
      },
    });
    client.activate();
    return () => client.deactivate();
  }, [enabled]);  // stable — onPrice accessed via ref
}

// ── usePortfolioWS ─────────────────────────────────────────────────────────
// Connects to the RAW WebSocket endpoint: /ws/portfolio?token=<JWT>
//
// Backend flow:
//   1. afterConnectionEstablished → reads ?token=, validates JWT,
//      calls authenticateSession() which adds the session to userSessions
//      and immediately sends the current portfolio snapshot.
//   2. BinanceWebSocketService.onPriceUpdated() iterates userSessions and
//      calls notifyUserPortfolioChanged(email) for every connected user.
//   3. notifyUserPortfolioChanged() recalculates portfolio and pushes JSON
//      directly to the raw WebSocket session.
//
// Auto-reconnects every 3 s if the connection drops.
export function usePortfolioWS({ onUpdate, enabled = true, onStatus }) {
  // Keep callbacks in refs — effect only re-runs when `enabled` changes
  const onUpdateRef = useRef(onUpdate);
  const onStatusRef = useRef(onStatus);
  onUpdateRef.current = onUpdate;
  onStatusRef.current = onStatus;

  useEffect(() => {
    if (!enabled) return;

    const token = getToken();
    if (!token) { onStatusRef.current?.('error'); return; }

    let destroyed  = false;
    let retryTimer = null;
    let ws         = null;

    function connect() {
      if (destroyed) return;
      onStatusRef.current?.('connecting');

      ws = new WebSocket(`${PORTFOLIO_WS}?token=${encodeURIComponent(token)}`);

      ws.onopen = () => {
        if (destroyed) { ws.close(); return; }
        onStatusRef.current?.('connected');
      };

      ws.onmessage = ({ data }) => {
        if (destroyed) return;
        try {
          const payload = JSON.parse(data);
          // Filter out auth-status messages; forward only real portfolio objects
          if (payload.totalValueUsdt !== undefined || payload.holdings !== undefined) {
            onUpdateRef.current?.(payload);
          }
        } catch {}
      };

      ws.onclose = () => {
        if (destroyed) return;
        onStatusRef.current?.('disconnected');
        retryTimer = setTimeout(connect, 3000);
      };

      ws.onerror = () => {
        onStatusRef.current?.('error');
        ws.close();
      };
    }

    connect();

    return () => {
      destroyed = true;
      clearTimeout(retryTimer);
      ws?.close();
    };
  }, [enabled]);
}
