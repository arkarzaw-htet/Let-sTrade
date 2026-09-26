import { useState, useCallback } from 'react';
import { usePricesWS } from '../useWebSocket';
const PAIRS = ['BTCUSDT','ETHUSDT','SOLUSDT','BNBUSDT','XRPUSDT','ADAUSDT'];
const fmt = n => Number(n).toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2});
export default function TickerBar() {
  const [prices,setPrices] = useState({}); const [previous,setPrevious] = useState({});
  const onPrice = useCallback(data => { if (!PAIRS.includes(data.symbol)) return; setPrices(old => { setPrevious(p => ({...p,[data.symbol]:old[data.symbol]})); return {...old,[data.symbol]:parseFloat(data.price)}; }); },[]);
  usePricesWS({onPrice,enabled:true});
  return <div className="ticker-bar"><div className="ticker-track">{PAIRS.map(sym => { const price=prices[sym], prev=previous[sym], dir=price>prev?'price-up':price<prev?'price-down':''; return <span className="ticker-item" key={sym}><span className="ticker-symbol">{sym.replace('USDT','')} / USDT</span><span className={'ticker-price '+dir}>{price ? '$'+fmt(price) : '—'}</span></span>; })}</div></div>;
}
