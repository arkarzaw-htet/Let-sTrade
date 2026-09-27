// ─────────────────────────────────────────────
//  API utility — all fetch calls go here
// ─────────────────────────────────────────────
const BASE = 'https://letstrade.arkarzawhtet.com/api';

function getToken() {
  return localStorage.getItem('let_trade_token');
}

function authHeaders() {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
}

async function request(method, path, body) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: authHeaders(),
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }
  if (!res.ok) {
    const msg = data?.message || data?.error || `HTTP ${res.status}`;
    throw new Error(msg);
  }
  return data;
}

export const api = {
  // Auth
  register: (body) => request('POST', '/auth/register', body),
  login: (body) => request('POST', '/auth/login', body),

  // User
  me: () => request('GET', '/users/me'),

  // Cryptos
  cryptos: () => request('GET', '/cryptocurrencies'),
  crypto: (symbol) => request('GET', `/cryptocurrencies/${symbol}`),

  // Market
  price: (symbol) => request('GET', `/market/${symbol}/price`),
  stats: (symbol) => request('GET', `/market/${symbol}/stats`),

  // Wallet
  wallet: () => request('GET', '/wallet'),
  deposit: (body) => request('POST', '/wallet/deposit', body),

  // Orders
  placeOrder: (body) => request('POST', '/orders', body),
  orders: () => request('GET', '/orders'),
  order: (id) => request('GET', `/orders/${id}`),

  // Portfolio
  portfolio: () => request('GET', '/portfolio'),
};
