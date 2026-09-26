
import { useState, useCallback } from 'react';
import './index.css';
import AuthPage from './pages/AuthPage'; import MarketPage from './pages/MarketPage'; import PortfolioPage from './pages/PortfolioPage'; import WalletPage from './pages/WalletPage'; import OrdersPage from './pages/OrdersPage'; import Navbar from './components/Navbar'; import TickerBar from './components/TickerBar'; import TradeModal from './components/TradeModal';
function Toasts({toasts}) { return <div className="toast-container">{toasts.map(t => <div key={t.id} className={'toast toast-'+t.type}>{t.msg}</div>)}</div>; }
export default function App() {
 const [token,setToken]=useState(()=>localStorage.getItem('let_trade_token')); const [page,setPage]=useState('market'); const [tradeCoin,setTradeCoin]=useState(null); const [toasts,setToasts]=useState([]);
 const showToast=useCallback(({type,msg})=>{const id=Date.now();setToasts(p=>[...p,{id,type,msg}]);setTimeout(()=>setToasts(p=>p.filter(t=>t.id!==id)),4000)},[]);
 if(!token) return <><AuthPage onLogin={tok=>{setToken(tok);setPage('market')}}/><Toasts toasts={toasts}/></>;
 const current={market:<MarketPage onTrade={setTradeCoin}/>,portfolio:<PortfolioPage/>,wallet:<WalletPage onToast={showToast}/>,orders:<OrdersPage/>}[page] || <MarketPage onTrade={setTradeCoin}/>;
 return <div className="app-shell"><TickerBar/><Navbar activePage={page} setActivePage={setPage} onLogout={()=>{localStorage.removeItem('let_trade_token');setToken(null);setPage('market')}}/><main className="app-main"><div className="app-content">{current}</div></main>{tradeCoin&&<TradeModal coin={tradeCoin} onClose={()=>setTradeCoin(null)} onSuccess={toast=>{setTradeCoin(null);showToast(toast)}}/>}<Toasts toasts={toasts}/></div>;
}
