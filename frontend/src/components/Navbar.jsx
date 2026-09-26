import { useEffect, useState } from 'react';
import { api } from '../api';

export default function Navbar({ activePage, setActivePage, onLogout }) {
  const [user, setUser] = useState(null);
  const [menuOpen, setMenuOpen] = useState(false);
  useEffect(() => { api.me().then(setUser).catch(() => {}); }, []);
  const items = [['market', 'Markets'], ['portfolio', 'Portfolio'], ['wallet', 'Wallet'], ['orders', 'Orders']];
  return <nav className="topbar"><div className="topbar-inner">
    <button className="brand" onClick={() => setActivePage('market')}><span className="brand-mark">L</span><span>Let's Trade <small>Paper exchange</small></span></button>
    <div className="nav-links">{items.map(([id, label]) => <button key={id} className={'nav-link ' + (activePage === id ? 'active' : '')} onClick={() => setActivePage(id)}>{label}</button>)}</div>
    <div className="account"><span className="account-name">{user?.name?.split(' ')[0]}</span><button className="avatar" aria-label="Account menu" onClick={() => setMenuOpen(v => !v)}>{user?.name?.[0]?.toUpperCase() || '?'}</button>
      {menuOpen && <div className="account-menu"><div className="account-details"><strong>{user?.name || 'Account'}</strong><span>{user?.email}</span></div><button className="menu-action" onClick={onLogout}>Sign out</button></div>}
    </div>
  </div></nav>;
}
