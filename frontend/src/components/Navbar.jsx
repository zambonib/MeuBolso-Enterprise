import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

export const Navbar = () => {
  const { user, logout } = useAuth();
  const [theme, setTheme] = useState(() => {
    return document.documentElement.getAttribute('data-theme') || 'light';
  });

  const toggleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(nextTheme);
    document.documentElement.setAttribute('data-theme', nextTheme);
  };

  const getInitial = () => {
    if (!user) return 'U';
    const nameStr = user.name || user.username || user.email || 'U';
    return nameStr.charAt(0).toUpperCase();
  };

  return (
    <header className="navbar">
      <div className="navbar-brand">
        <div className="navbar-brand-icon" style={{ display: 'flex', alignItems: 'center' }}>
          <img src="/logo.svg" alt="Logo Meu Bolso Enterprise" style={{ width: '32px', height: '32px', objectFit: 'contain' }} />
        </div>
        <span>Meu Bolso Enterprise</span>
      </div>

      <div className="navbar-actions">
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          onClick={toggleTheme}
          title="Alternar Tema"
        >
          {theme === 'light' ? '🌙 Escuro' : '☀️ Claro'}
        </button>

        {user && (
          <div className="user-pill">
            <div className="user-avatar">{getInitial()}</div>
            <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1.2 }}>
              <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>{user.name || user.username}</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--nimbus-text-muted)' }}>{user.email}</span>
            </div>
          </div>
        )}

        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={logout}
        >
          Sair
        </button>
      </div>
    </header>
  );
};

export default Navbar;
